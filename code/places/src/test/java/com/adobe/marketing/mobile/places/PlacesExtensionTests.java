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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.location.Location;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.Places;
import com.adobe.marketing.mobile.SharedStateResult;
import com.adobe.marketing.mobile.SharedStateStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlacesExtensionTests {

    PlacesExtension extension;

    @Mock PlacesState state;

    @Mock private PlacesDispatcher placesDispatcher;

    @Mock private PlacesQueryService queryService;

    @Mock private ExtensionApi extensionApi;

    @Mock private Location mockLocation;

    private static final long SAMPLE_TTL = 8990;

    @Before
    public void testSetup() {
        extension = new PlacesExtension(extensionApi);
        extension.placesDispatcher = placesDispatcher;
        extension.queryService = queryService;
        extension.state = state;

        reset(extensionApi);
        reset(state);
    }

    @Test
    public void test_getVersion() {
        assertEquals(Places.EXTENSION_VERSION, extension.getVersion());
    }

    @Test
    public void test_getName() {
        assertEquals("com.adobe.module.places", extension.getName());
    }

    @Test
    public void test_getFriendlyName() {
        assertEquals("Places", extension.getFriendlyName());
    }

    @Test
    public void test_onRegister() {
        // setup
        Map<String, Object> placesSharedState =
                new HashMap<String, Object>() {
                    {
                        put("key", "value");
                    }
                };
        when(state.getPlacesSharedState()).thenReturn(placesSharedState);

        // test
        extension.onRegistered();

        // verify that two listeners are registers
        verify(extensionApi, times(2)).registerEventListener(any(), any(), any());
        verify(extensionApi, times(1)).createSharedState(placesSharedState, null);
    }

    // ========================================================================================
    // readyForEvent
    // ========================================================================================

    @Test
    public void test_readyForEvent_whenConfigurationSet() {
        // setup
        setConfigurationSharedState("optedin");

        // test
        assertTrue(extension.readyForEvent(testGeofenceEvent()));
    }

    @Test
    public void test_readyForEvent_whenConfigurationNotSet() {
        // setup
        resetConfigurationSharedState();

        // test
        assertFalse(extension.readyForEvent(testGeofenceEvent()));
    }

    // ========================================================================================
    // handleConfigurationEvent
    // ========================================================================================

    @Test
    public void handleConfigurationEvent_WhenPrivacyOptIn() {
        // setup
        setConfigurationSharedState("optedin");

        // test
        extension.handleConfigurationResponseEvent(emptyEvent());

        // verify that shared state and events are not stopped.
        verifyNoInteractions(state);
        verify(extensionApi, times(0)).stopEvents();
        verify(extensionApi, times(0)).createSharedState(eq(new HashMap<>()), eq(null));
    }

    @Test
    public void handleConfigurationEvent_WhenPrivacyOptOut() {
        // setup
        setConfigurationSharedState("optedout");

        // test
        extension.handleConfigurationResponseEvent(emptyEvent());

        // verify
        verify(state).clearData();
        verify(extensionApi).stopEvents();
        verify(extensionApi).createSharedState(eq(new HashMap<>()), eq(null));
    }

    @Test
    public void handleConfigurationEvent_WhenPrivacyUnknown() {
        // setup
        setConfigurationSharedState("unknown");

        // test
        extension.handleConfigurationResponseEvent(emptyEvent());

        // verify that shared state and events are not stopped.
        verifyNoInteractions(state);
        verify(extensionApi, times(0)).stopEvents();
        verify(extensionApi, times(0)).createSharedState(eq(new HashMap<>()), eq(null));
    }

    // ========================================================================================
    // getNearByPlaceEvent
    // ========================================================================================

    @Test
    public void getNearByPlaceEvent_when_InvalidConfiguration() {
        // setup
        setInvalidConfigurationSharedState();

        // test
        extension.handlePlacesRequestEvent(testGetNearByPOIEvent());

        // verify
        verifyNoInteractions(queryService);
        verify(placesDispatcher)
                .dispatchNearbyPlaces(
                        eq(new ArrayList<>()),
                        eq(PlacesRequestError.CONFIGURATION_ERROR),
                        any(Event.class));
    }

    @Test
    public void getNearByPlaceEvent_when_privacyOptOut() {
        // setup
        setConfigurationSharedState("optedout");

        // test
        extension.handlePlacesRequestEvent(testGetNearByPOIEvent());

        // verify
        verifyNoInteractions(state);
        verifyNoInteractions(queryService);
        verify(placesDispatcher)
                .dispatchNearbyPlaces(
                        eq(new ArrayList<>()),
                        eq(PlacesRequestError.PRIVACY_OPTED_OUT),
                        any(Event.class));
    }

    @Test
    public void getNearByPlaceEvent_when_queryServiceResponseIsNotASuccess() {
        // setup
        setConfigurationSharedState("optedin");

        // setup call back to respond with service error
        PlacesQueryResponse failedResponse = new PlacesQueryResponse();
        failedResponse.fetchFailed("", PlacesRequestError.SERVER_RESPONSE_ERROR);
        doAnswer(
                        invocation -> {
                            ((PlacesQueryResponseCallback) invocation.getArguments()[2])
                                    .call(failedResponse);
                            return null;
                        })
                .when(queryService)
                .getNearbyPlaces(any(), any(), any());

        // test
        extension.handlePlacesRequestEvent(testGetNearByPOIEvent());

        // verify
        verify(placesDispatcher)
                .dispatchNearbyPlaces(
                        eq(new ArrayList<>()),
                        eq(PlacesRequestError.SERVER_RESPONSE_ERROR),
                        any(Event.class));
    }

    @Test
    public void getNearByPlaceEvent_happy() {
        // setup
        setConfigurationSharedState("optedin");

        PlacesQueryResponse sampleQueryResponse = createSuccessQueryResponse();
        doAnswer(
                        invocation -> {
                            ((PlacesQueryResponseCallback) invocation.getArguments()[2])
                                    .call(sampleQueryResponse);
                            return null;
                        })
                .when(queryService)
                .getNearbyPlaces(any(), any(), any());

        // test
        Event event = testGetNearByPOIEvent();
        extension.handlePlacesRequestEvent(event);

        // verify interaction with QueryService
        verify(queryService).getNearbyPlaces(any(), any(), any());

        // verify interaction with placesState
        verify(state).processNetworkResponse(eq(sampleQueryResponse));

        // verify shared state creation
        verify(extensionApi).createSharedState(any(Map.class), eq(event));

        // verify interactions with dispatcher
        verify(placesDispatcher)
                .dispatchNearbyPlaces(
                        eq(sampleQueryResponse.getAllPOIs()), eq(PlacesRequestError.OK), eq(event));
        verify(placesDispatcher)
                .dispatchNearbyPlaces(
                        eq(sampleQueryResponse.getAllPOIs()), eq(PlacesRequestError.OK), eq(null));
    }

    // ========================================================================================
    // handleGeofenceEvent
    // ========================================================================================

    @Test
    public void handleGeofenceEvent_when_privacyOptedOut() {
        // setup
        setConfigurationSharedState("optedout");
        PlacesRegion region = new PlacesRegion(createPOI("poi"), "entry", 100);
        HashMap<String, Object> placesState = new HashMap<>();
        placesState.put("key", "value");

        when(state.getPlacesSharedState()).thenReturn(placesState);
        when(state.processRegionEvent(any())).thenReturn(region);

        // test
        extension.handlePlacesRequestEvent(testGeofenceEvent());

        // verify
        verifyNoInteractions(state);
        verify(extensionApi, times(0)).createSharedState(any(), any());
        verifyNoInteractions(placesDispatcher);
    }

    @Test
    public void handleGeofenceEvent_Happy() {
        // setup
        setConfigurationSharedState("optedin");
        PlacesRegion region = new PlacesRegion(createPOI("poi"), "entry", 100);
        HashMap<String, Object> placesState = new HashMap<>();
        placesState.put("key", "value");

        when(state.getPlacesSharedState()).thenReturn(placesState);
        when(state.processRegionEvent(any())).thenReturn(region);

        // test
        final Event event = testGeofenceEvent();
        extension.handlePlacesRequestEvent(event);

        // verify
        verify(state).setMembershiptTtl(eq(SAMPLE_TTL));

        // verify the creation of shared state
        verify(extensionApi).createSharedState(eq(placesState), eq(event));

        // verify the dispatched event
        verify(placesDispatcher).dispatchRegionEvent(eq(region));

        // verify the dispatched edge event
        verify(placesDispatcher).dispatchExperienceEventToEdge(eq(region));
    }

    // ========================================================================================
    // handleGetUserWithinPOIsEvent
    // ========================================================================================
    @Test
    public void handleGetUserWithinPOIsEvent_Happy() {
        // setup
        HashMap<String, Object> data = new HashMap<>();
        data.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES);

        Event testEvent =
                new Event.Builder(
                                "Get UserWithin POIs event",
                                EventType.PLACES,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(data)
                        .build();
        ArrayList<PlacesPOI> pois = new ArrayList<PlacesPOI>();
        pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, "libraryName", 2, null));
        pois.add(new PlacesPOI("identifier2", "name", 34.33, -121.55, 50, "libraryName", 2, null));
        when(state.getUserWithInPOIs()).thenReturn(pois);

        // test
        extension.handlePlacesRequestEvent(testEvent);

        // verify
        verify(state).getUserWithInPOIs();

        // verify the interaction with the dispatcher
        verify(placesDispatcher, times(1)).dispatchUserWithinPOIs(eq(pois), eq(testEvent));
        verify(placesDispatcher, times(1)).dispatchUserWithinPOIs(eq(pois), eq(null));
    }

    // ========================================================================================
    // handleGetLastKnownLocation
    // ========================================================================================
    @Test
    public void handleGetLastKnownLocation_Happy() {
        // setup
        HashMap<String, Object> data = new HashMap<>();
        data.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);

        Event testEvent =
                new Event.Builder(
                                "Get Last known location",
                                EventType.PLACES,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(data)
                        .build();
        when(mockLocation.getLatitude()).thenReturn(11.11);
        when(mockLocation.getLongitude()).thenReturn(22.22);

        when(state.loadLastKnownLocation()).thenReturn(mockLocation);

        // test
        extension.handlePlacesRequestEvent(testEvent);

        // verify the interaction with dispatcher
        verify(placesDispatcher).dispatchLastKnownLocation(eq(11.11), eq(22.22), eq(testEvent));
        verify(placesDispatcher).dispatchLastKnownLocation(eq(11.11), eq(22.22), eq(null));
    }

    @Test
    public void handleGetLastKnownLocation_When_NullLocationReturned() {
        // setup
        HashMap<String, Object> data = new HashMap<>();
        data.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);

        Event testEvent =
                new Event.Builder(
                                "Get Last known location",
                                EventType.PLACES,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(data)
                        .build();
        when(state.loadLastKnownLocation()).thenReturn(null);

        // test
        extension.handlePlacesRequestEvent(testEvent);

        // verify the interaction with dispatcher
        verify(placesDispatcher)
                .dispatchLastKnownLocation(
                        eq(PlacesTestConstants.INVALID_LAT_LON),
                        eq(PlacesTestConstants.INVALID_LAT_LON),
                        eq(testEvent));
    }

    // ========================================================================================
    // handleSetAuthorizationStatusEvent
    // ========================================================================================
    @Test
    public void handleSetLocationPermissionStatusEvent_Happy() {
        // test
        extension.handlePlacesRequestEvent(
                testSetLocationPermissionStatusEvent(
                        PlacesAuthorizationStatus.ALWAYS.stringValue()));

        // verify
        verify(state).setAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS.stringValue());
        verify(extensionApi).createSharedState(any(), any());
    }

    @Test
    public void handleSetLocationPermissionStatusEvent_InvalidStatusValue() {
        // test
        extension.handlePlacesRequestEvent(testSetLocationPermissionStatusEvent("invalid"));

        // verify
        verifyNoInteractions(state);
        verifyNoInteractions(extensionApi);
    }

    // ========================================================================================
    // saveLastKnownLocation
    // ========================================================================================
    @Test
    public void getNearByAPIEvents_savesLastKnownLocation() {
        // setup
        setConfigurationSharedState("optedin");

        // test
        extension.handlePlacesRequestEvent(testGetNearByPOIEvent());

        // verify
        verify(state).saveLastKnownLocation(eq(34.33), eq(-124.33));
    }

    @Test
    public void saveLastKnownLocation_When_InvalidLocation() {
        // setup
        setConfigurationSharedState("optedin");
        HashMap<String, Object> data = new HashMap<>();
        data.put(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 44.2);
        data.put(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, -2124.33);
        data.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES);

        Event testEvent =
                new Event.Builder(
                                "Get nearby places event",
                                EventType.PLACES,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(data)
                        .build();

        // test
        extension.handlePlacesRequestEvent(testEvent);

        // verify
        verifyNoInteractions(state);
    }

    // ========================================================================================
    // Helper methods
    // ========================================================================================
    private HashMap<String, Object> setConfigurationSharedState(final String privacyStatus) {
        HashMap<String, Object> configData = new HashMap<>();
        configData.put(
                PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY,
                privacyStatus);
        List<Map<String, String>> libraries = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Map<String, String> library = new HashMap<>();
            library.put(
                    PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID,
                    "lib" + (i + 1));
            libraries.add(library);
        }

        configData.put(
                PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES,
                libraries);
        configData.put(
                PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT,
                "endpoint");
        configData.put(
                PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL,
                SAMPLE_TTL);

        when(extensionApi.getSharedState(
                        eq(PlacesTestConstants.EventDataKeys.Configuration.EXTENSION_NAME),
                        any(),
                        anyBoolean(),
                        any()))
                .thenReturn(new SharedStateResult(SharedStateStatus.SET, configData));
        return configData;
    }

    private HashMap<String, Object> resetConfigurationSharedState() {
        HashMap<String, Object> configData = new HashMap<>();
        when(extensionApi.getSharedState(
                        eq(PlacesTestConstants.EventDataKeys.Configuration.EXTENSION_NAME),
                        any(),
                        anyBoolean(),
                        any()))
                .thenReturn(new SharedStateResult(SharedStateStatus.NONE, configData));
        return configData;
    }

    private HashMap<String, Object> setInvalidConfigurationSharedState() {
        // Configuration shared state with no places properties
        HashMap<String, Object> configData = new HashMap<>();
        configData.put(
                PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY,
                "optedin");
        when(extensionApi.getSharedState(
                        eq(PlacesTestConstants.EventDataKeys.Configuration.EXTENSION_NAME),
                        any(),
                        anyBoolean(),
                        any()))
                .thenReturn(new SharedStateResult(SharedStateStatus.SET, configData));
        return configData;
    }

    private Event emptyEvent() {
        Event event =
                new Event.Builder("emptyEvent", EventType.PLACES, EventSource.RESPONSE_CONTENT)
                        .build();
        return event;
    }

    private Event testGetNearByPOIEvent() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 34.33);
        data.put(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, -124.33);
        data.put(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, 20);
        data.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES);

        return new Event.Builder(
                        "Get nearby places event", EventType.PLACES, EventSource.REQUEST_CONTENT)
                .setEventData(data)
                .build();
    }

    private Event testSetLocationPermissionStatusEvent(final String status) {
        HashMap<String, Object> eventData = new HashMap<>();
        eventData.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS);
        eventData.put(PlacesTestConstants.EventDataKeys.Places.AUTH_STATUS, status);

        return new Event.Builder(
                        PlacesTestConstants.EventName.REQUEST_SETAUTHORIZATIONSTATUS,
                        EventType.PLACES,
                        EventSource.REQUEST_CONTENT)
                .setEventData(eventData)
                .build();
    }

    private Event testGeofenceEvent() {
        HashMap<String, Object> data = new HashMap<>();
        data.put(PlacesTestConstants.EventDataKeys.Places.REGION_ID, "regionID");
        data.put(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE, 1);
        data.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT);

        return new Event.Builder("Geofence event", EventType.PLACES, EventSource.REQUEST_CONTENT)
                .setEventData(data)
                .build();
    }

    private PlacesPOI createPOI(final String id) {
        return new PlacesPOI(id, "hidden", 34.33, -121.55, 150, "libraryName", 22, null);
    }

    private PlacesQueryResponse createSuccessQueryResponse() {
        PlacesQueryResponse response = new PlacesQueryResponse();
        response.containsUserPOIs =
                new ArrayList<PlacesPOI>() {
                    {
                        add(createPOI("poi1"));
                        add(createPOI("poi2"));
                    }
                };
        response.nearByPOIs =
                new ArrayList<PlacesPOI>() {
                    {
                        add(createPOI("poi3"));
                        add(createPOI("poi4"));
                    }
                };
        response.isSuccess = true;
        return response;
    }
}
