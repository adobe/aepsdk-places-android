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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.location.Location;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.AdobeCallbackWithError;
import com.adobe.marketing.mobile.AdobeError;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionErrorCallback;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Places;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlacesTests {

    @Mock
    Location mockLocation;

    @Mock
    GeofencingEvent geofencingEvent;

    @Mock
    Geofence geofence1;

    @Mock
    Geofence geofence2;

    private List<PlacesPOI> responseNearByPois = new ArrayList<>();
    private boolean successCallbackCalled;
    private boolean errorCallbackCalled;
    AdobeCallback<List<PlacesPOI>> successCallback;
    AdobeCallback<PlacesRequestError> errorCallback;
    private PlacesRequestError placesRequestError;
    private ArgumentCaptor<Event> eventCaptor;
    private ArgumentCaptor<AdobeCallbackWithError> callbackCaptor = ArgumentCaptor.forClass(AdobeCallbackWithError.class);
    MockedStatic<MobileCore> mockedMobileCore;
    private MockedConstruction<Location> locationConstructor;

    @Before
    public void testSetup() {
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        mockedMobileCore = mockStatic(MobileCore.class);
        successCallbackCalled = false;
        errorCallbackCalled = false;
        successCallback = (pois) -> {
            successCallbackCalled = true;
            responseNearByPois = pois;
        };

        errorCallback = (result) -> {
            errorCallbackCalled = true;
            placesRequestError = result;
        };
    }

    @After
    public void clean() {
        mockedMobileCore.close();
    }


    @Test
    public void test_registerExtension() {
        final ArgumentCaptor<Class> extensionClassCaptor = ArgumentCaptor.forClass(Class.class);
        final ArgumentCaptor<ExtensionErrorCallback> callbackCaptor = ArgumentCaptor.forClass(
                ExtensionErrorCallback.class
        );
        mockedMobileCore
                .when(() -> MobileCore.registerExtension(extensionClassCaptor.capture(), callbackCaptor.capture()))
                .thenReturn(true);
        // test
        Places.registerExtension();

        // verify: happy
        assertNotNull(callbackCaptor.getValue());
        assertEquals(PlacesExtension.class, extensionClassCaptor.getValue());

        // verify: error callback was called
        callbackCaptor.getValue().error(null);
    }

    @Test
    public void getNearbyPlaces_should_dispatchPlacesRequestContentEvent() {
        // test
        Places.getNearbyPointsOfInterest(mockLocation(22.22, -11.11), 20, successCallback, errorCallback);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), anyLong(), any()));
        final Event dispatchedEvent = eventCaptor.getValue();
        assertNotNull(dispatchedEvent);

        // verify dispatched event details
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_GETNEARBYPLACES,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());
        assertEquals("event has the correct latitude data", 22.22,
                (double) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.LATITUDE), 0.0);
        assertEquals("event has the correct longitude data", -11.11,
                (double) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.LONGITUDE), 0.00d);
        assertEquals("event has the correct places count", 20,
                (int) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT), 0);
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES,
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
    }

    @Test
    public void getNearbyPlaces_when_responseEvent_with_validPOIs() {
        // setup
        Map<String,Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, PlacesUtil.convertPOIListToMap(getSamplePOIList(2)));
        eventData.put(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS, PlacesRequestError.OK.getValue());
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();

        // test
        Places.getNearbyPointsOfInterest(mockLocation(22.22, -11.11), 20, successCallback, errorCallback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertTrue("SuccessCallback is called ", successCallbackCalled);
        assertFalse("ErrorCallback is not called ", errorCallbackCalled);
        assertEquals("the callback is called with correct pois", 2, responseNearByPois.size());
    }

    @Test
    public void getNearbyPlaces_when_responseEvent_with_connectivityError_status() {
        // setup
        Map<String,Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS,
                PlacesRequestError.CONNECTIVITY_ERROR.getValue());
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();

        // test
        Places.getNearbyPointsOfInterest(mockLocation(22.22, -11.11), 20, successCallback, errorCallback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertFalse("SuccessCallback is not called ", successCallbackCalled);
        assertTrue("ErrorCallback is called ", errorCallbackCalled);
        assertEquals(PlacesRequestError.CONNECTIVITY_ERROR, placesRequestError);
    }

    @Test
    public void getNearbyPlaces_when_responseEvent_with_inValidPOIS() {
        // setup
        Map<String,Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, "Invalid");
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();
        // test
        Places.getNearbyPointsOfInterest(mockLocation(22.22, -11.11), 20, successCallback, errorCallback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertFalse("SuccessCallback is not called ", successCallbackCalled);
        assertTrue("ErrorCallback is called ", errorCallbackCalled);
        assertEquals(PlacesRequestError.UNKNOWN_ERROR, placesRequestError);
    }

    @Test
    public void getNearbyPlaces_when_responseEvent_with_nullEventData() {
        // setup
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(null).build();

        // test
        // test
        Places.getNearbyPointsOfInterest(mockLocation(22.22, -11.11), 20, successCallback, errorCallback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertFalse("SuccessCallback is not called ", successCallbackCalled);
        assertTrue("ErrorCallback is called ", errorCallbackCalled);
        assertEquals(PlacesRequestError.UNKNOWN_ERROR, placesRequestError);
    }

    @Test
    public void getNearbyPlaces_when_callbackIsNull() {
        // test
        Places.getNearbyPointsOfInterest(mockLocation(11.11, -22.22), 20, null, null);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), any()));
    }

    @Test
    public void getNearbyPlaces_when_callbackTimeout() {
        // test
        Places.getNearbyPointsOfInterest(mockLocation(11.11, -22.22), 20, successCallback, errorCallback);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().fail(AdobeError.CALLBACK_TIMEOUT);

        // verify if the error callback is called with correct value
        assertTrue(errorCallbackCalled);
        assertEquals(PlacesRequestError.UNKNOWN_ERROR, placesRequestError);
    }

    // ========================================================================================
    // getCurrentPointsOfInterest
    // ========================================================================================

    @Test
    public void getCurrentPointsOfInterest_should_dispatchPlacesRequestContentEvent() {
        // setup
        AdobeCallback<List<PlacesPOI>> callback = (pois) ->{};

        // test
        Places.getCurrentPointsOfInterest(callback);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), anyLong(), any()));
        final Event dispatchedEvent = eventCaptor.getValue();
        assertNotNull(dispatchedEvent);

        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_GETUSERWITHINPLACES,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES, dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
    }

    @Test
    public void getCurrentPointsOfInterest_when_callbackIsNull() {
        // test
        Places.getCurrentPointsOfInterest(null);

        // verify
        mockedMobileCore.verifyNoInteractions();
    }

    @Test
    public void getCurrentPointsOfInterest_when_responseEvent_with_validPOIs() {
        // setup
        AdobeCallback<List<PlacesPOI>> callback = (pois) -> {
            responseNearByPois = pois;
        };

        // create response event
        Map<String,Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS, PlacesUtil.convertPOIListToMap(getSamplePOIList(2)));
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();


        // test
        Places.getCurrentPointsOfInterest(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(),callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);


        // verify
        assertEquals("the callback is called with correct pois", 2, responseNearByPois.size());
    }

    @Test
    public void getCurrentPointsOfInterest_when_responseEvent_with_invalidPOIS() {
        // setup
        AdobeCallback<List<PlacesPOI>> callback = (pois) -> { responseNearByPois = pois; };
        Map<String,Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS, "Invalid");
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();


        // test
        Places.getCurrentPointsOfInterest(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(),callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertEquals("the callback is called with empty pois", 0, responseNearByPois.size());
    }

    @Test
    public void getCurrentPointsOfInterest_when_responseEvent_with_nullEventData() {
        // setup
        AdobeCallback<List<PlacesPOI>> callback = (pois) -> {responseNearByPois = pois;};
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(null).build();


        // test
        Places.getCurrentPointsOfInterest(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(),callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertEquals("the callback is called with empty pois", 0, responseNearByPois.size());
    }

    @Test
    public void getCurrentPointsOfInterest_when_callbackTimeout() {
        // setup
        AdobeError[] obtainedError = new AdobeError[1];
        AdobeCallbackWithError<List<PlacesPOI>> callback = new AdobeCallbackWithError<List<PlacesPOI>>() {
            @Override
            public void fail(AdobeError adobeError) {
                obtainedError[0] = adobeError;
            }

            @Override
            public void call(List<PlacesPOI> pois) {}
        };

        // test
        Places.getCurrentPointsOfInterest(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(),callbackCaptor.capture()));
        callbackCaptor.getValue().fail(AdobeError.CALLBACK_TIMEOUT);

        // verify
        assertEquals(AdobeError.CALLBACK_TIMEOUT, obtainedError[0]);
    }


    // ========================================================================================
    // processGeofenceEvent
    // ========================================================================================

    @Test
    public void getLastKnownLocation_should_dispatchPlacesRequestContentEvent() {
        // setup
        AdobeCallback<Location> callback = (location) -> {};

        // test
        Places.getLastKnownLocation(callback);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(eventCaptor.capture(), anyLong(), any()));
        final Event dispatchedEvent = eventCaptor.getValue();
        assertNotNull(dispatchedEvent);

        // verify the dispatched event details
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_GETLASTKNOWNLOCATION,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());

        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION,
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
    }

    @Test
    public void getLastKnownLocation_when_callbackIsNull() {
        // test
        Places.getLastKnownLocation(null);

        // verify
        mockedMobileCore.verifyNoInteractions();
    }

    @Test
    public void getLastKnownLocation_when_responseEvent_with_nullEventData() {
        // setup
        final Location[] obtainedLocation = new Location[1];
        AdobeCallback<Location> callback = (location) -> {
            obtainedLocation[0] = location;
        };
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES, EventSource.RESPONSE_CONTENT).build();

        // test
        Places.getLastKnownLocation(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertNull("The callback is called with null location", obtainedLocation[0]);
    }

    @Test
    public void getLastKnownLocation_when_responseEvent_with_InvalidLatLong() {
        // setup
        final Location[] obtainedLocation = new Location[1];
        AdobeCallback<Location> callback = (location) -> {
            obtainedLocation[0] = location;
        };

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, -91);
        eventData.put(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, 100);
        Event responseEventInvalidLatLong = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();

        // test
        Places.getLastKnownLocation(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEventInvalidLatLong);

        // verify
        assertNull("The callback is called with null location", obtainedLocation[0]);
    }

    @Test
    public void getLastKnownLocation_when_errorResponse() {
        // setup
        final AdobeError[] capturedError = new AdobeError[1];
        AdobeCallbackWithError<Location> callback = new AdobeCallbackWithError<Location>() {
            @Override
            public void fail(AdobeError adobeError) {
                capturedError[0] = adobeError;
            }

            @Override
            public void call(Location location) {
            }
        };

        // test
        Places.getLastKnownLocation(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().fail(AdobeError.UNEXPECTED_ERROR);

        // verify
        assertEquals("The callback is called with correct error.", capturedError[0], AdobeError.UNEXPECTED_ERROR);
    }

    @Test
    public void getLastKnownLocation_when_responseEvent_with_validLatLong() {
        // setup
        final Location[] obtainedLocation = new Location[1];
        AdobeCallback<Location> callback = (location) -> {
            obtainedLocation[0] = location;
        };

        locationConstructor = mockConstruction(Location.class, (mock, context) -> {
            when(mock.getLatitude()).thenReturn(11.11);
            when(mock.getLongitude()).thenReturn(-22.22);
        });

        Map<String, Object> eventData = new HashMap();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, 11.11);
        eventData.put(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, -22.22);
        Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
                EventSource.RESPONSE_CONTENT).setEventData(eventData).build();

        // test
        Places.getLastKnownLocation(callback);
        mockedMobileCore.verify(() -> MobileCore.dispatchEventWithResponseCallback(any(), anyLong(), callbackCaptor.capture()));
        callbackCaptor.getValue().call(responseEvent);

        // verify
        assertNotNull("The callback is called with valid location", obtainedLocation[0]);
        assertEquals("The callback is called with valid latitude", 11.11, obtainedLocation[0].getLatitude(), 0.0);
        assertEquals("The callback is called with valid latitude", -22.22, obtainedLocation[0].getLongitude(), 0.0);

        locationConstructor.close();
    }

    // ========================================================================================
    // processGeofenceEvent
    // ========================================================================================

    @Test
    public void processGeofenceEvent_should_dispatchPlacesRequestContentEvent() {
        // test
        Places.processGeofenceEvent(mockGeofencingEvent(Geofence.GEOFENCE_TRANSITION_ENTER, Arrays.asList(mockGeofence1())));

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
        final Event dispatchedEvent = eventCaptor.getValue();
        assertNotNull(dispatchedEvent);

        // verify dispatched event details
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_PROCESSREGIONEVENT,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT,
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
        assertEquals("event has the correct regionId", "geofence1",
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_ID));
        assertEquals("event has the correct region transition type", "entry",
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE));
    }

    @Test
    public void processGeofenceEvent_when_multiplePOIExited() {
        // test
        Places.processGeofenceEvent(mockGeofencingEvent(Geofence.GEOFENCE_TRANSITION_EXIT, Arrays.asList(mockGeofence1(), mockGeofence2())));

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()), times(2));
        final Event exitEvent1 = eventCaptor.getAllValues().get(0);
        final Event exitEvent2 = eventCaptor.getAllValues().get(1);


        // verify first exit event dispatched
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_PROCESSREGIONEVENT,
                exitEvent1.getName());
        assertEquals("event has correct event type", EventType.PLACES, exitEvent1.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, exitEvent1.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT,
                exitEvent1.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
        assertEquals("event has the correct regionId", "geofence1",
                exitEvent1.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_ID));
        assertEquals("event has the correct region transition type", "exit",
                exitEvent1.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE));

        // verify second exit event dispatched
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_PROCESSREGIONEVENT,
                exitEvent2.getName());
        assertEquals("event has correct event type", EventType.PLACES, exitEvent2.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, exitEvent2.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT,
                exitEvent2.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
        assertEquals("event has the correct regionId", "geofence2",
                exitEvent2.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_ID));
        assertEquals("event has the correct region transition type", "exit",
                exitEvent2.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE));
    }

    // ========================================================================================
    // processGeofence
    // ========================================================================================

    @Test
    public void processGeofence_should_dispatchPlacesRequestContentEvent() {
        // test
        Places.processGeofence(mockGeofence1(), Geofence.GEOFENCE_TRANSITION_ENTER);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
        final Event dispatchedEvent = eventCaptor.getValue();
        assertNotNull(dispatchedEvent);

        // verify dispatched event details
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_PROCESSREGIONEVENT,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT,
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
        assertEquals("event has the correct regionId", "geofence1",
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_ID));
        assertEquals("event has the correct region transition type", "entry",
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE));
    }

    @Test
    public void processGeofence_when_nullGeofence() {
        // test
        Places.processGeofence(null, Geofence.GEOFENCE_TRANSITION_ENTER);

        // verify no event is dispatched
        mockedMobileCore.verifyNoInteractions();
    }

    @Test
    public void processGeofence_when_invalidGeofenceType() {
        // test
        Places.processGeofence(mockGeofence1(), Geofence.GEOFENCE_TRANSITION_DWELL);

        // verify no event is dispatched
        mockedMobileCore.verifyNoInteractions();
    }

    // ========================================================================================
    // setAuthorizationStatus
    // ========================================================================================

    @Test
    public void setLocationPermissionStatus_should_dispatchRequestContentEvent() {
        // test
        Places.setAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS);

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
        final Event dispatchedEvent = eventCaptor.getValue();
        assertNotNull(dispatchedEvent);

        // verify dispatched event details
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_SETAUTHORIZATIONSTATUS,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS,
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
        assertEquals("event has the correct authorization status",
                PlacesAuthorizationStatus.ALWAYS.stringValue(),
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.AUTH_STATUS));
    }

    @Test
    public void setLocationPermissionStatus_withNullStatus_shouldNotDispatchEvent() {
        // test
        Places.setAuthorizationStatus(null);

        // verify
        mockedMobileCore.verifyNoInteractions();
    }

    // ========================================================================================
    // clear
    // ========================================================================================

    @Test
    public void test_clear_dispatchPlacesRequestContentEvent() {
        // test
        Places.clear();

        // verify
        mockedMobileCore.verify(() -> MobileCore.dispatchEvent(eventCaptor.capture()));
        assertNotNull(eventCaptor.getValue());
        final Event dispatchedEvent = eventCaptor.getValue();

        // verify dispatched event details
        assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_RESET,
                dispatchedEvent.getName());
        assertEquals("event has correct event type", EventType.PLACES, dispatchedEvent.getType());
        assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, dispatchedEvent.getSource());
        assertEquals("event has the correct request type",
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_RESET,
                dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE));
    }

    // ========================================================================================
    // Private methods
    // ========================================================================================

    private List<PlacesPOI> getSamplePOIList(final int count) {
        List<PlacesPOI> pois = new ArrayList<PlacesPOI>();

        for (int i = 0; i < count; i++) {
            pois.add(new PlacesPOI("nearByPOI" + i, "hidden", 34.33, -121.55, 150, "libraryName", 2, null));
        }

        return pois;
    }

    private Location mockLocation(double latitude, double longitude) {
        when(mockLocation.getLatitude()).thenReturn(latitude);
        when(mockLocation.getLongitude()).thenReturn(longitude);
        return mockLocation;
    }

    private GeofencingEvent mockGeofencingEvent(int geofenceTransitionType, List<Geofence> geofences) {
        when(geofencingEvent.getGeofenceTransition()).thenReturn(geofenceTransitionType);
        when(geofencingEvent.getTriggeringGeofences()).thenReturn(geofences);
        return geofencingEvent;
    }

    private Geofence mockGeofence1() {
        when(geofence1.getRequestId()).thenReturn("geofence1");
        return geofence1;
    }

    private Geofence mockGeofence2() {
        when(geofence2.getRequestId()).thenReturn("geofence2");
        return geofence2;
    }

}
