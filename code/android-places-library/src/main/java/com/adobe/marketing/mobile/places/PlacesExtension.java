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

import java.util.ArrayList;
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
class PlacesExtension extends InternalModule {

	ConcurrentLinkedQueue<Event> eventsQueue;
	EventData lastKnownConfigurationState;
	PlacesDispatcherPlacesResponseContent placesDispatcherPlacesResponseContent;
	PlacesQueryService queryService;
	PlacesState state;



	/**
	 * Constructor for {@code PlacesExtension} module.
	 * <p>
	 * Constructs a new instance of a {@code PlacesExtension} module, registers required listeners and dispatchers.
	 * with the associated {@code EventHub} instance.
	 *
	 * @param hub the {@link EventHub} instance where this module is registered
	 * @param services the {@link PlatformServices} used by this module
	 */
	PlacesExtension(final EventHub hub, final PlatformServices services) {
		super(PlacesConstants.EventDataKeys.Places.MODULE_NAME, hub, services);
		this.registerListener(EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT,
							  PlacesListenerConfigurationResponseContent.class);
		this.registerListener(EventType.PLACES, EventSource.REQUEST_CONTENT, PlacesListenerPlacesRequestContent.class);
		this.placesDispatcherPlacesResponseContent = this.createDispatcher(
					PlacesDispatcherPlacesResponseContent.class);
		this.eventsQueue = new ConcurrentLinkedQueue<Event>();
		state = new PlacesState(getPlatformServices());

		// share places state if we got one
		EventData placesSharedState = state.getPlacesSharedState();

		if (placesSharedState != null && !placesSharedState.isEmpty()) {
			createOrUpdateSharedState(0, placesSharedState);
		}
	}

	/**
	 * This method queues the incoming event in {@link #eventsQueue} and processes them in order of occurrence.
	 * Only the {@link Event}'s that needs on Configuration shared state to be resolved before processing should be queued.
	 *
	 * @param event the {@code Event} the needs to be queued
	 */
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
		EventData eventData = event.getData();

		if (eventData == null || eventData.isEmpty()) {
			Log.debug(PlacesConstants.LOG_TAG, "Places request content event's eventData is empty, Ignoring event");
			return true;
		}


		// Retrieve the latest configuration shared state data
		EventData configData = retrieveConfigurationEventState(event);

		if (configData == EventHub.SHARED_STATE_PENDING) {
			return false;
		}


		String requestType = eventData.optString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE, null);

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
				clearSharedStates();
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
		Log.debug(PlacesConstants.LOG_TAG, "Handling get user-within points of interest event. For more details refer to " +
				  PlacesConstants.DocsLinks.GET_CURRENT_POI_EVENT);

		// dispatch userWithinPOIS for the awaiting one time listener
		placesDispatcherPlacesResponseContent.dispatchUserWithinPOIs(state.getUserWithInPOIs(), event.getResponsePairID());
		// dispatch userWithinPOIS for all other listeners
		placesDispatcherPlacesResponseContent.dispatchUserWithinPOIs(state.getUserWithInPOIs(), null);
	}

	void handleGetLastKnownLocation(final Event event) {
		Log.debug(PlacesConstants.LOG_TAG, "Handling get last known location event");
		PlacesGpsLocation gpsLocation = state.loadLastKnownLocation();

		if (gpsLocation == null) {
			placesDispatcherPlacesResponseContent.dispatchLastKnownLocation(PlacesConstants.INVALID_LAT_LON,
					PlacesConstants.INVALID_LAT_LON, event.getResponsePairID());
			return;
		}

		// dispatch lastKnownLocation for all other listeners
		placesDispatcherPlacesResponseContent.dispatchLastKnownLocation(gpsLocation.getLatitude(), gpsLocation.getLongitude(),
				event.getResponsePairID());
		// dispatch lastKnownLocation for all other listeners
		placesDispatcherPlacesResponseContent.dispatchLastKnownLocation(gpsLocation.getLatitude(), gpsLocation.getLongitude(),
				null);
	}

	void saveLastKnownLocation(final Event event) {

		// Don't save the location if privacy is opted-out, Bail out right away
		retrieveConfigurationEventState(event);

		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(PlacesConstants.LOG_TAG,
					  "Ignoring to save the last known location, Privacy opted out. For more details refer to " +
					  PlacesConstants.DocsLinks.PRIVACY_STATUS);
			return;
		}

		EventData eventData = event.getData();

		if (eventData == null || eventData.isEmpty()) {
			Log.debug(PlacesConstants.LOG_TAG, "Unable to save location, invalid eventData");
			return;
		}

		double latitude = eventData.optDouble(PlacesConstants.EventDataKeys.Places.LATITUDE, PlacesConstants.INVALID_LAT_LON);
		double longitude = eventData.optDouble(PlacesConstants.EventDataKeys.Places.LONGITUDE, PlacesConstants.INVALID_LAT_LON);

		if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
			Log.debug(PlacesConstants.LOG_TAG, "Unable to save location, invalid latitude/longitude");
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
		EventData data = event.getData();
		String authStatusString = data.optString(PlacesConstants.EventDataKeys.Places.AUTH_STATUS, null);

		if (!PlacesAuthorizationStatus.isValidStatus(authStatusString)) {
			Log.debug(PlacesConstants.LOG_TAG,
					  "Invalid Authorization status value is set to Places Extension. Please check PlacesAuthorizationStatus class. For more details refer to "
					  + PlacesConstants.DocsLinks.AUTHORIZATION_STATUS);
			return;
		}

		state.setAuthorizationStatus(authStatusString);

		// update the places shared state
		createOrUpdateSharedState(event.getEventNumber(), state.getPlacesSharedState());

	}


	/**
	 * Resets the shared state, all the in-memory and persisted places state variables.
	 */
	void reset() {
		state.clearData();
		clearSharedStates();
	}


	/**
	 * Public method to handle getNearByPlaces event.
	 * <p>
	 * This method handles the Places request content event generated when the getNearByPlaces API is called.
	 * On valid configuration this method connects with the Place query services, fetches and dispatches the n near by poi's.
	 *
	 * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event
	 */
	private void handleGetNearByPlaceEvent(final Event event, final EventData configData) {
		Log.debug(PlacesConstants.LOG_TAG, "Handling get near by place event. For more details refer to " +
				  PlacesConstants.DocsLinks.GET_NEARBY_POI_EVENT);
		// Retrieve the latest configuration shared state data
		PlacesConfiguration placesConfig = new PlacesConfiguration(configData, getPlatformServices());

		if (!placesConfig.isValid()) {
			Log.debug(PlacesConstants.LOG_TAG, "Ignoring the get nearby places event, Invalid Configuration");
			placesDispatcherPlacesResponseContent.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(),
					PlacesRequestError.CONFIGURATION_ERROR, event.getResponsePairID());
			return;
		}

		// Bail out if privacy is opted out.
		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(PlacesConstants.LOG_TAG, "Ignoring the get nearby places event, Privacy opted out. For more details refer to "
					  + PlacesConstants.DocsLinks.PRIVACY_STATUS);
			placesDispatcherPlacesResponseContent.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(),
					PlacesRequestError.PRIVACY_OPTED_OUT,
					event.getResponsePairID());
			return;
		}

		queryService = getPlacesQueryService();

		if (queryService == null) {
			Log.warning(PlacesConstants.LOG_TAG,
						"Ignoring the get nearby places event, platform services unavailable, couldn't initialize the query service.");
			placesDispatcherPlacesResponseContent.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(),
					PlacesRequestError.QUERY_SERVICE_UNAVAILABLE,
					event.getResponsePairID());
			return ;
		}

		PlacesQueryResponse response = queryService.getNearbyPlaces(event.getData(), placesConfig);

		if (!response.isSuccess) {
			Log.debug(PlacesConstants.LOG_TAG, response.errorMessage);
			placesDispatcherPlacesResponseContent.dispatchNearbyPlaces(new ArrayList<PlacesPOI>(), response.resultStatus,
					event.getResponsePairID());
			return;
		}

		// read and set the latest membership ttl value from configuration
		state.setMembershiptTtl(placesConfig.getMembershipTtl());

		// on success, process the response - cache POIs, persist POIs and update the shared state values
		state.processNetworkResponse(response, event);

		// update the places shared state
		createOrUpdateSharedState(event.getEventNumber(), state.getPlacesSharedState());

		// dispatch nearbyPOI list to the awaiting one-time listener
		placesDispatcherPlacesResponseContent.dispatchNearbyPlaces(response.getAllPOIs(), PlacesRequestError.OK,
				event.getResponsePairID());
		// dispatch nearbyPOI list for other listeners
		placesDispatcherPlacesResponseContent.dispatchNearbyPlaces(response.getAllPOIs(), PlacesRequestError.OK, null);

	}



	private void handleGeofenceEvent(final Event event, final EventData configData) {
		PlacesConfiguration placesConfig = new PlacesConfiguration(configData, getPlatformServices());

		// Bail out if privacy is opted out.
		retrieveConfigurationEventState(event);

		if (getMobilePrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
			Log.debug(PlacesConstants.LOG_TAG, "Ignoring the geofence event, Privacy opted out. For more details refer to " +
					  PlacesConstants.DocsLinks.PRIVACY_STATUS);
			return;
		}

		// read and set the latest membership ttl value from configuration
		state.setMembershiptTtl(placesConfig.getMembershipTtl());

		// process the region event
		PlacesRegion regionEvent = state.processRegionEvent(event);

		// update the places shared state
		createOrUpdateSharedState(event.getEventNumber(), state.getPlacesSharedState());

		// dispatch the processed region event
		placesDispatcherPlacesResponseContent.dispatchRegionEvent(regionEvent);

	}


	/**
	 * Retrieves the configuration corresponding to the provided {@code Event}.
	 * <p>
	 * If the configuration shared state is pending, attempts to read the last known configuration shared state from cache.
	 * If the configuration shared state is pending and the last known configuration shared state is null, tries to read the oldest configuration shared state.
	 * If the configuration shared state is valid, retrieves the valid shared state and updates the last known configuration shared cache variable.
	 *
	 * @param event the {@link EventType#CONFIGURATION} - {@link EventSource#RESPONSE_CONTENT} event associated with the configuration change
	 * @return {@code EventData} containing configuration shared state
	 */
	private EventData retrieveConfigurationEventState(final Event event) {
		EventData configData = this.getSharedEventState(PlacesConstants.EventDataKeys.Configuration.EXTENSION_NAME, event);

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
			String privacyString = this.lastKnownConfigurationState.optString(
									   PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY, "unknown");
			return MobilePrivacyStatus.fromString(privacyString);
		} else {
			return MobilePrivacyStatus.UNKNOWN;
		}
	}


	/**
	 * Attempts to instantiate the instance of {@code PlacesQueryService}.
	 * <p>
	 * No operation is performed if the {@link #queryService} variable is already instantiated.
	 * This method returns null when, unable to instantiate {@code PlacesQueryService} instance because of missing platform service.
	 *
	 * @return {@link PlacesQueryService} an instance of PlacesQueryService
	 */
	PlacesQueryService getPlacesQueryService() {
		if (queryService == null) {
			PlatformServices platformServices = getPlatformServices();

			if (platformServices == null) {
				return null;
			}

			try {
				queryService = new PlacesQueryService(platformServices.getJsonUtilityService(), platformServices.getNetworkService());
			} catch (MissingPlatformServicesException e) {
				return null;
			}
		}

		return queryService;
	}
}
