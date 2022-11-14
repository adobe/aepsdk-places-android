///*
// *  ***********************************************************************
// *  * ADOBE CONFIDENTIAL
// *  * ___________________
// *  *
// *  * Copyright 2018 Adobe Systems Incorporated
// *  * All Rights Reserved.
// *  *
// *  * NOTICE:  All information contained herein is, and remains
// *  * the property of Adobe Systems Incorporated and its suppliers,
// *  * if any.  The intellectual and technical concepts contained
// *  * herein are proprietary to Adobe Systems Incorporated and its
// *  * suppliers and are protected by trade secret or copyright law.
// *  * Dissemination of this information or reproduction of this material
// *  * is strictly forbidden unless prior written permission is obtained
// *  * from Adobe Systems Incorporated.
// *  *************************************************************************
// */
//
//package com.adobe.marketing.mobile.places;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
//public class PlacesExtensionTests extends BaseTest {
//
//	static private final long SAMPLE_TTL = 8990;
//
//	private TestablePlacesExtension placesExtension;
//	private MockPlacesState mockPlacesState;
//	private MockPlacesDispatcher mockPlacesDispatcher;
//	private MockPlacesQueryService mockPlacesQueryService;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		Log.setLogLevel(LoggingMode.VERBOSE);
//		mockPlacesState = new MockPlacesState(null);
//		mockPlacesDispatcher = new MockPlacesDispatcher(eventHub, placesExtension);
//		placesExtension = new TestablePlacesExtension(eventHub, platformServices);
//		mockPlacesQueryService = new MockPlacesQueryService(platformServices.getJsonUtilityService(),
//				platformServices.getNetworkService());
//
//		placesExtension.placesDispatcherPlacesResponseContent = mockPlacesDispatcher;
//		placesExtension.state = mockPlacesState;
//		placesExtension.mockPlacesQueryService = mockPlacesQueryService;
//
//	}
//
//	// ========================================================================================
//	// handleConfigurationEvent
//	// ========================================================================================
//	@Test
//	public void handleConfigurationEvent_WhenPrivacyOptIn() {
//		// setup
//		setConfigurationSharedState("optedin");
//
//		// test
//		placesExtension.handleConfigurationEvent(emptyEvent());
//
//		// verify
//		assertFalse(mockPlacesState.clearDataOutCalled);
//	}
//
//
//	@Test
//	public void handleConfigurationEvent_WhenPrivacyOptOut() {
//		// pre-setup test
//		placesExtension.queueEvent(testGetNearByPOIEvent());
//		placesExtension.queueEvent(testGetNearByPOIEvent());
//		assertEquals(2, placesExtension.getEventQueue().size());
//
//		// setup
//		setConfigurationSharedState("optedout");
//
//		// test
//		placesExtension.handleConfigurationEvent(emptyEvent());
//
//		// verify
//		assertTrue(mockPlacesState.clearDataOutCalled);
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void handleConfigurationEvent_WhenPrivacyUnknown() {
//		// setup
//		setConfigurationSharedState("unknown");
//
//		// test
//		placesExtension.handleConfigurationEvent(emptyEvent());
//
//		// verify
//		assertFalse(mockPlacesState.clearDataOutCalled);
//	}
//
//	@Test
//	public void handleConfigurationEvent_NoConfiguration() {
//
//		// test
//		placesExtension.handleConfigurationEvent(emptyEvent());
//
//		// verify
//		assertFalse(mockPlacesState.clearDataOutCalled);
//	}
//
//
//	// ========================================================================================
//	// queueEvent
//	// ========================================================================================
//
//	@Test
//	public void queue_NullEvent() {
//		// test
//		placesExtension.queueEvent(null);
//
//		// verify
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//
//	@Test
//	public void queue_Event_with_EmptyEventData() {
//		// test
//		placesExtension.queueEvent(emptyEvent());
//
//		// verify that the event is not waiting in the queue
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void queue_RandomEvent() {
//		// test
//		setConfigurationSharedState("optedin");
//		placesExtension.queueEvent(randomEvent());
//
//		// verify
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	// ========================================================================================
//	// getNearByPlaceEvent
//	// ========================================================================================
//
//	@Test
//	public void queue_getNearByPlaceEvent_when_NoConfiguration() {
//		// test
//		placesExtension.queueEvent(testGeofenceEvent());
//
//		// verify
//		assertFalse(mockPlacesQueryService.getNearByPlacesWasCalled);
//		assertFalse(mockPlacesDispatcher.dispatchNearbyPlacesWasCalled);
//		assertEquals(1, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void queue_getNearByPlaceEvent_when_InvalidConfiguration() {
//		// test
//		setInvalidConfigurationSharedState();
//		placesExtension.queueEvent(testGetNearByPOIEvent());
//
//		// verify
//		assertFalse(mockPlacesQueryService.getNearByPlacesWasCalled);
//		assertTrue(mockPlacesDispatcher.dispatchNearbyPlacesWasCalled);
//		assertEquals(1, mockPlacesDispatcher.dispatchNearbyPlacesWasCalledCount);
//		assertEquals(0, mockPlacesDispatcher.dispatchNearbyPlacesParamPoiList.get(0).size());
//		assertEquals(PlacesRequestError.CONFIGURATION_ERROR,
//					 mockPlacesDispatcher.dispatchNearbyPlacesParamResponseStatus.get(0));
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void queue_getNearByPlaceEvent_when_privacyOptOut() {
//		// setup
//		setConfigurationSharedState("optedout");
//
//		// test
//		placesExtension.queueEvent(testGetNearByPOIEvent());
//
//		// verify
//		assertFalse(mockPlacesQueryService.getNearByPlacesWasCalled);
//		assertTrue(mockPlacesDispatcher.dispatchNearbyPlacesWasCalled);
//		assertEquals(1, mockPlacesDispatcher.dispatchNearbyPlacesWasCalledCount);
//		assertEquals(PlacesRequestError.PRIVACY_OPTED_OUT, mockPlacesDispatcher.dispatchNearbyPlacesParamResponseStatus.get(0));
//		assertEquals(0, mockPlacesDispatcher.dispatchNearbyPlacesParamPoiList.get(0).size());
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void queue_getNearByPlaceEvent_when_queryServicesNull() {
//		// setup
//		setConfigurationSharedState("optedin");
//		placesExtension.mockPlacesQueryService = null;
//
//		// test
//		placesExtension.queueEvent(testGetNearByPOIEvent());
//
//		// verify
//		assertFalse(mockPlacesQueryService.getNearByPlacesWasCalled);
//		assertTrue(mockPlacesDispatcher.dispatchNearbyPlacesWasCalled);
//		assertEquals(1, mockPlacesDispatcher.dispatchNearbyPlacesWasCalledCount);
//		assertEquals(PlacesRequestError.QUERY_SERVICE_UNAVAILABLE,
//					 mockPlacesDispatcher.dispatchNearbyPlacesParamResponseStatus.get(0));
//		assertEquals(0, mockPlacesDispatcher.dispatchNearbyPlacesParamPoiList.get(0).size());
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//
//	@Test
//	public void queue_getNearByPlaceEvent_when_queryServiceResponseIsNotASuccess() {
//		// setup
//		setConfigurationSharedState("optedin");
//		PlacesQueryResponse failedResponse = new PlacesQueryResponse();
//		failedResponse.fetchFailed("", PlacesRequestError.SERVER_RESPONSE_ERROR);
//		mockPlacesQueryService.mockedResponse = failedResponse;
//
//
//		// test
//		placesExtension.queueEvent(testGetNearByPOIEvent());
//
//		// verify
//		assertTrue(mockPlacesQueryService.getNearByPlacesWasCalled);
//		assertEquals(1, mockPlacesDispatcher.dispatchNearbyPlacesWasCalledCount);
//		assertTrue(mockPlacesDispatcher.dispatchNearbyPlacesWasCalled);
//		assertEquals(PlacesRequestError.SERVER_RESPONSE_ERROR,
//					 mockPlacesDispatcher.dispatchNearbyPlacesParamResponseStatus.get(0));
//		assertEquals(0, mockPlacesDispatcher.dispatchNearbyPlacesParamPoiList.get(0).size());
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//
//	@Test
//	public void queue_getNearByPlaceEvent_happy() {
//		// setup
//		setConfigurationSharedState("optedin");
//
//		// setup placesState
//		EventData placesState = new EventData();
//		placesState.putString("key", "value");
//		mockPlacesState.getPlacesSharedStateReturnValue = placesState;
//
//		PlacesQueryResponse testqueryResponse = createSuccessQueryResponse();
//		mockPlacesQueryService.mockedResponse = testqueryResponse;
//
//		// setup region events
//		final PlacesRegion region1 = new PlacesRegion(createPOI("poi1"), "entry", 100);
//		List<PlacesRegion> regionEvents = new ArrayList<PlacesRegion>() {
//			{
//				add(region1);
//			}
//		};
//
//
//
//		// test
//		Event event = testGetNearByPOIEvent();
//		placesExtension.queueEvent(event);
//
//		// verify interaction with QueryService
//		assertTrue(mockPlacesQueryService.getNearByPlacesWasCalled);
//
//		// verify interaction with placesState
//		assertTrue(mockPlacesState.processNetworkResponseWasCalled);
//		assertEquals(testqueryResponse, mockPlacesState.processNetworkResponseParamResponse);
//		assertEquals(event, mockPlacesState.processNetworkResponseParamEvent);
//		assertEquals(SAMPLE_TTL, mockPlacesState.membershipTtl);
//
//		// verify shared state creation
//		assertTrue(eventHub.createOrUpdateSharedStateWithVersionCalled);
//		assertEquals(event.getEventNumber(), eventHub.createOrUpdateSharedStateWithVersionParamVersion);
//		assertEquals(placesState, eventHub.createOrUpdateSharedStateWithVersionParamState);
//
//		// verify interactions with dispatcher
//		assertTrue(mockPlacesDispatcher.dispatchNearbyPlacesWasCalled);
//		assertEquals(2, mockPlacesDispatcher.dispatchNearbyPlacesWasCalledCount);
//
//		// verify if the OTL is called with correct data
//		assertEquals(4, mockPlacesDispatcher.dispatchNearbyPlacesParamPoiList.get(0).size());
//		assertEquals(PlacesRequestError.OK, mockPlacesDispatcher.dispatchNearbyPlacesParamResponseStatus.get(0));
//		assertEquals(event.getResponsePairID(), mockPlacesDispatcher.dispatchNearbyPlacesParamPairId.get(0));
//
//		// verify the generic event dispatched
//		assertEquals(PlacesRequestError.OK, mockPlacesDispatcher.dispatchNearbyPlacesParamResponseStatus.get(1));
//		assertEquals(4, mockPlacesDispatcher.dispatchNearbyPlacesParamPoiList.get(1).size());
//		assertEquals(null, mockPlacesDispatcher.dispatchNearbyPlacesParamPairId.get(1));
//
//
//
//		// assert that the event is removed from the queue
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void handleGetNearByPlaceEvent_NoRegionEventToDispatch() {
//		// setup
//		setConfigurationSharedState("optedin");
//
//		// setup placesState
//		EventData placesState = new EventData();
//		mockPlacesState.getPlacesSharedStateReturnValue = placesState;
//
//		PlacesQueryResponse testqueryResponse = createSuccessQueryResponse();
//		mockPlacesQueryService.mockedResponse = testqueryResponse;
//
//		// test
//		Event event = testGetNearByPOIEvent();
//		placesExtension.queueEvent(event);
//
//		// verify interactions with dispatcher for regionEvent
//		assertFalse(mockPlacesDispatcher.dispatchRegionEventWasCalled);
//
//		// assert that the event is removed from the queue
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//
//
//	// ========================================================================================
//	// handleGeofenceEvent
//	// ========================================================================================
//
//	@Test
//	public void queue_handleGeofenceEvent_when_NoConfiguration() {
//		// setup
//		PlacesRegion region = new PlacesRegion(createPOI("poi"), "entry", 100);
//		EventData placesState = new EventData();
//		placesState.putString("key", "value");
//		eventHub.createOrUpdateSharedStateWithVersionCalled = false; // make sure you clear all the previous sharedStateUpdates
//
//		mockPlacesState.processRegionEventReturnValue = region;
//		mockPlacesState.getPlacesSharedStateReturnValue = placesState;
//
//		// test
//		placesExtension.queueEvent(testGeofenceEvent());
//
//		// verify
//		assertFalse(mockPlacesState.processRegionEventWasCalled);
//		assertFalse(eventHub.createOrUpdateSharedStateWithVersionCalled);
//		assertFalse(mockPlacesDispatcher.dispatchRegionEventWasCalled);
//		assertEquals(1, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void queue_handleGeofenceEvent_when_privacyOptedOut() {
//		// setup
//		eventHub.createOrUpdateSharedStateWithVersionCalled = false; // make sure to clear all the previous sharedState updates
//		setConfigurationSharedState("optedout");
//		PlacesRegion region = new PlacesRegion(createPOI("poi"), "entry", 100);
//		EventData placesState = new EventData();
//		placesState.putString("key", "value");
//
//		mockPlacesState.processRegionEventReturnValue = region;
//		mockPlacesState.getPlacesSharedStateReturnValue = placesState;
//
//		// test
//		placesExtension.queueEvent(testGeofenceEvent());
//
//		// verify
//		// verify
//		assertFalse(mockPlacesState.processRegionEventWasCalled);
//		assertFalse(eventHub.createOrUpdateSharedStateWithVersionCalled);
//		assertFalse(mockPlacesDispatcher.dispatchRegionEventWasCalled);
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	@Test
//	public void queue_handleGeofenceEvent_Happy() {
//		// setup
//		setConfigurationSharedState("optedin");
//		PlacesRegion region = new PlacesRegion(createPOI("poi"), "entry", 100);
//		EventData placesState = new EventData();
//		placesState.putString("key", "value");
//
//		mockPlacesState.processRegionEventReturnValue = region;
//		mockPlacesState.getPlacesSharedStateReturnValue = placesState;
//
//		// test
//		placesExtension.queueEvent(testGeofenceEvent());
//
//		// verify
//		assertTrue(mockPlacesState.processRegionEventWasCalled);
//		assertEquals(SAMPLE_TTL, mockPlacesState.membershipTtl);
//
//		assertTrue(eventHub.createOrUpdateSharedStateWithVersionCalled);
//		assertEquals(emptyEvent().getEventNumber(), eventHub.createOrUpdateSharedStateWithVersionParamVersion);
//		assertEquals(placesState, eventHub.createOrUpdateSharedStateWithVersionParamState);
//
//		assertTrue(mockPlacesDispatcher.dispatchRegionEventWasCalled);
//		assertEquals(region, mockPlacesDispatcher.dispatchRegionEventParamRegion);
//		assertEquals(0, placesExtension.getEventQueue().size());
//	}
//
//	// ========================================================================================
//	// handleGetUserWithinPOIsEvent
//	// ========================================================================================
//	@Test
//	public void handleGetUserWithinPOIsEvent_Happy() {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES);
//
//		Event testEvent = new Event.Builder("Get UserWithin POIs event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).build();
//		ArrayList<PlacesPOI> pois = new ArrayList<PlacesPOI>();
//		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, "libraryName", 2, null));
//		pois.add(new PlacesPOI("identifier2", "name", 34.33, -121.55, 50, "libraryName", 2, null));
//		mockPlacesState.getUserWithInPOIsReturnValue =  pois;
//
//		// test
//		placesExtension.handleGetUserWithinPOIsEvent(testEvent);
//
//		// verify
//		assertTrue(mockPlacesState.getUserWithInPOIsWasCalled);
//
//		// verify the interaction with the dispatcher
//		assertTrue(mockPlacesDispatcher.dispatchUserWithinPOIsCalled);
//		assertEquals(2, mockPlacesDispatcher.dispatchUserWithinPOIsCallCount);
//
//		// verify the event is dispatched to awaiting one time listener
//		assertEquals(pois, mockPlacesDispatcher.dispatchUserWithinPOIsEventParamPoiList.get(0));
//		assertEquals(testEvent.getResponsePairID(), mockPlacesDispatcher.dispatchUserWithinPOIsEventParamEventPaidId.get(0));
//
//		// verify generic event is dispatched to eventHub
//		assertEquals(pois, mockPlacesDispatcher.dispatchUserWithinPOIsEventParamPoiList.get(1));
//		assertEquals(null, mockPlacesDispatcher.dispatchUserWithinPOIsEventParamEventPaidId.get(1));
//	}
//
//
//
//	// ========================================================================================
//	// handleGetLastKnownLocation
//	// ========================================================================================
//	@Test
//	public void handleGetLastKnownLocation_Happy() {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);
//
//		Event testEvent = new Event.Builder("Get Last known location", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).setResponsePairID("pairid").build();
//		PlacesGpsLocation gpsLocation = new PlacesGpsLocation(33.33, 44.44);
//		mockPlacesState.loadLastKnownLocationReturnValue =  gpsLocation;
//
//		// test
//		placesExtension.handleGetLastKnownLocation(testEvent);
//
//		// verify the interaction with dispatcher
//		assertTrue(mockPlacesDispatcher.dispatchLastKnownLocationWasCalled);
//		assertEquals(2, mockPlacesDispatcher.dispatchLastKnownLocationWasCallCount);
//
//		// verify the event dispatch to one time listener
//		assertEquals(33.33, mockPlacesDispatcher.dispatchLastKnownLocationParamLatitude.get(0), 0);
//		assertEquals(44.44, mockPlacesDispatcher.dispatchLastKnownLocationParamLongitude.get(0), 0);
//		assertEquals(testEvent.getResponsePairID(), mockPlacesDispatcher.dispatchLastKnownLocationParamPairID.get(0));
//
//		// verify the generic event dispatched
//		assertEquals(33.33, mockPlacesDispatcher.dispatchLastKnownLocationParamLatitude.get(1), 0);
//		assertEquals(44.44, mockPlacesDispatcher.dispatchLastKnownLocationParamLongitude.get(1), 0);
//		assertEquals(null, mockPlacesDispatcher.dispatchLastKnownLocationParamPairID.get(1));
//	}
//
//	@Test
//	public void handleGetLastKnownLocation_When_NullLocationReturned() {
//		// setup
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);
//
//		Event testEvent = new Event.Builder("Get Last known location", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).setResponsePairID("pairid").build();
//		mockPlacesState.loadLastKnownLocationReturnValue =  null;
//
//		// test
//		placesExtension.handleGetLastKnownLocation(testEvent);
//
//		// verify
//		assertTrue(mockPlacesDispatcher.dispatchLastKnownLocationWasCalled);
//		assertEquals(1, mockPlacesDispatcher.dispatchLastKnownLocationWasCallCount);
//		assertEquals(PlacesTestConstants.INVALID_LAT_LON, mockPlacesDispatcher.dispatchLastKnownLocationParamLatitude.get(0),
//					 0);
//		assertEquals(PlacesTestConstants.INVALID_LAT_LON, mockPlacesDispatcher.dispatchLastKnownLocationParamLongitude.get(0),
//					 0);
//		assertEquals(testEvent.getResponsePairID(), mockPlacesDispatcher.dispatchLastKnownLocationParamPairID.get(0));
//	}
//
//	// ========================================================================================
//	// handleSetAuthorizationStatusEvent
//	// ========================================================================================
//	@Test
//	public void handleSetLocationPermissionStatusEvent_Happy() {
//		// test
//		placesExtension.handleSetAuthorizationStatusEvent(testSetLocationPermissionStatusEvent(
//					PlacesAuthorizationStatus.ALWAYS.stringValue()));
//
//		// verify
//
//		assertTrue(mockPlacesState.saveLocationPermissionStatusWasCalled);
//		assertTrue(eventHub.createOrUpdateSharedStateWithVersionCalled);
//		assertEquals(PlacesAuthorizationStatus.ALWAYS.stringValue(),
//					 mockPlacesState.saveLocationPermissionStatusParamStatus);
//	}
//
//	@Test
//	public void handleSetLocationPermissionStatusEvent_InvalidStatusValue() {
//		// setup
//		eventHub.createOrUpdateSharedStateWithVersionCalled = false; // make sure to clear all the previous sharedState updates
//
//		// test
//		placesExtension.handleSetAuthorizationStatusEvent(testSetLocationPermissionStatusEvent("invalid"));
//
//		// verify
//		assertFalse(mockPlacesState.saveLocationPermissionStatusWasCalled);
//		assertFalse(eventHub.createOrUpdateSharedStateWithVersionCalled);
//	}
//
//
//	// ========================================================================================
//	// saveLastKnownLocation
//	// ========================================================================================
//	@Test
//	public void saveLastKnownLocation_Happy() {
//		// test
//		placesExtension.saveLastKnownLocation(testGetNearByPOIEvent());
//
//		// verify
//		assertTrue(mockPlacesState.saveLastKnownLocationWasCalled);
//		assertEquals(34.33, mockPlacesState.saveLastKnownLocationParamLatitude, 0);
//		assertEquals(-124.33, mockPlacesState.saveLastKnownLocationParamLongitude, 0);
//	}
//
//	@Test
//	public void saveLastKnownLocation_When_EmptyEventData() {
//		Event testEvent = new Event.Builder("Get Last known location", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setResponsePairID("pairid").build();
//
//		// test
//		placesExtension.saveLastKnownLocation(testEvent);
//
//		// verify
//		assertFalse(mockPlacesState.saveLastKnownLocationWasCalled);
//	}
//
//	@Test
//	public void saveLastKnownLocation_When_privacyOptedOut() {
//		setConfigurationSharedState("optedout");
//
//		// test
//		placesExtension.saveLastKnownLocation(testGetNearByPOIEvent());
//
//		// verify
//		assertFalse(mockPlacesState.saveLastKnownLocationWasCalled);
//	}
//
//	@Test
//	public void saveLastKnownLocation_When_InvalidLocation() {
//		EventData data = new EventData();
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 44.2);
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, -2124.33);
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES);
//
//		Event testEvent = new Event.Builder("Get nearby places event", EventType.PLACES,
//											EventSource.REQUEST_CONTENT).setData(data).setEventNumber(5).build();
//
//		// test
//		placesExtension.saveLastKnownLocation(testEvent);
//
//		// verify
//		assertFalse(mockPlacesState.saveLastKnownLocationWasCalled);
//	}
//
//
//
//	// ========================================================================================
//	// getPlacesQueryService
//	// ========================================================================================
//	@Test
//	public void getPlacesQueryService_Happy() {
//		// test
//		assertNotNull(placesExtension.testableGetPlacesQueryService());
//	}
//
//	@Test
//	public void getPlacesQueryService_NullJsonUtilityService() {
//		// setup
//		platformServices.fakeJsonUtilityService = null;
//		placesExtension = new TestablePlacesExtension(eventHub, platformServices);
//
//		// test
//		assertNull(placesExtension.testableGetPlacesQueryService());
//	}
//
//
//	@Test
//	public void getPlacesQueryService_NullNetworkService() {
//		// setup
//		platformServices.mockNetworkService = null;
//		placesExtension = new TestablePlacesExtension(eventHub, platformServices);
//
//		// test
//		assertNull(placesExtension.testableGetPlacesQueryService());
//	}
//
//	@Test
//	public void getPlacesQueryService_nullPlatformService() {
//		// setup
//
//		placesExtension = new TestablePlacesExtension(eventHub, null);
//
//		// test
//		assertNull(placesExtension.testableGetPlacesQueryService());
//	}
//
//
//	// ========================================================================================
//	// Helper methods
//	// ========================================================================================
//	EventData setConfigurationSharedState(final String privacyStatus) {
//		EventData configData = new EventData();
//		configData.putString(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY, privacyStatus);
//		List<Variant> libraries = new ArrayList<Variant>();
//
//		for (int i = 0; i < 2; i++) {
//			Map<String, String> library = new HashMap<String, String>();
//			library.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "lib" + (i + 1));
//			libraries.add(Variant.fromStringMap(library));
//		}
//
//		configData.putVariantList(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, libraries);
//		configData.putString(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, "endpoint");
//		configData.putLong(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL, SAMPLE_TTL);
//
//		eventHub.createAndUpdateSharedState("com.adobe.module.configuration", 3, configData);
//		return configData;
//	}
//
//	EventData setInvalidConfigurationSharedState() {
//		// Configuration shared state with no places properties
//		EventData configData = new EventData();
//		configData.putString(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY, "optedin");
//		eventHub.createAndUpdateSharedState("com.adobe.module.configuration", 3, configData);
//		return configData;
//	}
//
//	private Event emptyEvent() {
//		Event event = new Event.Builder("emptyEvent", EventType.PLACES, EventSource.RESPONSE_CONTENT).build();
//		event.setEventNumber(5);
//		return event;
//	}
//
//	private Event randomEvent() {
//		final EventData eventData = new EventData();
//		eventData.putString("randomKey", "randomValue");
//		Event event = new Event.Builder("randomEvent", EventType.PLACES,
//										EventSource.RESPONSE_CONTENT).setData(eventData).build();
//		event.setEventNumber(5);
//		return event;
//	}
//
//	private Event testGetNearByPOIEvent() {
//		EventData data = new EventData();
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, 34.33);
//		data.putDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, -124.33);
//		data.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, 20);
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES);
//
//		return new Event.Builder("Get nearby places event", EventType.PLACES,
//								 EventSource.REQUEST_CONTENT).setData(data).setEventNumber(5).build();
//	}
//
//	private Event testSetLocationPermissionStatusEvent(final String status) {
//
//		EventData eventData = new EventData();
//		eventData.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
//							PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS);
//		eventData.putString(PlacesConstants.EventDataKeys.Places.AUTH_STATUS, status);
//
//		return new Event.Builder(PlacesConstants.EventName.REQUEST_SETAUTHORIZATIONSTATUS, EventType.PLACES,
//								 EventSource.REQUEST_CONTENT)
//			   .setData(eventData)
//			   .build();
//	}
//
//	private Event testGeofenceEvent() {
//		EventData data = new EventData();
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REGION_ID, "regionID");
//		data.putInteger(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE, 1);
//		data.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//					   PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT);
//
//		return new Event.Builder("Geofence event", EventType.PLACES,
//								 EventSource.REQUEST_CONTENT).setData(data).setEventNumber(5).build();
//	}
//
//
//
//
//	private PlacesPOI createPOI(final String id) {
//		return new PlacesPOI(id, "hidden", 34.33, -121.55, 150, "libraryName", 22, null);
//	}
//
//	private PlacesQueryResponse createSuccessQueryResponse() {
//		PlacesQueryResponse response = new PlacesQueryResponse();
//		response.containsUserPOIs = new ArrayList<PlacesPOI>() {
//			{
//				add(createPOI("poi1"));
//				add(createPOI("poi2"));
//			}
//		};
//		response.nearByPOIs = new ArrayList<PlacesPOI>() {
//			{
//				add(createPOI("poi3"));
//				add(createPOI("poi4"));
//			}
//		};
//		response.isSuccess = true;
//		return response;
//	}
//}
