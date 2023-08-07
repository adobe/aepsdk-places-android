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
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Places module allows customers to take actions based on the location of their users. The Places module will act
 * as the interface to the Places Query Service APIs (APIs). By listening for events containing GPS coordinates and
 * geofence region events, the Places module will dispatch new events for processing by the Rules Engine. The Places
 * module will also be responsible for retrieving and delivering a list of the nearest POIs for the app - data it will
 * retrieve from the APIs. The regions returned by the APIs will be stored in cache and persistence, which will enable
 * limited offline processing.
 * <p>
 * The {@code PlacesExtension} module listens for the following {@link Event}s:
 * <ol>
 *     <li>{@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT}</li>
 *     <li>{@link EventType#CONFIGURATION} - {@link EventSource#RESPONSE_CONTENT}</li>
 * </ol>
 * <p>
 * The {@code PlacesExtension} module dispatches the following {@code Events}:
 * <ol>
 * 	   <li>{@link EventType#PLACES} - {@link EventSource#RESPONSE_CONTENT}</li>
 * </ol>
 */
public class PlacesExtension extends Extension {

    private static final String CLASS_NAME = "PlacesExtension";

    ExtensionApi extensionApi;
    PlacesDispatcher placesDispatcher;
    PlacesQueryService queryService;
    PlacesState state;

    protected PlacesExtension(final ExtensionApi extensionApi) {
        super(extensionApi);
        this.extensionApi = extensionApi;
        state = new PlacesState(ServiceProvider.getInstance().getDataStoreService());
        queryService = new PlacesQueryService(ServiceProvider.getInstance().getNetworkService());
        placesDispatcher = new PlacesDispatcher(extensionApi);
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
        extensionApi.registerEventListener(
                EventType.CONFIGURATION,
                EventSource.RESPONSE_CONTENT,
                this::handleConfigurationResponseEvent
        );
        extensionApi.registerEventListener(
                EventType.PLACES,
                EventSource.REQUEST_CONTENT,
                this::handlePlacesRequestEvent
        );

        // share places state if we got one
        final Map<String, Object> placesSharedState = state.getPlacesSharedState();
        if (placesSharedState != null && !placesSharedState.isEmpty()) {
            extensionApi.createSharedState(placesSharedState, null);
        }
    }

    @Override
    public boolean readyForEvent(final @NonNull Event event) {
        if (extensionApi.getSharedState(PlacesConstants.EventDataKeys.Configuration.EXTENSION_NAME,
                event, false, SharedStateResolution.ANY).getStatus() == SharedStateStatus.SET) {
            return true;
        }
        Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "readyForEvent - Waiting for configuration to process places events.");
        return false;
    }

    void handlePlacesRequestEvent(@NonNull final Event event) {
        final Map<String, Object> eventData = event.getEventData();

        if (eventData == null || eventData.isEmpty()) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handlePlacesRequestEvent - Ignoring Places Request event, eventData is empty.");
            return;
        }

        final String requestType = DataReader.optString(eventData, PlacesConstants.EventDataKeys.Places.REQUEST_TYPE, null);
        if (StringUtils.isNullOrEmpty(requestType)) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handlePlacesRequestEvent - Ignoring Places Request event due to missing request type");
            return;
        }

        switch (requestType) {
            case PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES:
                handleGetUserWithinPOIsEvent(event);
                break;
            case PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION:
                handleGetLastKnownLocation(event);
                break;
            case PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_RESET:
                reset();
                break;
            case PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES: {
                final Map<String, Object> configData = retrieveConfigurationEventState(event);
                saveLastKnownLocation(event);
                handleGetNearByPlaceEvent(event, configData);
                break;
            }
            case PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT: {
                final Map<String, Object> configData = retrieveConfigurationEventState(event);
                handleGeofenceEvent(event, configData);
                break;
            }
            case PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS:
                handleSetAuthorizationStatusEvent(event);
                break;
            default:
                Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, String.format("handlePlacesRequestEvent - Ignoring Places Request event due to a unrecognized request type - %s.", requestType));
                break;
        }
    }

    /**
     * Handle changes in the configuration.
     * <p>
     * Reads the privacy value and acts accordingly.
     * On privacy opt-out, Stops all the location processing and clears the location and poi data in the sharedState.
     *
     * @param event the {@link EventType#CONFIGURATION} - {@link EventSource#RESPONSE_CONTENT} event associated with the configuration change
     */
    void handleConfigurationResponseEvent(@NonNull final Event event) {
        final Map<String, Object> configData = retrieveConfigurationEventState(event);

        // clear the queued up events if the privacy is opted-out
        if (getMobilePrivacyStatus(configData) == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleConfigurationResponseEvent - Stopping Places processing due to privacy opt-out.");
            extensionApi.stopEvents();
            reset();
        }
    }


    /**
     * Handler for getUserWithinPOI public api.
     *
     * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event requesting the current POI
     */
    private void handleGetUserWithinPOIsEvent(@NonNull final Event event) {
        Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetUserWithinPOIsEvent - Handling get user-within points of interest event.");

        final List<PlacesPOI> userWithinPOIs = state.getUserWithInPOIs();

        // dispatch userWithinPOIS for getUserWithinPOI API callback registered with awaiting one time listener.
        placesDispatcher.dispatchUserWithinPOIs(userWithinPOIs, event);
        // dispatch userWithinPOIS for all other listeners
        placesDispatcher.dispatchUserWithinPOIs(userWithinPOIs, null);
    }

    /**
     * Handler for getLastKnownLocation public api.
     *
     * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event requesting last known location
     */
    private void handleGetLastKnownLocation(@NonNull final Event event) {
        Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetLastKnownLocation - Handling get last known location event");
        final Location location = state.loadLastKnownLocation();

        if (location == null) {
            placesDispatcher.dispatchLastKnownLocation(PlacesConstants.INVALID_LAT_LON,
                    PlacesConstants.INVALID_LAT_LON, event);
            return;
        }

        // dispatch lastKnownLocation for the getLastKnownLocation API callback waiting with registered onetime listener
        placesDispatcher.dispatchLastKnownLocation(location.getLatitude(), location.getLongitude(),
                event);
        // dispatch lastKnownLocation for all other listeners
        placesDispatcher.dispatchLastKnownLocation(location.getLatitude(), location.getLongitude(),
                null);
    }

    /**
     * Saves the location from Places Request {@link Event} into persistence.
     *
     * @param event the getNearbyPOI {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event containing device's location
     */
    private void saveLastKnownLocation(@NonNull final Event event) {

        // Don't save the location if privacy is opted-out, Bail out right away
        final Map<String, Object> configData = retrieveConfigurationEventState(event);

        if (getMobilePrivacyStatus(configData) == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "saveLastKnownLocation - Ignoring to save the last known location, Privacy opted out");
            return;
        }

        final Map<String, Object> eventData = event.getEventData();
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
    private void handleSetAuthorizationStatusEvent(@NonNull final Event event) {
        final Map<String, Object> data = event.getEventData();
        final String authStatusString = DataReader.optString(data, PlacesConstants.EventDataKeys.Places.AUTH_STATUS, null);

        if (!PlacesAuthorizationStatus.isValidStatus(authStatusString)) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME,
                    "handleSetAuthorizationStatusEvent - Invalid Authorization status value is set to Places Extension. Please check PlacesAuthorizationStatus class.");
            return;
        }

        state.setAuthorizationStatus(authStatusString);

        // update the places shared state
        extensionApi.createSharedState(state.getPlacesSharedState(), event);
    }


    /**
     * Handler for `clear` public api.
     * Resets the shared state, all the in-memory and persisted places state variables.
     */
    private void reset() {
        Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "reset - Places shared state and persisted data has been reset.");
        state.clearData();
        extensionApi.createSharedState(new HashMap<>(), null);
    }


    /**
     * Public method to handle getNearByPlaces event.
     * <p>
     * This method handles the Places request content event generated when the getNearByPlaces API is called.
     * On valid configuration this method connects with the Place query services, fetches and dispatches the n near by poi's.
     *
     * @param event the {@link EventType#PLACES} - {@link EventSource#REQUEST_CONTENT} event
     */
    private void handleGetNearByPlaceEvent(@NonNull final Event event, final Map<String, Object> configData) {
        Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetNearByPlaceEvent - Handling get near by place event.");
        // Retrieve the latest configuration shared state data
        final PlacesConfiguration placesConfig = new PlacesConfiguration(configData);

        if (!placesConfig.isValid()) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetNearByPlaceEvent - Ignoring the get nearby places event, Invalid Configuration");
            placesDispatcher.dispatchNearbyPlaces(new ArrayList<>(),
                    PlacesRequestError.CONFIGURATION_ERROR, event);
            return;
        }

        // Bail out if privacy is opted out.
        if (getMobilePrivacyStatus(configData) == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGetNearByPlaceEvent - Ignoring the get nearby places event, Privacy opted out.");
            placesDispatcher.dispatchNearbyPlaces(new ArrayList<>(),
                    PlacesRequestError.PRIVACY_OPTED_OUT,
                    event);
            return;
        }


        queryService.getNearbyPlaces(event.getEventData(), placesConfig, response -> {

            if (!response.isSuccess) {
                Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, response.errorMessage);
                placesDispatcher.dispatchNearbyPlaces(new ArrayList<>(), response.resultStatus,
                        event);
                return;
            }

            // read and set the latest membership ttl value from configuration
            state.setMembershiptTtl(placesConfig.getMembershipTtl());

            // on success, process the response - cache POIs, persist POIs and update the shared state values
            state.processNetworkResponse(response);

            // update the places shared state
            extensionApi.createSharedState(state.getPlacesSharedState(), event);

            // dispatch nearbyPOI for the getNearbyPOI API callback waiting with registered onetime listener
            placesDispatcher.dispatchNearbyPlaces(response.getAllPOIs(), PlacesRequestError.OK,
                    event);
            // dispatch nearbyPOI list for other listeners
            placesDispatcher.dispatchNearbyPlaces(response.getAllPOIs(), PlacesRequestError.OK, null);

        });

    }

    private void handleGeofenceEvent(@NonNull final Event event, final Map<String, Object> configData) {
        Log.trace(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGeofenceEvent - Handling get geofence place event.");
        final PlacesConfiguration placesConfig = new PlacesConfiguration(configData);

        // Bail out if privacy is opted out.
        if (getMobilePrivacyStatus(configData) == MobilePrivacyStatus.OPT_OUT) {
            Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "handleGeofenceEvent - Ignoring the geofence event, Privacy opted out.");
            return;
        }

        // read and set the latest membership ttl value from configuration
        state.setMembershiptTtl(placesConfig.getMembershipTtl());

        // process the region event
        final PlacesRegion regionEvent = state.processRegionEvent(event);

        // update the places shared state
        extensionApi.createSharedState(state.getPlacesSharedState(), event);

        // dispatch the processed region event
        placesDispatcher.dispatchRegionEvent(regionEvent);

        // dispatch experience event to Edge
        placesDispatcher.dispatchExperienceEventToEdge(regionEvent);
    }


    /**
     * Retrieves the current Mobile SDK's configuration corresponding to the provided {@code Event}.
     *
     * @param event the {@link Event} for which the configuration has to be retrieved.
     * @return {@code Map<String,Object>} containing configuration shared state
     */
    private Map<String, Object> retrieveConfigurationEventState(final Event event) {
        return extensionApi.getSharedState(PlacesConstants.EventDataKeys.Configuration.EXTENSION_NAME, event, false, SharedStateResolution.ANY).getValue();
    }

    /**
     * Reads the privacy status from the current configuration.
     * <p>
     * {@link MobilePrivacyStatus#UNKNOWN} is returned when
     * <ol>
     * 		<li>No Valid Configuration shared state is available.</li>
     * 		<li>No privacy configuration key is found in configuration shared state.</li>
     * </ol>
     *
     * @return the privacy status.
     */
    private MobilePrivacyStatus getMobilePrivacyStatus(final Map<String, Object> configData) {
        if (configData != null
                && configData.containsKey(PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY)) {
            final String privacyString = DataReader.optString(configData, PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY, "unknown");
            return MobilePrivacyStatus.fromString(privacyString);
        } else {
            return MobilePrivacyStatus.UNKNOWN;
        }
    }

}