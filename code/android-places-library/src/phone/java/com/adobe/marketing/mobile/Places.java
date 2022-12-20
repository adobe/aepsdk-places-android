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
package com.adobe.marketing.mobile;

import android.location.Location;
import com.adobe.marketing.mobile.places.PlacesAuthorizationStatus;
import com.adobe.marketing.mobile.places.PlacesExtension;
import com.adobe.marketing.mobile.places.PlacesPOI;
import com.adobe.marketing.mobile.places.PlacesRequestError;
import com.adobe.marketing.mobile.places.PlacesUtil;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.DataReaderException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Places {

	public static final Class<? extends Extension> EXTENSION = PlacesExtension.class;

	private final static String EXTENSION_VERSION = "2.0.0";
	private static final String CLASS_NAME = "Places";
	private static final long EVENT_RESPONSE_TIMEOUT = 5000L;

	// Constants
	static final String LOG_TAG = "Places";

	private Places() { }

	// =======================================================================
	// Places Public API
	// =======================================================================

	/**
	 * Registers the Places extension with the {@code MobileCore}
	 * <p>
	 * This will allow the extension to send and receive events to and from the MobileCore.
	 */
	@Deprecated
	public static void registerExtension(){
		MobileCore.registerExtension(PlacesExtension.class, extensionError -> {
			if (extensionError == null) {
				return;
			}
			Log.error(LOG_TAG, CLASS_NAME, "There was an error when registering the Places extension: %s",
					extensionError.getErrorName());
		});
	}

	/**
	 * Returns the version of the {@link Places} Extension
	 *
	 * @return A {@link String} representing the extension version
	 */
	public static String extensionVersion() {
		return EXTENSION_VERSION;
	}

	/**
	 * Requests a list of nearby Points of Interest (POI) and returns them in a success callback.
	 *
	 * <p>
	 * Either successCallback or errorCallback is called depending upon the status of the get near by points of interest call.
	 * Look for {@link PlacesRequestError} in errorCallback to get more information about the reason for the failure.
	 *
	 *
	 * @param location a {@link Location} object represent the current location of the device
	 * @param limit a non-negative number representing the number of nearby POI to return from the request
	 * @param successCallback called on success, with list of {@link PlacesPOI} objects that represent the nearest POI to the device
	 * @param errorCallback called on failure, with {@link PlacesRequestError} representing the cause of failure
	 */
	public static void getNearbyPointsOfInterest(final Location location,
			final int limit,
			final AdobeCallback<List<PlacesPOI>> successCallback,
			final AdobeCallback<PlacesRequestError> errorCallback) {

		getNearbyPointsOfInterestInternal(location, limit, successCallback, errorCallback);
	}

	/**
	 * Pass a {@link GeofencingEvent} to be processed by the SDK.
	 * <p>
	 * Processes a place/geofence event in the sdk. Used for passive data collection (Analytics, Target, Audience Manager)
	 * and triggering actions using rules (In-app messaging, Postbacks, PII Request, Target profile updates, Audience Manager sync calls)
	 * <p>
	 * Calling this method will result in an Event being dispatched in the SDK, allowing for rules to be processed
	 * as a result of the triggering event.
	 *
	 * @param geofencingEvent the {@link GeofencingEvent} object that occurred while entering/exiting a GeoFence
	 */
	public static void processGeofenceEvent(final GeofencingEvent geofencingEvent) {

		if (geofencingEvent.hasError()) {
			Log.warning(LOG_TAG, CLASS_NAME,"Ignoring call to processGeofenceEvent. Provided GeofencingEvent has an error. ErrorCode: %d ", geofencingEvent.getErrorCode());
			return;
		}

		final String regionEventType = getRegionTransitionType(geofencingEvent.getGeofenceTransition());

		if (PlacesRegion.PLACE_EVENT_NONE.equals(regionEventType)) {
			Log.warning(LOG_TAG, CLASS_NAME, "Ignoring call to processGeofenceEvent. Transition type of GeofencingEvent is not recognized.");
			return;
		}

		final List<Map<String,Object>> regions = createRegionsEventData(geofencingEvent.getTriggeringGeofences(), regionEventType);

		if (regions == null) {
			Log.warning(LOG_TAG, CLASS_NAME,"Ignoring call to processGeofenceEvent. No valid Places region found for the provided GeofencingEvent.");
			return;
		}

		sendGeofenceEvents(regions);
	}

	/**
	 * Pass a {@link Geofence} and transition type to be processed by the SDK
	 *
	 * <p>
	 * Calling this method will result in an Event being dispatched in the SDK, allowing for rules to be processed
	 * as a result of the triggering event.
	 * <p>
	 * Use this API to pass only the required {@code Geofence} obtained from {@link GeofencingEvent#getTriggeringGeofences()}
	 * which the Places SDK needs to process.
	 * <p>
	 * Pass the transition type from {@link GeofencingEvent#getGeofenceTransition()}.
	 * Currently we support {@link Geofence#GEOFENCE_TRANSITION_ENTER} and {@link Geofence#GEOFENCE_TRANSITION_EXIT} transitions.
	 *
	 * @param geofence the {@code Geofence} object obtained from the {@link GeofencingEvent}
	 * @param transitionType an {@code int} representing the transition type for the passed geofence
	 */
	public static void processGeofence(final Geofence geofence, final int transitionType) {

		if (geofence == null) {
			Log.warning(LOG_TAG,CLASS_NAME, "Ignoring call to processGeofence. Geofence object is null.");
		}

		final String regionEventType = getRegionTransitionType(transitionType);
		if (PlacesRegion.PLACE_EVENT_NONE.equals(regionEventType)) {
			Log.warning(LOG_TAG,CLASS_NAME, "Ignoring call to processGeofence. TransitionType of the Geofence is not recognized.");
			return;
		}

		final ArrayList<Geofence> geofences = new ArrayList<>();
		geofences.add(geofence);

		final List<Map<String,Object>> regions = createRegionsEventData(geofences, regionEventType);

		if (regions == null) {
			Log.debug(LOG_TAG,CLASS_NAME, "Ignoring call to processGeofence. No valid Places region found for the provided Geofence");
			return;
		}

		sendGeofenceEvents(regions);
	}

	/**
	 * Returns all Points of Interest (POI) in which the device is currently known to be within.
	 *
	 * <p>
	 * Returns empty array when the device is not within any of the configured POIs.
	 *
	 * @param callback called with the list of {@link PlacesPOI} objects that represent the POIs within which the user is currently in
	 */
	public static void getCurrentPointsOfInterest(final AdobeCallback<List<PlacesPOI>> callback) {
		if (callback == null) {
			Log.debug(LOG_TAG,CLASS_NAME, "Ignoring call to getCurrentPointsOfInterest. Callback provided with getCurrentPointsOfInterest API is null.");
			return;
		}

		// Create Event Data
		final Map<String, Object> eventDataMap = new HashMap<>();
		eventDataMap.put(EventDataKeys.REQUEST_TYPE,
				EventDataKeys.REQUEST_TYPE_GET_USER_WITHIN_PLACES);

		final Event event = new Event.Builder(EventName.REQUEST_GETUSERWITHINPLACES, EventType.PLACES,
				EventSource.REQUEST_CONTENT)
				.setEventData(eventDataMap)
				.build();

		MobileCore.dispatchEventWithResponseCallback(event, EVENT_RESPONSE_TIMEOUT, new AdobeCallbackWithError<Event>() {
			final AdobeCallbackWithError userCallbackWithError = callback instanceof AdobeCallbackWithError ?
					(AdobeCallbackWithError) callback : null;
			@Override
			public void fail(final AdobeError adobeError) {
				executeCallbackWithError(adobeError);
			}

			@Override
			public void call(final Event event) {
				final Map<String, Object> responseEventData = event.getEventData();

				if (responseEventData == null) {
					callback.call(new ArrayList<>());
					return;
				}

				try {
					final List<Map> poisMap = DataReader.getTypedList(Map.class, responseEventData, EventDataKeys.USER_WITHIN_POIS);
					callback.call(PlacesUtil.convertMapToPOIList(poisMap));
				} catch (final DataReaderException e) {
					Log.warning(LOG_TAG, CLASS_NAME, String.format("Exception while reading POI from eventData. Returning empty POI list. Exception : %s", e.getLocalizedMessage()));
					callback.call(new ArrayList<>());
				}
			}

			private void executeCallbackWithError(final AdobeError adobeError) {
				if (userCallbackWithError != null) {
					userCallbackWithError.fail(adobeError);
				} else {
					callback.call(new ArrayList<>());
				}
			}
		});
	}

	/**
	 * Returns the last known Location of the device provided to the Places Extension.
	 *
	 * <p>
	 * Returns null, if the SDK doesn't know the last known location.
	 * Returns null, if the last known location has invalid latitude/longitude or any other error occurred while retrieving location.
	 *
	 * @param callback called with a {@link Location} object representing the last known lat/lon provided to the extension
	 */
	public static void getLastKnownLocation(final AdobeCallback<Location> callback) {

		if (callback == null) {
			Log.debug(LOG_TAG, CLASS_NAME, "Ignoring call to getLastKnownLocation. Callback is null.");
			return;
		}

		// Create Event Data
		final Map<String, Object> eventDataMap = new HashMap<>();
		eventDataMap.put(EventDataKeys.REQUEST_TYPE,
				EventDataKeys.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);

		final Event event = new Event.Builder(EventName.REQUEST_GETLASTKNOWNLOCATION, EventType.PLACES,
				EventSource.REQUEST_CONTENT)
				.setEventData(eventDataMap)
				.build();

		MobileCore.dispatchEventWithResponseCallback(event, EVENT_RESPONSE_TIMEOUT, new AdobeCallbackWithError<Event>() {
			final AdobeCallbackWithError userCallbackWithError = callback instanceof AdobeCallbackWithError ?
					(AdobeCallbackWithError) callback : null;
			@Override
			public void fail(final AdobeError adobeError) {
				executeCallbackWithError(adobeError);
			}

			@Override
			public void call(final Event event) {
				final Map<String, Object> eventDataMap = event.getEventData();

				if (eventDataMap == null || eventDataMap.isEmpty()) {
					Log.warning(LOG_TAG, CLASS_NAME, "Places response event have empty event data, returning null to getLastKnownLocation API call.");
					callback.call(null);
					return;
				}

				try {
					final double latitude = DataReader.getDouble(eventDataMap, EventDataKeys.LAST_KNOWN_LATITUDE);
					final double longitude = DataReader.getDouble(eventDataMap, EventDataKeys.LAST_KNOWN_LONGITUDE);
					if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
						Log.warning(LOG_TAG, CLASS_NAME, "Unable to read valid latitude and longitude from Places response event, returning null to getLastKnownLocation API call.");
						callback.call(null);
						return;
					}
					final Location lastKnownLocation = new Location("com.adobe.places.lastknownlocation");
					lastKnownLocation.setLatitude(latitude);
					lastKnownLocation.setLongitude(longitude);
					callback.call(lastKnownLocation);
				} catch (final DataReaderException exp) {
					Log.error(LOG_TAG, CLASS_NAME, "Unable to read latitude and longitude from Places response event");
					userCallbackWithError.fail(AdobeError.UNEXPECTED_ERROR);
				}
			}

			private void executeCallbackWithError(final AdobeError adobeError) {
				if (userCallbackWithError != null) {
					userCallbackWithError.fail(adobeError);
				} else {
					callback.call(null);
				}
			}
		});
	}

	/**
	 * Clears out the client-side data for Places in shared state, local storage, and in-memory.
	 */
	public static void clear() {
		// Create Event Data
		final Map<String, Object> eventDataMap = new HashMap<>();
		eventDataMap.put(EventDataKeys.REQUEST_TYPE,
				EventDataKeys.REQUEST_TYPE_RESET);

		// make an event
		final Event event = new Event.Builder(EventName.REQUEST_RESET, EventType.PLACES,
				EventSource.REQUEST_CONTENT)
				.setEventData(eventDataMap)
				.build();

		// dispatch the event
		MobileCore.dispatchEvent(event);
	}

	/**
	 * Sets the authorization status in the Places extension.
	 *
	 * The status provided is stored in the Places shared state, and is for reference only.
	 * Calling this method does not impact the actual location authorization status for this device.
	 *
	 * @param status the PlacesAuthorizationStatus to be set for this device
	 */
	public static void setAuthorizationStatus(final PlacesAuthorizationStatus status) {
		if (status == null) {
			Log.warning(LOG_TAG, CLASS_NAME,
					"Ignoring call to setAuthorizationStatus. PlacesAuthorizationStatus cannot be set to null. Provide a valid value.");
			return;
		}

		final Map<String, Object> eventDataMap = new HashMap<>();
		eventDataMap.put(EventDataKeys.REQUEST_TYPE,
				EventDataKeys.REQUEST_TYPE_SET_AUTHORIZATION_STATUS);
		eventDataMap.put(EventDataKeys.AUTH_STATUS, status.stringValue());

		final Event event = new Event.Builder(EventName.REQUEST_SETAUTHORIZATIONSTATUS,
				EventType.PLACES,
				EventSource.REQUEST_CONTENT)
				.setEventData(eventDataMap)
				.build();
		MobileCore.dispatchEvent(event);
	}

	/**
	 * Creates a list of event data representing region events for the given list of {@link Geofence} and regionEventType.
	 *
	 * @param geofences {@code List} of {@link Geofence} instances
	 * @param regionEventType eventType for all the geofences in the list
	 * @return a {@link List} of {@link Map} containing geofence event details
	 */
	private static List<Map<String,Object>> createRegionsEventData(final List<Geofence> geofences, final String regionEventType) {
		final List<Map<String,Object>> placesRegions = new ArrayList<>();

		if (geofences == null) {
			return placesRegions;
		}

		for (final Geofence geofence : geofences) {
			if (geofence == null) {
				continue;
			}

			final Map<String,Object> eventDataMap = new HashMap<>();
			eventDataMap.put(EventDataKeys.REGION_ID, geofence.getRequestId());
			eventDataMap.put(EventDataKeys.REGION_EVENT_TYPE, regionEventType);
			placesRegions.add(eventDataMap);
		}

		return placesRegions;
	}

	/**
	 * Dispatches {@link EventType#PLACES} {@link EventSource#REQUEST_CONTENT} event to
	 * process a geofence entry/exit.
	 *
	 * @param placesRegions a {@link List} of {@link Map} containing geofence id and geofence eventType
	 */
	private static void sendGeofenceEvents(final List<Map<String,Object>> placesRegions) {
		for (final Map<String,Object> placesRegion : placesRegions) {
			placesRegion.put(EventDataKeys.REQUEST_TYPE,
					EventDataKeys.REQUEST_TYPE_PROCESS_REGION_EVENT);
			final Event event = new Event.Builder(EventName.REQUEST_PROCESSREGIONEVENT, EventType.PLACES,
					EventSource.REQUEST_CONTENT)
					.setEventData(placesRegion)
					.build();
			MobileCore.dispatchEvent(event);
		}
	}

	/**
	 * Converts the geofenceTransitionEventType to PlacesEventType.
	 *
	 * @param geofenceTransitionType an {@code int} representing geofenceTransitionType
	 * @return an {@code int} representing PlacesEventType
	 */
	private static String getRegionTransitionType(final int geofenceTransitionType) {
		switch (geofenceTransitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return PlacesRegion.PLACE_EVENT_ENTRY;

			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return PlacesRegion.PLACE_EVENT_EXIT;

			default:
				return PlacesRegion.PLACE_EVENT_NONE;
		}
	}


	/**
	 * Dispatches {@link EventType#PLACES} {@link EventSource#REQUEST_CONTENT} event to request
	 * list of nearby points of interest for the provided search parameters.
	 *
	 * @param location		   {@link Location} current location of the device
	 * @param placesCount          {@code int} maximum number of nearby places to find
	 * @param successCallback      {@link AdobeCallback} success callback delivering an ordered list of nearby places
	 * @param errorCallback		   {@code AdobeCallback} error callback describing the errorStatus
	 */
	private static void getNearbyPointsOfInterestInternal(final Location location,
								   final int placesCount,
								   final AdobeCallback<List<PlacesPOI>> successCallback,
								   final AdobeCallback<PlacesRequestError> errorCallback) {

		// Create Event Data
		final Map<String,Object> eventDataMap = new HashMap<>();
		eventDataMap.put(EventDataKeys.LATITUDE, location.getLatitude());
		eventDataMap.put(EventDataKeys.LONGITUDE, location.getLongitude());
		eventDataMap.put(EventDataKeys.PLACES_COUNT, placesCount);
		eventDataMap.put(EventDataKeys.REQUEST_TYPE,
				EventDataKeys.REQUEST_TYPE_GET_NEARBY_PLACES);

		final Event event = new Event.Builder(EventName.REQUEST_GETNEARBYPLACES, EventType.PLACES,
				EventSource.REQUEST_CONTENT)
				.setEventData(eventDataMap)
				.build();


		MobileCore.dispatchEventWithResponseCallback(event, EVENT_RESPONSE_TIMEOUT, new AdobeCallbackWithError<Event>() {
			@Override
			public void fail(final AdobeError adobeError) {
				Log.debug(LOG_TAG, CLASS_NAME, "Error occurred while retrieving nearbyPOIs, Adobe Error: %s.", adobeError.getErrorName());
				callErrorCallback(PlacesRequestError.UNKNOWN_ERROR);
			}

			@Override
			public void call(final Event event) {
				final Map<String,Object> responseEventData = event.getEventData();

				if (responseEventData == null) {
					callErrorCallback(PlacesRequestError.UNKNOWN_ERROR);
					return;
				}

				try {
					// retrieve the status and the list of POI's from eventData
					final List<Map> poiMap = DataReader.getTypedList(Map.class, responseEventData, EventDataKeys.NEAR_BY_PLACES_LIST);
					final int resultStatusInteger = DataReader.getInt(responseEventData,EventDataKeys.RESULT_STATUS);
					final PlacesRequestError status = PlacesRequestError.fromInt(resultStatusInteger);

					// call the successCallback if the status is OK
					if (status == PlacesRequestError.OK) {
						callSuccessCallback(PlacesUtil.convertMapToPOIList(poiMap));
						return;
					}

					// else call the errorCallback with the obtained status
					callErrorCallback(status);


				} catch (final DataReaderException exp) {
					callErrorCallback(PlacesRequestError.UNKNOWN_ERROR);
				}
			}

			private void callErrorCallback(final PlacesRequestError requestError) {
				Log.debug(LOG_TAG, CLASS_NAME, "Error occurred while retrieving nearbyPOIs, Error code: %s.", requestError);

				if (errorCallback != null) {
					errorCallback.call(requestError);
				}
			}

			private void callSuccessCallback(final List<PlacesPOI> pois) {
				if (successCallback != null) {
					successCallback.call(pois);
				}
			}
		});
	}

	private static final class EventDataKeys {
		static final String STATE_OWNER = "stateowner";


		private EventDataKeys() {
		}

			private static final String MODULE_NAME = "com.adobe.module.places";

			// Places Request Content event keys
			private static final String PLACES_COUNT = "count";
			private static final String LATITUDE = "latitude";
			private static final String LONGITUDE = "longitude";

			// Places Response Content event keys
			private	static final String NEAR_BY_PLACES_LIST = "nearbypois";
			private static final String RESULT_STATUS = "status";
			static final String USER_WITHIN_POIS = "userwithinpois";
			static final String TRIGGERING_REGION = "triggeringregion";

			// request types
			static final String REQUEST_TYPE = "requesttype";
			static final String REQUEST_TYPE_GET_NEARBY_PLACES = "requestgetnearbyplaces";
			static final String REQUEST_TYPE_PROCESS_REGION_EVENT = "requestprocessregionevent";
			static final String REQUEST_TYPE_GET_USER_WITHIN_PLACES = "requestgetuserwithinplaces";
			static final String REQUEST_TYPE_GET_LAST_KNOWN_LOCATION = "requestgetlastknownlocation";
			static final String REQUEST_TYPE_RESET = "requestreset";
			static final String REQUEST_TYPE_SET_AUTHORIZATION_STATUS = "requestsetauthorizationstatus";

			// Region Keys
			static final String REGION_NAME = "regionname";
			static final String REGION_ID = "regionid";
			static final String REGION_EVENT_TYPE = "regioneventtype";
			static final String REGION_METADATA = "regionmetadata";
			static final String REGION_TIMESTAMP = "timestamp";

			// last known location keys
			static final String LAST_KNOWN_LATITUDE = "lastknownlatitude";
			static final String LAST_KNOWN_LONGITUDE = "lastknownlongitude";

			// places authorization status
			static final String AUTH_STATUS = "authstatus";
		}

	private static final class EventName {
		// places request content event names
		static final String REQUEST_GETUSERWITHINPLACES = "requestgetuserwithinplaces";
		static final String REQUEST_GETLASTKNOWNLOCATION = "requestgetlastknownlocation";
		static final String REQUEST_GETNEARBYPLACES = "requestgetnearbyplaces";
		static final String REQUEST_PROCESSREGIONEVENT = "requestprocessregionevent";
		static final String REQUEST_RESET = "requestreset";
		static final String REQUEST_SETAUTHORIZATIONSTATUS = "requestsetauthorizationstatus";

		private EventName() {
		}
	}

	private static final class PlacesRegion {
		static final String PLACE_EVENT_NONE  = "none";
		static final String PLACE_EVENT_ENTRY = "entry";
		static final String PLACE_EVENT_EXIT  = "exit";

		private PlacesRegion() {

		}
	}

}

