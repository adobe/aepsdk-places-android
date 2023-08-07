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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlacesDispatcherTests {

	@Mock
	private ExtensionApi extensionApi;

	private PlacesDispatcher placesDispatcher;

	@Before
	public void testSetup() {
		placesDispatcher = new PlacesDispatcher(extensionApi);
		reset(extensionApi);
	}

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
	private static String SAMPLE_DATASET_ID = "1234";
	private static Event triggerEvent = new Event.Builder("Test event", EventType.PLACES,
			EventSource.REQUEST_CONTENT).setEventData(null).build();

	private ArgumentCaptor<Event> dispatchedEventCaptor = ArgumentCaptor.forClass(Event.class);

	@Test
	public void test_dispatchNearbyPlaces() {
		List<PlacesPOI> pois = new ArrayList<>();
		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));

		// test
		final Event triggeringEvent = triggerEvent;
		placesDispatcher.dispatchNearbyPlaces(pois, PlacesRequestError.OK, triggeringEvent);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify dispatchedEvent
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETNEARBYPLACES, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		assertTrue(dispatchedEvent.getEventData().containsKey(
					   PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST));
		assertEquals(PlacesRequestError.OK.getValue(), (int)dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS));
		assertEquals(2, dispatchedEvent.getEventData().size());
		assertEquals(triggeringEvent.getUniqueIdentifier(), dispatchedEvent.getResponseID());
	}

	@Test
	public void test_dispatchNearbyPlaces_withNullEvent() {
		List<PlacesPOI> pois = new ArrayList<>();
		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));

		// test
		placesDispatcher.dispatchNearbyPlaces(pois, PlacesRequestError.OK, null);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify dispatchedEvent
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETNEARBYPLACES, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		assertTrue(dispatchedEvent.getEventData().containsKey(
				PlacesTestConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST));
		assertEquals(PlacesRequestError.OK.getValue(), (int)dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.RESULT_STATUS));
		assertEquals(2, dispatchedEvent.getEventData().size());
		assertNull(dispatchedEvent.getResponseID());
	}

	@Test
	public void test_dispatchUserWithinPOIs() {
		List<PlacesPOI> pois = new ArrayList<PlacesPOI>();
		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));
		pois.add(new PlacesPOI("identifier2", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));

		// test
		final Event triggeringEvent = triggerEvent;
		placesDispatcher.dispatchUserWithinPOIs(pois, triggeringEvent);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify dispatchedEvent
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETUSERWITHINPLACES, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		Map<String, Object> eventData = dispatchedEvent.getEventData();
		assertTrue(eventData.containsKey(
					   PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS));
		assertEquals(1, eventData.size());
		assertEquals(2, ((Collection<?>) eventData.get(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS)).size());
		assertEquals(triggeringEvent.getUniqueIdentifier(), dispatchedEvent.getResponseID());
	}

	@Test
	public void test_dispatchUserWithinPOIs_withNullEvent() {
		List<PlacesPOI> pois = new ArrayList<PlacesPOI>();
		pois.add(new PlacesPOI("identifier", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));
		pois.add(new PlacesPOI("identifier2", "name", 34.33, -121.55, 50, SAMPLE_LIBRARY, SAMPLE_WEIGHT, null));

		// test
		placesDispatcher.dispatchUserWithinPOIs(pois, null);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify dispatchedEvent
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETUSERWITHINPLACES, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		Map<String, Object> eventData = dispatchedEvent.getEventData();
		assertTrue(eventData.containsKey(
				PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS));
		assertEquals(1, eventData.size());
		assertEquals(2, ((Collection<?>) eventData.get(PlacesTestConstants.EventDataKeys.Places.USER_WITHIN_POIS)).size());
		assertNull(dispatchedEvent.getResponseID());
	}

	@Test
	public void test_dispatchRegionEvent_When_NullRegion() {
		// test
		placesDispatcher.dispatchRegionEvent(null);

		// verify
		verifyNoInteractions(extensionApi);
	}

	@Test
	public void test_dispatchRegionEvent() {
		// setup
		final PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									  SAMPLE_LIBRARY, SAMPLE_WEIGHT
									  , SAMPLE_METADATA);
		final PlacesRegion region =  new PlacesRegion(poi, PlacesRegion.PLACE_EVENT_ENTRY, 777777777) ;

		// test
		placesDispatcher.dispatchRegionEvent(region);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify event details
		assertEquals(PlacesTestConstants.EventName.RESPONSE_PROCESSREGIONEVENT, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		assertEquals(3, dispatchedEvent.getEventData().size());


		// verify region details
		final Map poiMap = (Map)dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.TRIGGERING_REGION);
		assertTrue(poi.getName().equals(poiMap.get(PlacesTestConstants.POIKeys.NAME)));
		assertEquals(dispatchedEvent.getEventData().get(
						 PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE), PlacesRegion.PLACE_EVENT_ENTRY);
		assertEquals(dispatchedEvent.getEventData().get(
						 PlacesTestConstants.EventDataKeys.Places.REGION_TIMESTAMP), Long.valueOf(777777777));
	}

	@Test
	public void test_dispatchLastKnownLocation() {
		// test
		final Event triggeringEvent = triggerEvent;
		placesDispatcher.dispatchLastKnownLocation(31.33, -121.33, triggeringEvent);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify event details
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETLASTKNOWNLOCATION, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		assertEquals(2, dispatchedEvent.getEventData().size());

		// verify location details
		assertEquals(31.33,(double) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE), 0);
		assertEquals(-121.33, (double) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE), 0);
		assertEquals(triggeringEvent.getUniqueIdentifier(), dispatchedEvent.getResponseID());
	}

	@Test
	public void test_dispatchLastKnownLocation_withNullEvent() {
		// test
		placesDispatcher.dispatchLastKnownLocation(31.33, -121.33, null);

		// verify
		verify(extensionApi).dispatch(dispatchedEventCaptor.capture());
		Event dispatchedEvent = dispatchedEventCaptor.getValue();

		// verify event details
		assertEquals(PlacesTestConstants.EventName.RESPONSE_GETLASTKNOWNLOCATION, dispatchedEvent.getName());
		assertEquals(EventType.PLACES, dispatchedEvent.getType());
		assertEquals(EventSource.RESPONSE_CONTENT, dispatchedEvent.getSource());
		assertEquals(2, dispatchedEvent.getEventData().size());

		// verify location details
		assertEquals(31.33,(double) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE), 0);
		assertEquals(-121.33, (double) dispatchedEvent.getEventData().get(PlacesTestConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE), 0);
		assertNull(dispatchedEvent.getResponseID());
	}

	@Test
	public void test_sendExperienceEventToEdge_withInvalidEventType() {
		// setup
		final PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
				SAMPLE_LIBRARY, SAMPLE_WEIGHT
				, SAMPLE_METADATA);
		final PlacesRegion region =  new PlacesRegion(poi, "randomType", 777777777);

		// test
		placesDispatcher.dispatchExperienceEventToEdge(region);

		// verify
		verifyNoInteractions(extensionApi);
	}

	@Test
	public void test_sendExperienceEventToEdge_WithValidEntryEvent() {
		// setup
		final PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
				SAMPLE_LIBRARY, SAMPLE_WEIGHT
				, SAMPLE_METADATA);
		final PlacesRegion region =  new PlacesRegion(poi, PlacesRegion.PLACE_EVENT_ENTRY, 777777777);

		// test
		placesDispatcher.dispatchExperienceEventToEdge(region);

		// verify
		verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture());
		final Event dispatchedEvent = dispatchedEventCaptor.getValue();

		final Map<String, Object> xdm = (Map<String, Object>) dispatchedEvent.getEventData().get(PlacesTestConstants.XDM.Key.XDM);
		assertEquals(PlacesTestConstants.XDM.Location.EventType.ENTRY, xdm.get(PlacesTestConstants.XDM.Key.EVENT_TYPE));

		final Map<String, Object> placesContext = (Map<String, Object>) xdm.get(PlacesTestConstants.XDM.Key.PLACE_CONTEXT);
		final Map<String, Object> poiInteraction = (Map<String, Object>) placesContext.get(PlacesTestConstants.XDM.Key.POI_INTERACTION);

		final Map<String, Object> poiDetail = (Map<String, Object>) poiInteraction.get(PlacesTestConstants.XDM.Key.POI_DETAIL);
		assertEquals(SAMPLE_IDENTIFIER, poiDetail.get(PlacesTestConstants.XDM.Key.POI_ID));
		assertEquals(SAMPLE_NAME, poiDetail.get(PlacesTestConstants.XDM.Key.NAME));

		final List<Map<String, Object>> metadataList = (List<Map<String, Object>>)
				((Map<String,Object>)poiDetail.get(PlacesTestConstants.XDM.Key.METADATA)).get(PlacesTestConstants.XDM.Key.LIST);
		for (final Map.Entry<String, String> entry: SAMPLE_METADATA.entrySet()) {
			final Map<String, Object> metadata = new HashMap<String, Object>() {{
				put(PlacesConstants.XDM.Key.KEY, entry.getKey());
				put(PlacesConstants.XDM.Key.VALUE, entry.getValue());
			}};
			assertTrue(metadataList.contains(metadata));
		}

		assertEquals("xdm.eventType", dispatchedEvent.getMask()[0]);
		assertEquals("xdm.placeContext.POIinteraction.poiDetail.poiID", dispatchedEvent.getMask()[1]);
	}

	@Test
	public void test_sendExperienceEventToEdge_WithValidExitEvent() {
		// setup
		final PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
				SAMPLE_LIBRARY, SAMPLE_WEIGHT
				, SAMPLE_METADATA);
		final PlacesRegion region =  new PlacesRegion(poi, PlacesRegion.PLACE_EVENT_EXIT, 777777777);

		// test
		placesDispatcher.dispatchExperienceEventToEdge(region);

		// verify
		verify(extensionApi, times(1)).dispatch(dispatchedEventCaptor.capture());
		final Event dispatchedEvent = dispatchedEventCaptor.getValue();

		final Map<String, Object> xdm = (Map<String, Object>) dispatchedEvent.getEventData().get(PlacesTestConstants.XDM.Key.XDM);
		assertEquals(PlacesTestConstants.XDM.Location.EventType.EXIT, xdm.get(PlacesTestConstants.XDM.Key.EVENT_TYPE));

		final Map<String, Object> placesContext = (Map<String, Object>) xdm.get(PlacesTestConstants.XDM.Key.PLACE_CONTEXT);
		final Map<String, Object> poiInteraction = (Map<String, Object>) placesContext.get(PlacesTestConstants.XDM.Key.POI_INTERACTION);

		final Map<String, Object> poiDetail = (Map<String, Object>) poiInteraction.get(PlacesTestConstants.XDM.Key.POI_DETAIL);
		assertEquals(SAMPLE_IDENTIFIER, poiDetail.get(PlacesTestConstants.XDM.Key.POI_ID));
		assertEquals(SAMPLE_NAME, poiDetail.get(PlacesTestConstants.XDM.Key.NAME));

		final List<Map<String, Object>> metadataList = (List<Map<String, Object>>)
				((Map<String,Object>)poiDetail.get(PlacesTestConstants.XDM.Key.METADATA)).get(PlacesTestConstants.XDM.Key.LIST);
		for (final Map.Entry<String, String> entry: SAMPLE_METADATA.entrySet()) {
			final Map<String, Object> metadata = new HashMap<String, Object>() {{
				put(PlacesConstants.XDM.Key.KEY, entry.getKey());
				put(PlacesConstants.XDM.Key.VALUE, entry.getValue());
			}};
			assertTrue(metadataList.contains(metadata));
		}

		assertEquals("xdm.eventType", dispatchedEvent.getMask()[0]);
		assertEquals("xdm.placeContext.POIinteraction.poiDetail.poiID", dispatchedEvent.getMask()[1]);
	}

	private PlacesConfiguration createPlacesConfig() {

		List<Map<String, String>> libraries = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			Map<String, String> library = new HashMap<>();
			library.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "lib" + (i + 1));
			libraries.add(library);
		}

		final Map<String, Object> configData = new HashMap<>();
		configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, libraries);
		configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, "serverEndpoint");
		configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL, 200);
		return new PlacesConfiguration(configData);
	}
}
