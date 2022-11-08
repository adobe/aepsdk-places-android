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
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.Places;
import com.adobe.marketing.mobile.SharedStateResolution;
import com.adobe.marketing.mobile.SharedStateStatus;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The Places module allows customers to take actions based on the location of their users. The Places module will act
 * as the interface to the Places Query Service APIs (APIs). By listening for events containing GPS coordinates and
 * geofence region events, the Places module will dispatch new events for processing by the Rules Engine. The Places
 * module will also be responsible for retrieving and delivering a list of the nearest POIs for the app - data it will
 * retrieve from the APIs. The regions returned by the APIs will be stored in cache and persistence, which will enable
 * limited offline processing.
 *
 * The {@code PlacesExtension} module listens for the following {@link Event}s:
 * <ol>
 *     <li>{@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT}</li>
 *     <li>{@link EventType#CONFIGURATION} - {@link EventSource#RESPONSE_CONTENT}</li>
 * </ol>
 *
 * The {@code PlacesExtension} module dispatches the following {@code Events}:
 * <ol>
 *	   <li>{@link EventType#PLACES} - {@link EventSource#RESPONSE_CONTENT}</li>
 * </ol>
 *
 * The {@code PlacesExtension} module has dependencies on the following  {@link PlatformServices}:
 * <ol>
 *     <li>List all the service that's used by places module</li>
 * </ol>
 */
public class PlacesExtension extends Extension {

	private static final String CLASS_NAME = "PlacesExtension";

	ConcurrentLinkedQueue<Event> eventsQueue;
	// TODO : to be removed in next PR. Now there is not need to save configuration
	Map<String,Object> lastKnownConfigurationState;
	PlacesDispatcher placesDispatcher;
	PlacesQueryService queryService;
	PlacesState state;


	protected PlacesExtension(ExtensionApi extensionApi) {
		super(extensionApi);
	}

	@Override
	protected String getVersion() {
		return Places.extensionVersion();
	}

	@NonNull
	@Override
	protected String getName() {
		return PlacesConstants.MODULE_NAME;
	}

	@Override
	protected String getFriendlyName() {
		return PlacesConstants.FRIENDLY_NAME;
	}

	@Override
	protected void onRegistered() {
		getApi().registerEventListener(
				EventType.CONFIGURATION,
				EventSource.RESPONSE_CONTENT,
				this::handleConfigurationResponseEvent
		);
		getApi().registerEventListener(
				EventType.PLACES,
				EventSource.REQUEST_CONTENT,
				this::handlePlacesRequestEvent
		);

		// share places state if we got one
		final Map<String,Object> placesSharedState = state.getPlacesSharedState();
		if (placesSharedState != null && !placesSharedState.isEmpty()) {
			getApi().createSharedState(placesSharedState, null);
		}

		// TO BE REMOVED IN NEXT PR
		this.eventsQueue = new ConcurrentLinkedQueue<Event>();
		state = new PlacesState();
		queryService = new PlacesQueryService();
	}

	@Override
	public boolean readyForEvent(@NonNull Event event) {
		return getApi().getSharedState(PlacesConstants.EventDataKeys.Configuration.EXTENSION_NAME,
				event, false, SharedStateResolution.ANY).getStatus() == SharedStateStatus.SET;
	}

	private void handlePlacesRequestEvent(final Event event) {
	// TODO: to be done in next PR
	}

	private void handleConfigurationResponseEvent(final Event event) {
	// TODO: to be done in next PR
	}


	/**
	 * This method queues the incoming event in {@link #eventsQueue} and processes them in order of occurrence.
	 * Only the {@link Event}'s that needs on Configuration shared state to be resolved before processing should be queued.
	 *
	 * @param event the {@code Event} the needs to be queued
	 */
	// TODO: to be removed in next PR
	void queueEvent(final Event event) {
		if (event == null) {
			return;
		}

		// queue only non null event
		eventsQueue.add(event);
		processEventQueue();
	}

	/**
	 * Processes stored events in the queue as long there's a corresponding
	 * configuration state available.
	 */
	void processEventQueue() {
		while (!eventsQueue.isEmpty()) {
			final Event currentEvent = eventsQueue.peek();
			boolean isEventProcessed = processPlacesRequestEvent(currentEvent);

			if (isEventProcessed) {
				eventsQueue.poll();
			} else {
				break;
			}
		}
	}

	/**
	 * Processes the places request content events.
	 * Returns false, if the configuration for the event is pending.
	 * Returns true, if the event is processed with the valid configuration.
	 *
	 * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event
	 * @return {@code Boolean} representing the event processed status.
	 */
	boolean processPlacesRequestEvent(final Event event) {
		// quick return if the eventData is empty/null
		final Map<String,Object> eventData = event.getEventData();

		if (eventData == null || eventData.isEmpty()) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "processPlacesRequestEvent - Places request content event's eventData is empty, Ignoring event");
			return true;
		}

		// Retrieve the latest configuration shared state data
		final Map<String,Object> configData = retrieveConfigurationEventState(event);

		final String requestType = DataReader.optString(eventData, PlacesConstants.EventDataKeys.Places.REQUEST_TYPE, null);

		if (!StringUtils.isNullOrEmpty(requestType)) {
			if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES.equals(requestType)) {
				handleGetNearByPlaceEvent(event, configData);
				return true;
			}

			if (PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT.equals(requestType)) {
				handleGeofenceEvent(event, configData);
				return true;
			}
		}

		return true;
	}

	/**
	 * Public method to handle changes in the configuration.
	 * <p>
	 * Reads the privacy value and acts accordingly.
	 * On privacy opt-out, Stops all the location processing and clears the location and poi data in the sharedState.
	 *
	 * @param event the {@link EventType#CONFIGURATION} - {@link EventSource#RESPONSE_CONTENT} event associated with the configuration change
	 */
	void handleConfigurationEvent(final Event event) {
		if (event != null) {
			retrieveConfigurationEventState(event);

			// clear the queued up events if the privacy is opted-out
			if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
				state.clearData();
				eventsQueue.clear();
				getApi().createSharedState(null, event);
				return;
			}

			// Else try processing the pending event in queue on configuration change
			processEventQueue();
		}
	}


	/**
	 * Handler for GetUserWithinPOI public api.
	 *
	 * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event
	 */
	void handleGetUserWithinPOIsEvent(final Event event) {
		Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "Handling get user-within points of interest event.");

		// dispatch userWithinPOIS for the awaiting one time listener
		placesDispatcher.dispatchUserWithinPOIs(state.getUserWithInPOIs(), event);
		// dispatch userWithinPOIS for all other listeners
		placesDispatcher.dispatchUserWithinPOIs(state.getUserWithInPOIs(), null);
	}

	void handleGetLastKnownLocation(final Event event) {
		Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "Handling get last known location event");
		final Location location = state.loadLastKnownLocation();

		if (location == null) {
			placesDispatcher.dispatchLastKnownLocation(PlacesConstants.INVALID_LAT_LON,
					PlacesConstants.INVALID_LAT_LON, event);
			return;
		}

		// dispatch lastKnownLocation for all other listeners
		placesDispatcher.dispatchLastKnownLocation(location.getLatitude(), location.getLongitude(),
				event);
		// dispatch lastKnownLocation for all other listeners
		placesDispatcher.dispatchLastKnownLocation(location.getLatitude(), location.getLongitude(),
				null);
	}

	void saveLastKnownLocation(final Event event) {

		// Don't save the location if privacy is opted-out, Bail out right away
		retrieveConfigurationEventState(event);

		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME,"saveLastKnownLocation - Ignoring to save the last known location, Privacy opted out");
			return;
		}

		final Map<String,Object> eventData = event.getEventData();

		if (eventData == null || eventData.isEmpty()) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "saveLastKnownLocation - Unable to save location, invalid eventData");
			return;
		}

		final double latitude = DataReader.optDouble(eventData, PlacesConstants.EventDataKeys.Places.LATITUDE, PlacesConstants.INVALID_LAT_LON);
		final double longitude = DataReader.optDouble(eventData, PlacesConstants.EventDataKeys.Places.LONGITUDE, PlacesConstants.INVALID_LAT_LON);

		if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "saveLastKnownLocation - Unable to save location, invalid latitude/longitude");
			return;
		}

		state.saveLastKnownLocation(latitude, longitude);
	}


	/**
	 * Method to handle the authorization status change event
	 *
	 * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event
	 */
	void handleSetAuthorizationStatusEvent(final Event event) {
		final Map<String,Object> data = event.getEventData();
		final String authStatusString = DataReader.optString(data, PlacesConstants.EventDataKeys.Places.AUTH_STATUS, null);

		if (!PlacesAuthorizationStatus.isValidStatus(authStatusString)) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME,
					  "handleSetAuthorizationStatusEvent - Invalid Authorization status value is set to Places Extension. Please check PlacesAuthorizationStatus class.");
			return;
		}

		state.setAuthorizationStatus(authStatusString);

		// update the places shared state
		getApi().createSharedState(state.getPlacesSharedState(), event);
	}


	/**
	 * Resets the shared state, all the in-memory and persisted places state variables.
	 */
	void reset() {
		state.clearData();
		getApi().createSharedState(null, null);
	}


	/**
	 * Public method to handle getNearByPlaces event.
	 * <p>
	 * This method handles the Places request content event generated when the getNearByPlaces API is called.
	 * On valid configuration this method connects with the Place query services, fetches and dispatches the n near by poi's.
	 *
	 * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event
	 */
	private void handleGetNearByPlaceEvent(final Event event, final Map<String,Object> configData) {
		Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "Handling get near by place event.");
		// Retrieve the latest configuration shared state data
		final PlacesConfiguration placesConfig = new PlacesConfiguration(configData);

		if (!placesConfig.isValid()) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetNearByPlaceEvent - Ignoring the get nearby places event, Invalid Configuration");
			placesDispatcher.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(),
					PlacesRequestError.CONFIGURATION_ERROR, event);
			return;
		}

		// Bail out if privacy is opted out.
		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetNearByPlaceEvent - Ignoring the get nearby places event, Privacy opted out.");
			placesDispatcher.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(),
					PlacesRequestError.PRIVACY_OPTED_OUT,
					event);
			return;
		}


		queryService.getNearbyPlaces(event.getEventData(), placesConfig, new PlacesQueryResponseCallback() {
			@Override
			public void call(PlacesQueryResponse response) {

				if (!response.isSuccess) {
					Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, response.errorMessage);
					placesDispatcher.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(), response.resultStatus,
							event);
					return;
				}

				// read and set the latest membership ttl value from configuration
				state.setMembershiptTtl(placesConfig.getMembershipTtl());

				// on success, process the response - cache POIs, persist POIs and update the shared state values
				state.processNetworkResponse(response, event);

				// update the places shared state
				getApi().createSharedState(state.getPlacesSharedState(), event);

				// dispatch nearbyPOI list to the awaiting one-time listener
				placesDispatcher.dispatchNearbyPlaces(response.getAllPOIs(), PlacesRequestError.OK,
						event);
				// dispatch nearbyPOI list for other listeners
				placesDispatcher.dispatchNearbyPlaces(response.getAllPOIs(), PlacesRequestError.OK, null);

			}
		});

	}

	private void handleGeofenceEvent(final Event event, final Map<String,Object> configData) {
		final PlacesConfiguration placesConfig = new PlacesConfiguration(configData);

		// Bail out if privacy is opted out.
		retrieveConfigurationEventState(event);

		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGeofenceEvent - Ignoring the geofence event, Privacy opted out.");
			return;
		}

		// read and set the latest membership ttl value from configuration
		state.setMembershiptTtl(placesConfig.getMembershipTtl());

		// process the region event
		final PlacesRegion regionEvent = state.processRegionEvent(event);

		// update the places shared state
		getApi().createSharedState(state.getPlacesSharedState(), event);

		// dispatch the processed region event
		placesDispatcher.dispatchRegionEvent(regionEvent);

	}


	/**
	 * Retrieves the configuration corresponding to the provided {@code Event}.
	 * <p>
	 * If the configuration shared state is pending, attempts to read the last known configuration shared state from cache.
	 * If the configuration shared state is pending and the last known configuration shared state is null, tries to read the oldest configuration shared state.
	 * If the configuration shared state is valid, retrieves the valid shared state and updates the last known configuration shared cache variable.
	 *
	 * @param event the {@link EventType#CONFIGURATION} - {@link EventSource#RESPONSE_CONTENT} event associated with the configuration change
	 * @return {@code Map<String,Object>} containing configuration shared state
	 */
	// TODO : To be removed in next PR
	private Map<String,Object> retrieveConfigurationEventState(final Event event) {
		final Map<String,Object> configData = this.getSharedEventState(PlacesConstants.EventDataKeys.Configuration.EXTENSION_NAME, event);

		if (configData == EventHub.SHARED_STATE_PENDING) {
			if (this.lastKnownConfigurationState == null) {
				configData = this.getSharedEventState(PlacesConstants.EventDataKeys.Configuration.EXTENSION_NAME,
													  Event.SHARED_STATE_OLDEST);
				this.lastKnownConfigurationState = configData;
			} else {
				configData = this.lastKnownConfigurationState;
			}
		} else {
			this.lastKnownConfigurationState = configData;
		}

		return configData;
	}

	/**
	 * Reads the privacy status from the current configuration.
	 * <p>
	 * {@link MobilePrivacyStatus#UNKNOWN} is returned when
	 * <ol>
	 * 		<li>No Valid Configuration shared state is available.</li>
	 * 		<li>No privacy configuration key is found in configuration shared state.</li>
	 * </ol>
	 * @return the privacy status.
	 */
	private MobilePrivacyStatus getMobilePrivacyStatus() {
		if (this.lastKnownConfigurationState != null
				&& this.lastKnownConfigurationState.containsKey(
					PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY)) {
			final String privacyString = this.lastKnownConfigurationState.optString(
									   PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY, "unknown");
			return MobilePrivacyStatus.fromString(privacyString);
		} else {
			return MobilePrivacyStatus.UNKNOWN;
		}
	}

}
