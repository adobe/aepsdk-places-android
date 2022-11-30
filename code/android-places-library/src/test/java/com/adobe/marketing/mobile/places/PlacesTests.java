/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Inc.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Inc. and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Inc..
 **************************************************************************/
package com.adobe.marketing.mobile.places;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import android.location.Location;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Places;

@RunWith(RobolectricTestRunner.class)
public class PlacesTests {

	private Places core;
	private List<PlacesPOI> responseNearByPois = new ArrayList<>();
	private boolean successCallbackCalled;
	private boolean errorCallbackCalled;
	AdobeCallback<List<PlacesPOI>> successCallback;
	AdobeCallback<PlacesRequestError> errorCallback;
	private PlacesRequestError placesRequestError;

//
//	@Before
//	public void testSetup() {
//		super.beforeEach();
//		obtainedLocation = new PlacesGpsLocation(77, 77);
//		successCallbackCalled = false;
//		errorCallbackCalled = false;
//
//		successCallback = new AdobeCallback<List<PlacesPOI>>() {
//			@Override
//			public void call(List<PlacesPOI> values) {
//				successCallbackCalled = true;
//				responseNearByPois = values;
//			}
//		};
//
//		errorCallback = new AdobeCallback<PlacesRequestError> () {
//			@Override
//			public void call(PlacesRequestError result) {
//				errorCallbackCalled = true;
//				placesRequestError = result;
//			}
//		};
//	}

	@Test
	public void getNearbyPlaces_should_dispatchPlacesRequestContentEvent()  {
		// setup
		double latitude = 31.33;
		double longitude = -121.34;
		Location location = new Location("check");
		location.setLongitude(13.00);
		location.setLongitude(-11.44);
		AdobeCallback<List<PlacesPOI>> callback = new AdobeCallback<List<PlacesPOI>>() {
			@Override
			public void call(List<PlacesPOI> values) {

			}
		};

//		// test
//		Places.getNearbyPointsOfInterest(location, 20, callback);
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_GETNEARBYPLACES,
//					 eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.PLACES, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertNotNull("event has eventData", eventHub.dispatchedEvent.getData());
//
//
//		assertEquals("event has the correct latitude data", latitude,
//					 eventHub.dispatchedEvent.getData().optDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 0.00d), 0);
//		assertEquals("event has the correct longitude data", longitude,
//					 eventHub.dispatchedEvent.getData().optDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, 0.00d), 0);
//		assertEquals("event has the correct places count", 20,
//					 eventHub.dispatchedEvent.getData().optInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, 0));
//		assertEquals("event has the correct request type",
//					 PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES,
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, ""));
//		assertTrue("one-time listener is registered for callback", eventHub.registerOneTimeListenerCalled);
//		assertNotNull("one-time listener has a callback block", eventHub.registerOneTimeListenerParamBlock);
	}


//	@Test
//	public void getNearbyPlaces_when_responseEvent_with_validPOIs() {
//		// setup
//
//		double latitude = 31.33;
//		double longitude = -121.34;
//		PlacesGpsLocation location = new PlacesGpsLocation(latitude, longitude);
//		EventData eventData = new EventData();
//		eventData.putTypedList(PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, getSamplePOIList(2),
//							   new PlacesPOIVariantSerializer());
//		eventData.putInteger(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS, PlacesRequestError.OK.getValue());
//		Event responseEvent = new Event.Builder("responseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//
//		// test
//		core.getNearbyPointsOfInterest(location, 20, successCallback, errorCallback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertTrue("SuccessCallback is called ", successCallbackCalled);
//		assertFalse("ErrorCallback is not called ", errorCallbackCalled);
//		assertEquals("the callback is called with correct pois", 2, responseNearByPois.size());
//	}
//
//	@Test
//	public void getNearbyPlaces_when_responseEvent_with_connectivityError_status() {
//		// setup
//
//		double latitude = 31.33;
//		double longitude = -121.34;
//		PlacesGpsLocation location = new PlacesGpsLocation(latitude, longitude);
//		EventData eventData = new EventData();
//		eventData.putTypedList(PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, getSamplePOIList(2),
//							   new PlacesPOIVariantSerializer());
//		eventData.putInteger(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS,
//							 PlacesRequestError.CONNECTIVITY_ERROR.getValue());
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//
//		// test
//		core.getNearbyPointsOfInterest(location, 20, successCallback, errorCallback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertFalse("SuccessCallback is not called ", successCallbackCalled);
//		assertTrue("ErrorCallback is called ", errorCallbackCalled);
//		assertEquals(PlacesRequestError.CONNECTIVITY_ERROR, placesRequestError);
//	}
//
//
//	@Test
//	public void getNearbyPlaces_when_responseEvent_with_inValidPOIS() {
//		// setup
//		double latitude = 31.33;
//		double longitude = -121.34;
//		PlacesGpsLocation location = new PlacesGpsLocation(latitude, longitude);
//		EventData eventData = new EventData();
//		eventData.putString(PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, "Invalid");
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		// test
//		core.getNearbyPointsOfInterest(location, 20, successCallback, errorCallback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertFalse("SuccessCallback is not called ", successCallbackCalled);
//		assertTrue("ErrorCallback is called ", errorCallbackCalled);
//		assertEquals(PlacesRequestError.UNKNOWN_ERROR, placesRequestError);
//	}
//
//	@Test
//	public void getNearbyPlaces_when_responseEvent_with_nullEventData() {
//		// setup
//		double latitude = 31.33;
//		double longitude = -121.34;
//		PlacesGpsLocation location = new PlacesGpsLocation(latitude, longitude);
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(null).build();
//
//		// test
//		core.getNearbyPointsOfInterest(location, 20, successCallback, errorCallback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertFalse("SuccessCallback is not called ", successCallbackCalled);
//		assertTrue("ErrorCallback is called ", errorCallbackCalled);
//		assertEquals(PlacesRequestError.UNKNOWN_ERROR, placesRequestError);
//	}
//
//
//	@Test
//	public void getNearbyPlaces_when_callbackIsNull() {
//		// setup
//		double latitude = 31.33;
//		double longitude = -121.34;
//		PlacesGpsLocation location = new PlacesGpsLocation(latitude, longitude);
//
//		// test
//		core.getNearbyPointsOfInterest(location, 20, null, null);
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertFalse("one-time listener is not registered for callback", eventHub.registerOneTimeListenerCalled);
//	}
//
//
//	@Test
//	public void getNearbyPlaces_happy_and_when_errorCallbackIsNull() {
//		// setup
//
//		double latitude = 31.33;
//		double longitude = -121.34;
//		PlacesGpsLocation location = new PlacesGpsLocation(latitude, longitude);
//		EventData eventData = new EventData();
//		eventData.putTypedList(PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, getSamplePOIList(2),
//							   new PlacesPOIVariantSerializer());
//		eventData.putInteger(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS, PlacesRequestError.OK.getValue());
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		// test
//		core.getNearbyPointsOfInterest(location, 20, successCallback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertTrue("SuccessCallback is called ", successCallbackCalled);
//		assertFalse("ErrorCallback is not called ", errorCallbackCalled);
//		assertEquals("the callback is called with correct pois", 2, responseNearByPois.size());
//	}
//
//	@Test
//	public void getUserWithinPointsOfInterest_should_dispatchPlacesRequestContentEvent() {
//		// setup
//		AdobeCallback<List<PlacesPOI>> callback = new AdobeCallback<List<PlacesPOI>>() {
//			@Override
//			public void call(List<PlacesPOI> values) {
//
//			}
//		};
//
//		// test
//		core.getUserWithinPointsOfInterest(callback);
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_GETUSERWITHINPLACES,
//					 eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.PLACES, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertNotNull("event has eventData", eventHub.dispatchedEvent.getData());
//
//
//		assertEquals("event has the correct request type",
//					 PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES,
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, ""));
//		assertTrue("one-time listener is registered for callback", eventHub.registerOneTimeListenerCalled);
//		assertNotNull("one-time listener has a callback block", eventHub.registerOneTimeListenerParamBlock);
//	}
//
//	@Test
//	public void getUserWithinPointsOfInterest_when_callbackIsNull() {
//		// setup
//		eventHub.isDispatchedCalled = false; // make sure you clear all the previously dispactched events
//
//		// test
//		core.getUserWithinPointsOfInterest(null);
//
//		// verify
//		assertFalse("event not is dispatched", eventHub.isDispatchedCalled);
//		assertFalse("one-time listener is not registered for callback", eventHub.registerOneTimeListenerCalled);
//	}
//
//
//	@Test
//	public void getUserWithinPointsOfInterest_when_responseEvent_with_validPOIs() {
//		// setup
//		EventData eventData = new EventData();
//		eventData.putTypedList(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS, getSamplePOIList(2),
//							   new PlacesPOIVariantSerializer());
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		AdobeCallback<List<PlacesPOI>> callback = new AdobeCallback<List<PlacesPOI>>() {
//			@Override
//			public void call(List<PlacesPOI> values) {
//				responseNearByPois = values;
//			}
//		};
//
//		// test
//		core.getUserWithinPointsOfInterest(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertEquals("the callback is called with correct pois", 2, responseNearByPois.size());
//	}
//
//	@Test
//	public void getUserWithinPointsOfInterest_when_responseEvent_with_invalidPOIS() {
//		// setup
//
//		EventData eventData = new EventData();
//		eventData.putString(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS, "Invalid");
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		AdobeCallback<List<PlacesPOI>> callback = new AdobeCallback<List<PlacesPOI>>() {
//			@Override
//			public void call(List<PlacesPOI> values) {
//				responseNearByPois = values;
//			}
//		};
//
//		// test
//		core.getUserWithinPointsOfInterest(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertEquals("the callback is called with empty pois", 0, responseNearByPois.size());
//	}
//
//	@Test
//	public void getUserWithinPointsOfInterest_when_responseEvent_with_nullEventData() {
//		// setup
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(null).build();
//		AdobeCallback<List<PlacesPOI>> callback = new AdobeCallback<List<PlacesPOI>>() {
//			@Override
//			public void call(List<PlacesPOI> values) {
//				responseNearByPois = values;
//			}
//		};
//
//		// test
//		core.getUserWithinPointsOfInterest(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertEquals("the callback is called with empty pois", 0, responseNearByPois.size());
//	}
//
//
//
//	@Test
//	public void getLastKnownLocation_should_dispatchPlacesRequestContentEvent() {
//		// setup
//		AdobeCallback<PlacesGpsLocation> callback = new AdobeCallback<PlacesGpsLocation>() {
//			@Override
//			public void call(PlacesGpsLocation values) {
//
//			}
//		};
//
//		// test
//		core.getLastKnownLocation(callback);
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_GETLASTKNOWNLOCATION,
//					 eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.PLACES, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertNotNull("event has eventData", eventHub.dispatchedEvent.getData());
//
//
//		assertEquals("event has the correct request type",
//					 PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION,
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, ""));
//		assertTrue("one-time listener is registered for callback", eventHub.registerOneTimeListenerCalled);
//		assertNotNull("one-time listener has a callback block", eventHub.registerOneTimeListenerParamBlock);
//	}
//
//	@Test
//	public void getLastKnownLocation_when_callbackIsNull() {
//		// setup
//		eventHub.isDispatchedCalled = false; // make sure you clear all the previously dispactched events
//
//		// test
//		core.getLastKnownLocation(null);
//
//		// verify
//		assertFalse("event not is dispatched", eventHub.isDispatchedCalled);
//		assertFalse("one-time listener is not registered for callback", eventHub.registerOneTimeListenerCalled);
//	}
//
//
//	@Test
//	public void getLastKnownLocation_when_reponseEvent_with_nullEventData() {
//		// setup
//		AdobeCallback<PlacesGpsLocation> callback = new AdobeCallback<PlacesGpsLocation>() {
//			@Override
//			public void call(PlacesGpsLocation value) {
//				obtainedLocation = value;
//			}
//		};
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES, EventSource.RESPONSE_CONTENT).build();
//
//		// test
//		core.getLastKnownLocation(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertNull("The callback is called with null location", obtainedLocation);
//	}
//
//	@Test
//	public void getLastKnownLocation_when_reponseEvent_with_InvalidLatLong() {
//		// setup
//		AdobeCallback<PlacesGpsLocation> callback = new AdobeCallback<PlacesGpsLocation>() {
//			@Override
//			public void call(PlacesGpsLocation value) {
//				obtainedLocation = value;
//			}
//		};
//
//		EventData eventData = new EventData();
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, -91);
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, 100);
//		Event responseEventInvalidLatitudeLow = new Event.Builder("reponseEvent", EventType.PLACES,
//				EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		// test
//		core.getLastKnownLocation(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEventInvalidLatitudeLow);
//		// verify
//		assertNull("The callback is called with null location", obtainedLocation);
//
//
//
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, 91);
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, 100);
//		Event responseEventInvalidLatitudeHigh = new Event.Builder("reponseEvent", EventType.PLACES,
//				EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		// test
//		core.getLastKnownLocation(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEventInvalidLatitudeHigh);
//		// verify
//		assertNull("The callback is called with null location", obtainedLocation);
//
//
//
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, 20);
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, 180.2);
//		Event responseEventInvalidLongitudeHigh = new Event.Builder("reponseEvent", EventType.PLACES,
//				EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		// test
//		core.getLastKnownLocation(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEventInvalidLongitudeHigh);
//		// verify
//		assertNull("The callback is called with null location", obtainedLocation);
//
//
//
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, 20);
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, -180.2);
//		Event responseEventInvalidLongitudeLow = new Event.Builder("reponseEvent", EventType.PLACES,
//				EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		// test
//		core.getLastKnownLocation(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEventInvalidLongitudeLow);
//		// verify
//		assertNull("The callback is called with null location", obtainedLocation);
//	}
//
//	@Test
//	public void getLastKnownLocation_when_responseEvent_with_validLatLong() {
//		// setup
//		AdobeCallback<PlacesGpsLocation> callback = new AdobeCallback<PlacesGpsLocation>() {
//			@Override
//			public void call(PlacesGpsLocation value) {
//				obtainedLocation = value;
//			}
//		};
//		EventData eventData = new EventData();
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, -55.55);
//		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, -44.44);
//		Event responseEvent = new Event.Builder("reponseEvent", EventType.PLACES,
//												EventSource.RESPONSE_CONTENT).setData(eventData).build();
//
//
//		// test
//		core.getLastKnownLocation(callback);
//		eventHub.registerOneTimeListenerParamBlock.call(responseEvent);
//
//		// verify
//		assertNotNull("The callback is called with valid location", obtainedLocation);
//		assertEquals("The callback is called with valid latitude", -55.55, obtainedLocation.getLatitude(), 0.0);
//		assertEquals("The callback is called with valid latitude", -44.44, obtainedLocation.getLongitude(), 0.0);
//	}
//
//
//	@Test
//	public void processRegionEvent_should_dispatchPlacesRequestContentEvent() {
//		// setup
//		List<EventData> placesRegions = new ArrayList<EventData>();
//		EventData eventData = new EventData();
//		eventData.putString(PlacesConstants.EventDataKeys.Places.REGION_ID, "fenceId");
//		eventData.putInteger(PlacesConstants.EventDataKeys.Places.REGION_EVENT_TYPE, 1);
//		placesRegions.add(eventData);
//
//		// test
//		core.processRegionEvent(placesRegions);
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_PROCESSREGIONEVENT,
//					 eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.PLACES, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertNotNull("event has eventData", eventHub.dispatchedEvent.getData());
//
//		assertEquals("event has the correct radius data", "fenceId",
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REGION_ID, null));
//		assertEquals("event has the correct places count", 1,
//					 eventHub.dispatchedEvent.getData().optInteger(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE, -1));
//		assertEquals("event has the correct request type",
//					 PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT,
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, ""));
//
//	}
//
//	@Test
//	public void setLocationPermissionStatus_should_dispatchRequestContentEvent() {
//		// test
//		core.setPlacesAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS);
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_SETAUTHORIZATIONSTATUS,
//					 eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.PLACES, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertNotNull("event has eventData", eventHub.dispatchedEvent.getData());
//		assertEquals("event has the correct request type",
//					 PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS,
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, ""));
//		assertEquals("event has the correct authorization status",
//					 PlacesAuthorizationStatus.ALWAYS.stringValue(),
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.AUTH_STATUS, ""));
//	}
//
//	@Test
//	public void setLocationPermissionStatus_withNullStatus_shouldNotDispatchEvent() {
//		// setup
//		eventHub.isDispatchedCalled = false; // make sure you clear all the previously dispactched events
//
//		// test
//		core.setPlacesAuthorizationStatus(null);
//
//		// verify
//		assertFalse("event is dispatched", eventHub.isDispatchedCalled);
//	}
//
//
//	@Test
//	public void reset_should_dispatchRequestContentEvent() {
//		// test
//		core.reset();
//
//		// verify
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", PlacesTestConstants.EventName.REQUEST_RESET, eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.PLACES, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertNotNull("event has eventData", eventHub.dispatchedEvent.getData());
//		assertEquals("event has the correct request type",
//					 PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_RESET,
//					 eventHub.dispatchedEvent.getData().optString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, ""));
//	}


	private List<PlacesPOI> getSamplePOIList(final int count) {
		List<PlacesPOI> pois = new ArrayList<PlacesPOI>();

		for (int i = 0 ; i < count; i++) {
			pois.add(new PlacesPOI("nearByPOI" + i, "hidden", 34.33, -121.55, 150, "libraryName", 2, null));
		}

		return pois;
	}


}
