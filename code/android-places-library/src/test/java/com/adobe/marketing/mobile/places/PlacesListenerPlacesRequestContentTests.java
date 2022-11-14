///* **************************************************************************
// *
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe Inc.
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Inc. and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Inc. and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Inc..
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class PlacesListenerPlacesRequestContentTests extends BaseTest {
//	private PlacesListenerPlacesRequestContent testListener;
//	private MockPlacesExtension placesExtension;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		placesExtension = new MockPlacesExtension(eventHub, platformServices);
//		testListener = new PlacesListenerPlacesRequestContent(placesExtension, EventType.PLACES, EventSource.REQUEST_CONTENT);
//	}
//
//	@Test
//	public void testListener_NullEvent() throws Exception {
//		// test
//		testListener.hear(null);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify
//		assertFalse(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_NullEventData() throws Exception {
//		// setup
//		Event testEvent = new Event.Builder("places Event with null event data", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(null).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify
//		assertFalse(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_EmptyEventData() throws Exception {
//		// setup
//		Event testEvent = new Event.Builder("places Event with empty event data", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertFalse(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_GeoFenceEvent() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REGION_ID, "regionID");
//		data.putInteger(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE, 1);
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT);
//
//		Event testEvent = new Event.Builder("Geofence event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertTrue(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_getNearByPlacesEvent() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 34.33);
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, -124.33);
//		data.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, 20);
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES);
//
//		Event testEvent = new Event.Builder("Get nearby places event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertTrue(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertTrue(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//
//		// verify parameters passed
//		assertEquals(testEvent, placesExtension.queueEventParamEvent);
//	}
//
//	@Test
//	public void testListener_getUserWithinPOIsEvent() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES);
//
//		Event testEvent = new Event.Builder("Get UserWithin POIs event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertTrue(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//
//		// verify parameters passed
//		assertEquals(testEvent, placesExtension.handleGetUserWithinPOIsEventParamEvent);
//
//	}
//
//	@Test
//	public void testListener_getLastKnownLocationEvent() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);
//
//		Event testEvent = new Event.Builder("Get last known location event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertFalse(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertTrue(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//
//		// verify parameters passed
//		assertEquals(testEvent, placesExtension.handleGetLastKnownLocationParamEvent);
//	}
//
//	@Test
//	public void testListener_setLocationPermissionStatusEvent() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS);
//
//		Event testEvent = new Event.Builder("Requesting to set authorization status", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertFalse(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertTrue(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_resetEvent() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_RESET);
//
//		Event testEvent = new Event.Builder("Requesting to reset places data", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify method calls
//		assertFalse(placesExtension.isQueueEventCalled);
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertTrue(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_NoRequestType() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, -124.33);
//		data.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, 20);
//
//		Event testEvent = new Event.Builder("Get nearby places event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//
//	@Test
//	public void testListener_InvalidRequestType() throws Exception {
//		// setup
//		EventData data = new EventData();
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 34.33);
//		data.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, 20);
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE, "Invalid type");
//
//		Event testEvent = new Event.Builder("Get nearby places event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//
//		// test
//		testListener.hear(testEvent);
//		waitForExecutor(placesExtension.getExecutor());
//
//		// verify
//		assertFalse(placesExtension.isHandleConfigurationEventCalled);
//		assertFalse(placesExtension.isHandleGetUserWithinPOIsEventCalled);
//		assertFalse(placesExtension.handleGetLastKnownLocationEventCalled);
//		assertFalse(placesExtension.saveLastKnownLocationWasCalled);
//		assertFalse(placesExtension.ishandleSetLocationPermissionStatusEventCalled);
//		assertFalse(placesExtension.resetWasCalled);
//	}
//}