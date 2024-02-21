/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.places;

import android.location.Location;
import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.services.DataStoring;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NamedCollection;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;
import com.adobe.marketing.mobile.util.TimeUtils;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;

class PlacesState {

	private static final String CLASS_NAME = "PlacesState";
	private static final String PLACES_DATA_STORE = "placesdatastore";

	LinkedHashMap<String, PlacesPOI> cachedPOIs;
	PlacesPOI currentPOI;
	PlacesPOI lastEnteredPOI;
	PlacesPOI lastExitedPOI;
	String authStatus;
	NamedCollection placesDataStore;

	long membershipValidUntil;
	long membershipTtl;

	/**
	 * Constructor.
	 * <p>
	 * Creates a new instance of the {@link PlacesState}.
	 * Attempts to load the previous session data from the places dataStore.
	 */
	PlacesState(@NonNull final DataStoring datastore) {
		// load the persisted POI's to cache variable
		cachedPOIs = new LinkedHashMap<>();
		placesDataStore = datastore.getNamedCollection(PLACES_DATA_STORE);
		loadPersistedPOIs();
	}

	/**
	 * Processes the {@link PlacesQueryResponse} obtained from the Places Query Service call.
	 * <p>
	 * This method,
	 * <ol>
	 * 	   <li> Compares with the existing cached POI's and creates new Entry events if the POI has lately entered into a geoFence
	 * 	   <li> Overrides the cached POIs with the new ones obtained from the query response
	 * 	   <li> Persist the POIs obtained from the query response
	 * </ol>
	 *
	 * @param response the places query response
	 */
	void processNetworkResponse(final PlacesQueryResponse response) {
		// always reset the current POI
		currentPOI = null;

		if (response.containsUserPOIs != null && !response.containsUserPOIs.isEmpty()) {
			// update the currentPOI and LastEnteredPOI to the first value in the query response
			currentPOI = new PlacesPOI(response.containsUserPOIs.get(0));
			lastEnteredPOI = new PlacesPOI(response.containsUserPOIs.get(0)); // should I update the last entered POI here?
		}

		// refresh the cache with POI's obtained from the response
		cachePOIs(response);

		// update the validity of membershipPOI after every successful nearByPOI response
		updateMembershipValidUntilTimestamp();

		// then persist the cached POI's
		persistPOIs();
	}

	/**
	 * Processes the {@link EventType#PLACES} {@link EventSource#REQUEST_CONTENT} region entry/exit {@code Event} and updates the Places State.
	 * <ol>
	 * 	   <li> Reads the regionId from the event's {@code EventData}.
	 * 	   <li> Load the corresponding POI details for the given regionID from cache.
	 * 	   <li> Creates Entry/Exit {@link PlacesRegion} event comparing the previous and current state of the POI.
	 * 	   <li> Updates the shares state variable currentPOI, lastEnteredPOI and lastExitedPOI.
	 * </ol>
	 * <p>
	 * This method returns null when
	 * <ol>
	 * 	   <li> No valid regionId found in Event.
	 * 	   <li> Invalid regionEventType found in Event.
	 * 	   <li> When there does not exist any cached data for the obtained regionID from the Event.
	 * </ol>
	 *
	 * @param event the {@link EventType#PLACES} {@link EventSource#REQUEST_CONTENT} {@link Event}
	 * @return A {@link PlacesRegion} instance representing an Entry/Exit event identified while processing the requested data
	 */
	PlacesRegion processRegionEvent(final Event event) {
		// no need to do null pointer check for event or its eventData
		// check are made prior to handing over to this method
		final Map<String, Object> eventData = event.getEventData();

		// extract the required data from eventData
		final String regionId = DataReader.optString(eventData, PlacesConstants.EventDataKeys.Places.REGION_ID, null);
		final String regionType = DataReader.optString(
			eventData,
			PlacesConstants.EventDataKeys.Places.REGION_EVENT_TYPE,
			PlacesRegion.PLACE_EVENT_NONE
		);

		// bail out if the event has invalid regionId
		if (StringUtils.isNullOrEmpty(regionId)) {
			Log.warning(PlacesConstants.LOG_TAG, "Invalid regionId, Ignoring to process geofence event", regionId);
			return null;
		}

		// identify the POI among the cachedPOIs.
		final PlacesPOI matchedPOI = cachedPOIs.get(regionId);

		// bail out if no matchedPOI is found
		if (matchedPOI == null) {
			Log.warning(
				PlacesConstants.LOG_TAG,
				"Unable to find POI details for regionId : %s, Ignoring to process geofence event",
				regionId
			);
			return null;
		}

		// Edit the POI to containUser, create and dispatch an entry event
		if (regionType.equals(PlacesRegion.PLACE_EVENT_ENTRY)) {
			matchedPOI.setUserIsWithin(true);

			// update shared state variables
			lastEnteredPOI = matchedPOI;
			currentPOI = matchedPOI.comparePriority(currentPOI) ? matchedPOI : currentPOI;

			// update the validity of membershipPOI after every region event
			updateMembershipValidUntilTimestamp();
			persistPOIs();
			return new PlacesRegion(matchedPOI, PlacesRegion.PLACE_EVENT_ENTRY, event.getTimestamp());
		}
		// Edit the POI to not containUser, create and dispatch an exit event
		else if (regionType.equals(PlacesRegion.PLACE_EVENT_EXIT)) {
			if (matchedPOI.equals(currentPOI)) {
				// remove the currentPoi. If that's the poi which is exited
				currentPOI = null;
			}

			matchedPOI.setUserIsWithin(false);
			currentPOI = calculateCurrentPOI();

			// update the shared state variable
			lastExitedPOI = new PlacesPOI(matchedPOI);

			// update the validity of membershipPOI after every region event
			updateMembershipValidUntilTimestamp();
			persistPOIs();
			return new PlacesRegion(matchedPOI, PlacesRegion.PLACE_EVENT_EXIT, event.getTimestamp());
		} else {
			Log.warning(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"Unknown region type : %s, Ignoring process geofence event",
				regionType
			);
			return null;
		}
	}

	/**
	 * Creates the {@code EventData} for the places shared state from the POI's cached in {@link PlacesState}
	 *
	 * @return an {@link Map} representing the places shared state
	 */
	Map<String, Object> getPlacesSharedState() {
		// create and return the shared state eventData
		final Map<String, Object> data = new HashMap<>();

		// ensure that our membership data is still valid, and clear it out if it's not
		if (!isMembershipDataValid()) {
			clearMembershipData();
		}

		if (cachedPOIs != null && !cachedPOIs.isEmpty()) {
			data.put(
				PlacesConstants.SharedStateKeys.NEARBYPOIS,
				PlacesUtil.convertPOIListToMap(new ArrayList<>(cachedPOIs.values()))
			);
		}

		if (authStatus != null) {
			data.put(PlacesConstants.SharedStateKeys.AUTH_STATUS, authStatus);
		}

		if (currentPOI != null) {
			data.put(PlacesConstants.SharedStateKeys.CURRENT_POI, currentPOI.toMap());
		}

		if (lastEnteredPOI != null) {
			data.put(PlacesConstants.SharedStateKeys.LAST_ENTERED_POI, lastEnteredPOI.toMap());
		}

		if (lastExitedPOI != null) {
			data.put(PlacesConstants.SharedStateKeys.LAST_EXITED_POI, lastExitedPOI.toMap());
		}

		data.put(PlacesConstants.SharedStateKeys.VALID_UNTIL, membershipValidUntil);
		return data;
	}

	/**
	 * Gets the Points of Interest within which the device is currently geographically located
	 *
	 * @return A list containing POIs that the user is in. Return empty array if there is no POI that contains user
	 */
	List<PlacesPOI> getUserWithInPOIs() {
		final ArrayList<PlacesPOI> userWithInPOIs = new ArrayList<>();

		for (final PlacesPOI eachPOI : cachedPOIs.values()) {
			if (eachPOI.containsUser()) {
				userWithInPOIs.add(eachPOI);
			}
		}

		return userWithInPOIs;
	}

	/**
	 * Saves the Places Extension's last known location in persistence.
	 * <p>
	 * Passing an invalid latitude or longitude will remove the location from the persistence.
	 *
	 * @param latitude  {@code double} last known latitude
	 * @param longitude {@code double} last known longitude
	 */
	void saveLastKnownLocation(final double latitude, final double longitude) {
		if (placesDataStore == null) {
			Log.warning(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"Unable to persist authorization status, PlacesDatastore not available."
			);
			return;
		}

		if (!PlacesUtil.isValidLat(latitude) || !PlacesUtil.isValidLon(longitude)) {
			placesDataStore.remove(PlacesConstants.DataStoreKeys.LAST_KNOWN_LATITUDE);
			placesDataStore.remove(PlacesConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE);
			return;
		}

		placesDataStore.setDouble(PlacesConstants.DataStoreKeys.LAST_KNOWN_LATITUDE, latitude);
		placesDataStore.setDouble(PlacesConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE, longitude);
	}

	/**
	 * Loads the Places Extension's last known location from persistence.
	 * <p>
	 * Returns null, if the Places DataStore is not available
	 * or if the the latitude or longitude value is invalid.
	 *
	 * @return An {@link Location} instance containing the last known latitude and longitude
	 */
	Location loadLastKnownLocation() {
		if (placesDataStore == null) {
			Log.warning(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"Unable to persist authorization status, PlacesDataStore not available."
			);
			return null;
		}

		double latitude = placesDataStore.getDouble(
			PlacesConstants.DataStoreKeys.LAST_KNOWN_LATITUDE,
			PlacesConstants.INVALID_LAT_LON
		);
		double longitude = placesDataStore.getDouble(
			PlacesConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE,
			PlacesConstants.INVALID_LAT_LON
		);

		if (!PlacesUtil.isValidLat(latitude) || !PlacesUtil.isValidLon(longitude)) {
			return null;
		}

		final Location lastKnownLocation = new Location(PlacesConstants.Location.PROVIDER_TAG);
		lastKnownLocation.setLatitude(latitude);
		lastKnownLocation.setLongitude(longitude);
		return lastKnownLocation;
	}

	/**
	 * Saves the authorization value to persistence.
	 * <p>
	 * Make sure a valid status string is provided. Please use {@link PlacesAuthorizationStatus#isValidStatus(String)} to
	 * validate the status string before passing to this method.
	 * If null value is provided the authorization is set to default value {@link PlacesAuthorizationStatus#DEFAULT_VALUE}
	 *
	 * @param status the string value of {@link PlacesAuthorizationStatus} that needs to be persisted
	 */
	void setAuthorizationStatus(final String status) {
		this.authStatus = status;

		if (this.authStatus == null) {
			this.authStatus = PlacesAuthorizationStatus.DEFAULT_VALUE;
		}

		if (placesDataStore == null) {
			Log.warning(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"localStorage services from mobile core is not available, unable to persist authorization status"
			);
			return;
		}

		placesDataStore.setString(PlacesConstants.DataStoreKeys.AUTH_STATUS, authStatus);
		Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, String.format("Authorization status persisted, %s", authStatus));
	}

	void setMembershiptTtl(final long membershipTtl) {
		this.membershipTtl = membershipTtl;
	}

	/**
	 * Clears all persisted and in-memory data for PlacesState.
	 */
	void clearData() {
		// clear the in memory variables
		cachedPOIs.clear();
		lastExitedPOI = null;
		lastEnteredPOI = null;
		currentPOI = null;
		membershipValidUntil = 0;
		// then persist the empty data
		persistPOIs();

		saveLastKnownLocation(PlacesConstants.INVALID_LAT_LON, PlacesConstants.INVALID_LAT_LON);
		setAuthorizationStatus(PlacesAuthorizationStatus.DEFAULT_VALUE);
	}

	/**
	 * This method caches the nearby/containsUser from the {@link PlacesQueryResponse}.
	 *
	 * @param response the query response
	 */
	private void cachePOIs(final PlacesQueryResponse response) {
		// clear the existing cache
		cachedPOIs.clear();

		// cache containsUsers POI's
		// while caching convert the list into map<id,poiObject> for easy data handling
		if (response.containsUserPOIs != null && !response.containsUserPOIs.isEmpty()) {
			for (final PlacesPOI eachPOI : response.containsUserPOIs) {
				cachedPOIs.put(eachPOI.getIdentifier(), eachPOI);
			}
		}

		// cache NearbyPOIs
		// while caching convert the list into map<id,poiObject> for easy data handling
		if (response.nearByPOIs != null && !response.nearByPOIs.isEmpty()) {
			for (final PlacesPOI eachPOI : response.nearByPOIs) {
				cachedPOIs.put(eachPOI.getIdentifier(), eachPOI);
			}
		}
	}

	private void loadPersistedPOIs() {
		if (placesDataStore == null) {
			Log.warning(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"Unable to load POI's from persistence, placesDataStore not available."
			);
			return;
		}

		// attempt to load cachedPOIs
		cachedPOIs = new LinkedHashMap<>();
		final String nearbyString = placesDataStore.getString(PlacesConstants.DataStoreKeys.NEARBYPOIS, "");

		if (!StringUtils.isNullOrEmpty(nearbyString)) {
			try {
				final JSONObject nearbyJSON = new JSONObject(nearbyString);
				final Iterator<String> keys = nearbyJSON.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					cachedPOIs.put(key, new PlacesPOI(nearbyJSON.getJSONObject(key)));
				}
			} catch (final JSONException exception) {
				Log.warning(PlacesConstants.LOG_TAG, "Unable to load cached POI from JSON String : %s", nearbyString);
			}
		}

		// attempt to load current POI
		final String currentPOIString = placesDataStore.getString(PlacesConstants.DataStoreKeys.CURRENT_POI, "");

		if (!StringUtils.isNullOrEmpty(currentPOIString)) {
			try {
				currentPOI = new PlacesPOI(currentPOIString);
				Log.debug(
					PlacesConstants.LOG_TAG,
					CLASS_NAME,
					"CurrentPOI is loaded from persistence : %s",
					currentPOI
				);
			} catch (final JSONException exp) {
				Log.warning(
					PlacesConstants.LOG_TAG,
					CLASS_NAME,
					"Unable to load currentPOI from persistence : Exception - %s",
					exp
				);
			}
		}

		// attempt to load last entered POI
		final String lastEnteredString = placesDataStore.getString(PlacesConstants.DataStoreKeys.LAST_ENTERED_POI, "");

		if (!StringUtils.isNullOrEmpty(lastEnteredString)) {
			try {
				lastEnteredPOI = new PlacesPOI(lastEnteredString);
				Log.debug(
					PlacesConstants.LOG_TAG,
					CLASS_NAME,
					"Last Entered POI is loaded from persistence : %s",
					lastEnteredPOI
				);
			} catch (final JSONException exp) {
				Log.warning(
					PlacesConstants.LOG_TAG,
					CLASS_NAME,
					"Unable to load last entered POI from persistence : Exception - %s ",
					exp
				);
			}
		}

		// attempt to load last exited POI
		final String lastExitedString = placesDataStore.getString(PlacesConstants.DataStoreKeys.LAST_EXITED_POI, "");

		if (!StringUtils.isNullOrEmpty(lastExitedString)) {
			try {
				lastExitedPOI = new PlacesPOI(lastExitedString);
			} catch (final JSONException exp) {
				Log.warning(
					PlacesConstants.LOG_TAG,
					CLASS_NAME,
					"Unable to load last exited POI from persistence : Exception - %s",
					exp
				);
			}
		}

		// attempt to load authorization status at launch
		authStatus =
			placesDataStore.getString(
				PlacesConstants.DataStoreKeys.AUTH_STATUS,
				PlacesAuthorizationStatus.DEFAULT_VALUE
			);

		// load membership valid until timestamp
		membershipValidUntil = placesDataStore.getLong(PlacesConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL, 0);
	}

	private void persistPOIs() {
		if (placesDataStore == null) {
			Log.error(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"Unable to persist POI's in persistence, placesDataStore not available."
			);
			return;
		}

		// persist nearbyPOIs
		if (cachedPOIs != null && !cachedPOIs.isEmpty()) {
			try {
				final JSONObject nearbyPOIsJSON = new JSONObject();
				for (final String poiID : cachedPOIs.keySet()) {
					nearbyPOIsJSON.put(poiID, new JSONObject(cachedPOIs.get(poiID).toMap()));
				}
				final String jsonString = nearbyPOIsJSON.toString();
				placesDataStore.setString(PlacesConstants.DataStoreKeys.NEARBYPOIS, jsonString);
				Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "nearbyPOIs persisted, %s", jsonString);
			} catch (final Exception e) {
				Log.warning(
					PlacesConstants.LOG_TAG,
					CLASS_NAME,
					String.format("Unable to persist nearByPOIs in persistence, Exception: %s", e.getLocalizedMessage())
				);
			}
		} else {
			placesDataStore.remove(PlacesConstants.DataStoreKeys.NEARBYPOIS);
		}

		// persist currentPOI
		if (currentPOI != null) {
			final String jsonString = currentPOI.toJsonString();
			placesDataStore.setString(PlacesConstants.DataStoreKeys.CURRENT_POI, jsonString);
			Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "currentPOI persisted, %s", jsonString);
		} else {
			placesDataStore.remove(PlacesConstants.DataStoreKeys.CURRENT_POI);
		}

		// persist lastEnteredPOI
		if (lastEnteredPOI != null) {
			final String jsonString = lastEnteredPOI.toJsonString();
			placesDataStore.setString(PlacesConstants.DataStoreKeys.LAST_ENTERED_POI, jsonString);
			Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "lastEnteredPOI persisted, %s", jsonString);
		} else {
			placesDataStore.remove(PlacesConstants.DataStoreKeys.LAST_ENTERED_POI);
		}

		// persist lastExitedPOI
		if (lastExitedPOI != null) {
			final String jsonString = lastExitedPOI.toJsonString();
			placesDataStore.setString(PlacesConstants.DataStoreKeys.LAST_EXITED_POI, jsonString);
			Log.trace(PlacesConstants.LOG_TAG, "lastExitedPOI persisted, %s", jsonString);
		} else {
			placesDataStore.remove(PlacesConstants.DataStoreKeys.LAST_EXITED_POI);
		}

		placesDataStore.setLong(PlacesConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL, membershipValidUntil);
	}

	private PlacesPOI calculateCurrentPOI() {
		if (cachedPOIs == null) {
			return null;
		}

		PlacesPOI nextCurrentPOI = null;

		for (PlacesPOI poi : cachedPOIs.values()) {
			if (poi.containsUser()) {
				nextCurrentPOI = poi.comparePriority(nextCurrentPOI) ? poi : nextCurrentPOI;
			}
		}

		return nextCurrentPOI;
	}

	/**
	 * Clears membership data from the PlacesState instance.
	 * Membership data includes:
	 * <ol>
	 *     <li>Current POI</li>
	 *     <li>Last entered POI</li>
	 *     <li>Last exited POI</li>
	 * </ol>
	 */
	void clearMembershipData() {
		// clear out membership in memory
		currentPOI = null;
		lastEnteredPOI = null;
		lastExitedPOI = null;
		membershipValidUntil = 0;

		if (placesDataStore == null) {
			Log.warning(
				PlacesConstants.LOG_TAG,
				CLASS_NAME,
				"Unable to clear membership data, placesDataStore not available."
			);
			return;
		}

		// remove all the membership data from persistence
		placesDataStore.remove(PlacesConstants.DataStoreKeys.CURRENT_POI);
		placesDataStore.remove(PlacesConstants.DataStoreKeys.LAST_ENTERED_POI);
		placesDataStore.remove(PlacesConstants.DataStoreKeys.LAST_EXITED_POI);
		placesDataStore.remove(PlacesConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL);
	}

	private boolean isMembershipDataValid() {
		return TimeUtils.getUnixTimeInSeconds() < membershipValidUntil;
	}

	private void updateMembershipValidUntilTimestamp() {
		membershipValidUntil = TimeUtils.getUnixTimeInSeconds() + membershipTtl;
	}
}
