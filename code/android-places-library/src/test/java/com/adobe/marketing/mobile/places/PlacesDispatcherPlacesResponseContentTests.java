/*
 *  ***********************************************************************
 *  * ADOBE CONFIDENTIAL
 *  * ___________________
 *  *
 *  * Copyright 2018 Adobe Systems Incorporated
 *  * All Rights Reserved.
 *  *
 *  * NOTICE:  All information contained herein is, and remains
 *  * the property of Adobe Systems Incorporated and its suppliers,
 *  * if any.  The intellectual and technical concepts contained
 *  * herein are proprietary to Adobe Systems Incorporated and its
 *  * suppliers and are protected by trade secret or copyright law.
 *  * Dissemination of this information or reproduction of this material
 *  * is strictly forbidden unless prior written permission is obtained
 *  * from Adobe Systems Incorporated.
 *  *************************************************************************
 */

package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PlacesDispatcherPlacesResponseContentTests extends BaseTest {
	private PlacesDispatcherPlacesResponseContent dispatcher;
	private MockPlacesExtension placesExtension;

	private static String SAMPLE_IDENTIFIER = "identifier";
	private static String SAMPLE_NAME = "poname";
	private static double SAMPLE_LATITUDE = 33.44;
	private static double SAMPLE_LONGITUDE = -55.66;
	private static int SAMPLE_RADIUS = 300;
	private static String SAMPLE_LIBRARY = "library";
	private static int SAMPLE_WEIGHT = 200;
	private static Map<String, String> SAMPLE_METADATA = new HashMap<String, String>() {
		{
			put("city", "pity");
			put("state", "ate");
		}
	};

	@Before
	public void testSetup() throws Exception {
		super.beforeEach();
		placesExtension = new MockPlacesExtension(eventHub, platformServices);
		dispatcher = new PlacesDispatcherPlacesResponseContent(eventHub, placesExtension);
	}

	@Test
	public void test_dispatchNearbyPlaces() throws Exception {
		List<PlacesPOI> pois = new ArrayList<PlacesPOI>();
		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));

		dispatcher.dispatchNearbyPlaces(pois, PlacesRequestError.OK, "pairId");
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETNEARBYPLACES, eventHub.dispatchedEvent.getName());
		assertEquals(EventType.PLACES, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
		assertEquals("pairId", eventHub.dispatchedEvent.getPairID());
		assertTrue(eventHub.dispatchedEvent.getEventData().containsKey(
					   PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST));
		assertEquals(PlacesRequestError.OK.getValue(), eventHub.dispatchedEvent.getData().getInteger(
						 PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS));
		assertEquals(2, eventHub.dispatchedEvent.getEventData().size());
	}

	@Test
	public void test_dispatchUserWithinPOIs() throws Exception {
		List<PlacesPOI> pois = new ArrayList<PlacesPOI>();
		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));
		pois.add(new PlacesPOI("identifier2", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));

		dispatcher.dispatchUserWithinPOIs(pois, "pairId");
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETUSERWITHINPLACES, eventHub.dispatchedEvent.getName());
		assertEquals(EventType.PLACES, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
		assertEquals("pairId", eventHub.dispatchedEvent.getPairID());
		EventData eventData = eventHub.dispatchedEvent.getData();
		assertTrue(eventData.containsKey(
					   PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS));
		assertEquals(1, eventData.size());
		assertEquals(2, eventData.getTypedList(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS,
											   new PlacesPOIVariantSerializer()).size());

	}

	@Test
	public void test_dispatchRegionEvent_When_NullRegion() {
		// setup
		eventHub.isDispatchedCalled = false; // make sure you clear all the previously dispactched events

		// test
		dispatcher.dispatchRegionEvent(null);

		// verify
		assertFalse(eventHub.isDispatchedCalled);
	}

	@Test
	public void test_dispatchRegionEvent() throws Exception {
		// setup
		PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									  SAMPLE_LIBRARY, SAMPLE_WEIGHT
									  , SAMPLE_METADATA);
		PlacesRegion region = new PlacesRegion(poi, PlacesRegion.PLACE_EVENT_ENTRY, 777777777) ;
		// test
		dispatcher.dispatchRegionEvent(region);

		// verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(PlacesTestConstants.EventName.RESPONSE_PROCESSREGIONEVENT, eventHub.dispatchedEvent.getName());
		assertEquals(EventType.PLACES, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
		assertEquals(3, eventHub.dispatchedEvent.getEventData().size());

		PlacesPOI triggeringPoi = eventHub.dispatchedEvent.getData().getTypedObject(
									  PlacesTestConstants.EventDataKeys.Places.TRIGGERING_REGION, new PlacesPOIVariantSerializer());
		assertTrue(poi.equals(triggeringPoi));
		assertEquals(eventHub.dispatchedEvent.getEventData().get(
						 PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE), PlacesRegion.PLACE_EVENT_ENTRY);
		assertEquals(eventHub.dispatchedEvent.getEventData().get(
						 PlacesTestConstants.EventDataKeys.Places.REGION_TIMESTAMP), Long.valueOf(777777777));
	}

	@Test
	public void test_dispatchRegionEvent_When_RegionWith_null_POI() throws Exception {
		// setup
		PlacesRegion region = new PlacesRegion(null, PlacesRegion.PLACE_EVENT_ENTRY, 777777777) ;
		// test
		dispatcher.dispatchRegionEvent(region);

		// verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(PlacesTestConstants.EventName.RESPONSE_PROCESSREGIONEVENT, eventHub.dispatchedEvent.getName());
		assertEquals(EventType.PLACES, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
		assertEquals(3, eventHub.dispatchedEvent.getEventData().size());
		assertNull(eventHub.dispatchedEvent.getData().getTypedObject(PlacesTestConstants.EventDataKeys.Places.TRIGGERING_REGION,
				   new PlacesPOIVariantSerializer()));
		assertEquals(eventHub.dispatchedEvent.getEventData().get(
						 PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE), PlacesRegion.PLACE_EVENT_ENTRY);
		assertEquals(eventHub.dispatchedEvent.getEventData().get(
						 PlacesTestConstants.EventDataKeys.Places.REGION_TIMESTAMP), Long.valueOf(777777777));
	}

	@Test
	public void test_dispatchLastKnownLocation() throws Exception {
		// test
		dispatcher.dispatchLastKnownLocation(31.33, -121.33, "pairID");

		// verify
		assertTrue(eventHub.isDispatchedCalled);
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETLASTKNOWNLOCATION, eventHub.dispatchedEvent.getName());
		assertEquals(EventType.PLACES, eventHub.dispatchedEvent.getEventType());
		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
		assertEquals(2, eventHub.dispatchedEvent.getEventData().size());
		assertEquals(31.33, eventHub.dispatchedEvent.getData().getDouble(
						 PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE), 0.0);
		assertEquals(-121.33, eventHub.dispatchedEvent.getData().getDouble(
						 PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE), 0.0);
		assertEquals("pairID", eventHub.dispatchedEvent.getPairID());
	}
}
