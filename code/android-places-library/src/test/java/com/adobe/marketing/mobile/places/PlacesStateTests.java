///*
//  Copyright 2022 Adobe. All rights reserved.
//  This file is licensed to you under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License. You may obtain a copy
//  of the License at http://www.apache.org/licenses/LICENSE-2.0
//  Unless required by applicable law or agreed to in writing, software distributed under
//  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
//  OF ANY KIND, either express or implied. See the License for the specific language
//  governing permissions and limitations under the License.
// */
//
//package com.adobe.marketing.mobile.places;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//
//import static org.junit.Assert.*;
//
//import com.adobe.marketing.mobile.services.Log;
//
//public class PlacesStateTests  {
//
//	private static final String PLACES_DATA_STORE = "placesdatastore";
//
//	PlacesState placesState;
//	PlacesPOI sampleCurrentPOI = createPOI("currentPOI", 2);
//	PlacesPOI sampleLastEnteredPOI = createPOI("lastEnteredPOI", 2);
//	PlacesPOI sampleLastExitedPOI = createPOI("lastExitedPOI", 2);
//	private static final long SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP = 3990;
//
//	@Before
//	public void testSetup() {
//		placesState = new PlacesState();
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>();
//	}
//
//	// ========================================================================================
//	// Constructor
//	// ========================================================================================
//
//	@Test
//	public void PlacesState_Constructor_When_Happy() throws Exception {
//		// setup
//		persistSampleData();
//
//		// test
//		placesState = new PlacesState();
//
//		// verify load from persistence
//		assertEquals("currentPOI", placesState.currentPOI.getIdentifier());
//		assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
//		assertEquals("lastEnteredPOI", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals(2, placesState.cachedPOIs.size());
//		assertEquals(PlacesAuthorizationStatus.ALWAYS.stringValue(), placesState.authStatus);
//		assertEquals(SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP, placesState.membershipValidUntil);
//
//	}
//
//	// ========================================================================================
//	// processNetworkResponse
//	// ========================================================================================
//	@Test
//	public void ProcessNetworkResponse_Happy() throws Exception {
//
//		// test
//		placesState.processNetworkResponse(GetSampleSuccessPlacesRespone(2, 2),
//										   getPlacesRequestEvent());
//
//		// verify memory variables
//		assertEquals("containsUserPOI 0", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals("containsUserPOI 0", placesState.currentPOI.getIdentifier());
//		assertNull(placesState.lastExitedPOI);
//		assertEquals(4, placesState.cachedPOIs.size());
//
//		// verify the persistence
//		assertEquals("containsUserPOI 0", getPersistedCurrentPOI().getIdentifier());
//		assertEquals("containsUserPOI 0", getPersistedLastEnteredPOI().getIdentifier());
//		assertNull(getPersistedLastExitedPOI());
//		assertEquals(4, getPersistedCachedPOI().size());
//	}
//
//	@Test
//	public void ProcessNetworkResponse_When_NoPOIInResponse() throws Exception {
//		// test
//		placesState.processNetworkResponse(GetSampleSuccessPlacesRespone(0, 0),
//										   getPlacesRequestEvent());
//
//		// verify memory variables
//		assertNull(placesState.lastEnteredPOI);
//		assertNull(placesState.currentPOI);
//		assertNull(placesState.lastExitedPOI);
//		assertEquals(0, placesState.cachedPOIs.size());
//
//		// verify the persistence
//		assertNull(getPersistedCurrentPOI());
//		assertNull(getPersistedLastEnteredPOI());
//		assertNull(getPersistedLastExitedPOI());
//		assertEquals(0, getPersistedCachedPOI().size());
//	}
//
//
//	@Test
//	public void ProcessNetworkResponse_When_poisInCache() throws Exception {
//		// setup
//		LinkedHashMap<String, PlacesPOI> sampleCachedPOIs = new LinkedHashMap<String, PlacesPOI>();
//		PlacesPOI poi1 = new PlacesPOI("containsUserPOI 0", "hidden", 34.33, -121.55, 150, "libraryName", 2, null);
//		poi1.setUserIsWithin(true);
//		PlacesPOI poi2 = new PlacesPOI("containsUserPOI 1", "treasure", 34.33, -121.55, 150, "libraryName", 2, null);
//		poi2.setUserIsWithin(false);
//		sampleCachedPOIs.put("containsUserPOI 0", poi1);
//		sampleCachedPOIs.put("containsUserPOI 1", poi2);
//		placesState.cachedPOIs =  sampleCachedPOIs;
//
//		// test
//		placesState.processNetworkResponse(GetSampleSuccessPlacesRespone(3, 0),
//										   getPlacesRequestEvent());
//
//		// verify memory variables
//		assertEquals("containsUserPOI 0", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals("containsUserPOI 0", placesState.currentPOI.getIdentifier());
//		assertNull(placesState.lastExitedPOI);
//		assertEquals(3, placesState.cachedPOIs.size());
//
//		// verify the persistence
//		assertEquals("containsUserPOI 0", getPersistedCurrentPOI().getIdentifier());
//		assertEquals("containsUserPOI 0", getPersistedLastEnteredPOI().getIdentifier());
//		assertNull(getPersistedLastExitedPOI());
//		assertEquals(3, getPersistedCachedPOI().size());
//	}
//
//
//	@Test
//	public void ProcessNetworkResponse_When_poisInCache_And_NoUserWithInPOIInResponse() throws Exception {
//		// setup
//		placesState.cachedPOIs =  getSampleCachePOIs();
//		placesState.currentPOI = sampleCurrentPOI;
//		placesState.lastEnteredPOI = sampleLastEnteredPOI;
//		placesState.lastExitedPOI = sampleLastExitedPOI;
//
//		// test
//		placesState.processNetworkResponse(GetSampleSuccessPlacesRespone(0, 3),
//										   getPlacesRequestEvent());
//
//		// verify memory variables
//		assertNull(placesState.currentPOI);
//		assertEquals("lastEnteredPOI", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
//		assertEquals(3, placesState.cachedPOIs.size());
//
//		// verify the persistence
//		assertNull(getPersistedCurrentPOI());
//		assertEquals("lastEnteredPOI", getPersistedLastEnteredPOI().getIdentifier());
//		assertEquals("lastExitedPOI", getPersistedLastExitedPOI().getIdentifier());
//		assertEquals(3, getPersistedCachedPOI().size());
//	}
//
//	@Test
//	public void ProcessNetworkResponse_UpdatesMembershipValidUntilTimeStamp() throws Exception {
//		// setup
//		placesState.membershipTtl = 500;
//
//		// test
//		placesState.processNetworkResponse(GetSampleSuccessPlacesRespone(0, 3),
//										   getPlacesRequestEvent());
//
//		// verify memory variables
//		assertEquals(getUnixTimeInSeconds() + 500, placesState.membershipValidUntil, 1);
//
//		// verify the persistence
//		assertEquals(getUnixTimeInSeconds() + 500, getPersistedMembershipValidUntilTimestamp(), 1);
//	}
//
//
//	// ========================================================================================
//	// processRegionEvent
//	// ========================================================================================
//
//	@Test
//	public void ProcessRegionEvent_When_NullRegionID() throws Exception {
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent(null, "entry"));
//
//		// verify
//		assertNull(returnedRegion);
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_EmptyRegionID() throws Exception {
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("", "entry"));
//
//		// verify
//		assertNull(returnedRegion);
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_UnknownRegionType() throws Exception {
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("regionId", "none"));
//
//		// verify
//		assertNull(returnedRegion);
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_NoRegionIDFoundInCache() throws Exception {
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("regionID", "entry"));
//
//		// verify
//		assertNull(returnedRegion);
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_RegionEntryEvent() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("poi1", 1);
//		poi1.setUserIsWithin(false);
//		final PlacesPOI poi2 = createPOI("poi2", 1);
//		poi2.setUserIsWithin(false);
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("poi1", poi1);
//				put("poi2", poi2);
//			}
//		};
//		placesState.lastEnteredPOI = null;
//		placesState.currentPOI = null;
//		placesState.lastExitedPOI = null;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("poi1", "entry"));
//
//		// verify memory variables
//		assertEquals("poi1", placesState.currentPOI.getIdentifier());
//		assertEquals("poi1", placesState.lastEnteredPOI.getIdentifier());
//		assertNull(placesState.lastExitedPOI);
//		assertEquals(2, placesState.cachedPOIs.size());
//
//		// verify if the containsUser is set correctly
//		assertTrue(placesState.cachedPOIs.get("poi1").containsUser());
//		assertFalse(placesState.cachedPOIs.get("poi2").containsUser());
//
//		// verify the persistence
//		assertEquals("poi1", getPersistedCurrentPOI().getIdentifier());
//		assertEquals("poi1", getPersistedLastEnteredPOI().getIdentifier());
//		assertNull(getPersistedLastExitedPOI());
//		assertEquals(2, getPersistedCachedPOI().size());
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_RegionEntryEvent_updatesMembershipValidUntilTtl() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("poi1", 1);
//		placesState.membershipTtl = 500;
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("poi1", poi1);
//			}
//		};
//
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("poi1", "entry"));
//		assertEquals(getUnixTimeInSeconds() + 500, placesState.membershipValidUntil, 1); // verify memory variable
//		assertEquals(getUnixTimeInSeconds() + 500, getPersistedMembershipValidUntilTimestamp(), 1); // verify the persistence
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_RegionEntryEvent_And_NoPOIExistInCache() throws Exception {
//		// setup
//		placesState.cachedPOIs = getSampleCachePOIs();
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("notInCache", "entry"));
//
//		// verify
//		assertNull(returnedRegion);
//
//		// verify memory variables
//		assertNull(placesState.currentPOI);
//		assertNull(placesState.lastEnteredPOI);
//		assertNull(placesState.lastExitedPOI);
//		assertEquals(2, placesState.cachedPOIs.size());
//
//		// verify the persistence
//		assertNull(getPersistedCurrentPOI());
//		assertNull(getPersistedLastEnteredPOI());
//		assertNull(getPersistedLastExitedPOI());
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_RegionEntryEventOfHigherWeightPOI() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("lowWeightPOI", 2);
//		poi1.setUserIsWithin(true);
//		final PlacesPOI poi2 = createPOI("highWeightPOI", 1);
//		poi2.setUserIsWithin(false);
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("lowWeightPOI", poi1);
//				put("highWeightPOI", poi2);
//			}
//		};
//		placesState.lastEnteredPOI = poi1;
//		placesState.currentPOI = poi1;
//		placesState.lastExitedPOI = sampleLastExitedPOI;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("highWeightPOI", "entry"));
//
//		// verify
//		assertNotNull(returnedRegion);
//		assertEquals("highWeightPOI", returnedRegion.getIdentifier());
//		assertEquals(PlacesRegion.PLACE_EVENT_ENTRY, returnedRegion.getPlaceEventType());
//
//		// verify memory variables
//		assertEquals("highWeightPOI", placesState.currentPOI.getIdentifier());
//		assertEquals("highWeightPOI", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
//		assertEquals(2, placesState.cachedPOIs.size());
//
//		// verify if the containsUser is set correctly
//		assertTrue(placesState.cachedPOIs.get("highWeightPOI").containsUser());
//		assertTrue(placesState.cachedPOIs.get("highWeightPOI").containsUser());
//
//		// verify the persistence
//		assertEquals("highWeightPOI", getPersistedCurrentPOI().getIdentifier());
//		assertEquals("highWeightPOI", getPersistedLastEnteredPOI().getIdentifier());
//		assertEquals("lastExitedPOI", getPersistedLastExitedPOI().getIdentifier());
//		assertEquals(2, getPersistedCachedPOI().size());
//	}
//
//
//	@Test
//	public void ProcessRegionEvent_When_RegionEntryEventOfLowerWeightPOI() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("lowWeightPOI", 2);
//		poi1.setUserIsWithin(false);
//		final PlacesPOI poi2 = createPOI("highWeightPOI", 1);
//		poi2.setUserIsWithin(true);
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("lowWeightPOI", poi1);
//				put("highWeightPOI", poi2);
//			}
//		};
//		placesState.lastEnteredPOI = poi2;
//		placesState.currentPOI = poi2;
//		placesState.lastExitedPOI = sampleLastExitedPOI;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("lowWeightPOI", "entry"));
//
//		// verify
//		assertNotNull(returnedRegion);
//		assertEquals("lowWeightPOI", returnedRegion.getIdentifier());
//		assertEquals(PlacesRegion.PLACE_EVENT_ENTRY, returnedRegion.getPlaceEventType());
//
//		// verify memory variables
//		assertEquals("highWeightPOI", placesState.currentPOI.getIdentifier());
//		assertEquals("lowWeightPOI", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
//		assertEquals(2, placesState.cachedPOIs.size());
//
//		// verify if the containsUser is set correctly
//		assertTrue(placesState.cachedPOIs.get("lowWeightPOI").containsUser());
//		assertTrue(placesState.cachedPOIs.get("highWeightPOI").containsUser());
//
//		// verify the persistence
//		assertEquals("highWeightPOI", getPersistedCurrentPOI().getIdentifier());
//		assertEquals("lowWeightPOI", getPersistedLastEnteredPOI().getIdentifier());
//		assertEquals("lastExitedPOI", getPersistedLastExitedPOI().getIdentifier());
//		assertEquals(2, getPersistedCachedPOI().size());
//	}
//
//
//	@Test
//	public void ProcessRegionEvent_When_RegionExitEvent() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("poi1", 1);
//		poi1.setUserIsWithin(true);
//		final PlacesPOI poi2 = createPOI("poi2", 1);
//		poi2.setUserIsWithin(false);
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("poi1", poi1);
//				put("poi2", poi2);
//			}
//		};
//		placesState.lastEnteredPOI = poi1;
//		placesState.currentPOI = poi1;
//		placesState.lastExitedPOI = null;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));
//
//		// verify
//		assertNotNull(returnedRegion);
//		assertEquals("poi1", returnedRegion.getIdentifier());
//		assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion.getPlaceEventType());
//
//		// verify memory variables
//		assertNull(placesState.currentPOI);
//		assertEquals("poi1", placesState.lastEnteredPOI.getIdentifier());
//		assertEquals("poi1", placesState.lastExitedPOI.getIdentifier());
//		assertEquals(2, placesState.cachedPOIs.size());
//
//		// verify if the containsUser is set correctly
//		assertFalse(placesState.cachedPOIs.get("poi1").containsUser());
//		assertFalse(placesState.cachedPOIs.get("poi2").containsUser());
//
//		// verify the persistence
//		assertNull(getPersistedCurrentPOI());
//		assertEquals("poi1", getPersistedLastEnteredPOI().getIdentifier());
//		assertEquals("poi1", getPersistedLastExitedPOI().getIdentifier());
//		assertEquals(2, getPersistedCachedPOI().size());
//	}
//
//	@Test
//	public void ProcessRegionEvent_When_RegionEntryExit_updatesMembershipValidUntilTtl() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("poi1", 1);
//		poi1.setUserIsWithin(true);
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("poi1", poi1);
//			}
//		};
//		placesState.membershipTtl = 500;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));
//		assertEquals(getUnixTimeInSeconds() + 500, placesState.membershipValidUntil, 1); // verify memory variable
//		assertEquals(getUnixTimeInSeconds() + 500, getPersistedMembershipValidUntilTimestamp(), 1); // verify memory variable
//	}
//
//
//	@Test
//	public void ProcessRegionEvent_When_RegionExitEvent_NoPoiInCache() throws Exception {
//		// setup in memory variables
//		placesState.lastEnteredPOI = null;
//		placesState.currentPOI = null;
//		placesState.lastExitedPOI = null;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));
//
//		// verify
//		assertNull(returnedRegion);
//
//		// verify memory variables
//		assertNull(placesState.currentPOI);
//		assertNull(placesState.lastEnteredPOI);
//		assertNull(placesState.lastExitedPOI);
//		assertEquals(0, placesState.cachedPOIs.size());
//
//		// verify the persistence
//		assertNull(getPersistedCurrentPOI());
//		assertNull(getPersistedLastEnteredPOI());
//		assertNull(getPersistedLastExitedPOI());
//		assertEquals(0, getPersistedCachedPOI().size());
//	}
//
//
//
//	@Test
//	public void ProcessRegionEvent_When_RegionExitEvent_POIAlreadyExited() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("poi1", 1);
//		poi1.setUserIsWithin(false);
//		final PlacesPOI poi2 = createPOI("poi2", 1);
//		poi2.setUserIsWithin(false);
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("poi1", poi1);
//				put("poi2", poi2);
//			}
//		};
//		placesState.lastEnteredPOI = null;
//		placesState.currentPOI = null;
//		placesState.lastExitedPOI = poi1;
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));
//
//		// verify
//		assertNotNull(returnedRegion);
//		assertEquals("poi1", returnedRegion.getIdentifier());
//		assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion.getPlaceEventType());
//
//		// verify memory variables
//		assertNull(placesState.currentPOI);
//		assertNull(placesState.lastEnteredPOI);
//		assertEquals("poi1", placesState.lastExitedPOI.getIdentifier());
//		assertEquals(2, placesState.cachedPOIs.size());
//
//		// verify if the containsUser is set correctly
//		assertFalse(placesState.cachedPOIs.get("poi1").containsUser());
//		assertFalse(placesState.cachedPOIs.get("poi2").containsUser());
//
//		// verify the persistence
//		assertNull(getPersistedCurrentPOI());
//		assertNull(getPersistedLastEnteredPOI());
//		assertEquals("poi1", getPersistedLastExitedPOI().getIdentifier());
//		assertEquals(2, getPersistedCachedPOI().size());
//	}
//
//
//	@Test
//	public void ProcessRegionEvent_When_RegionExitEvent_WeightPreferenceForNextPOI() throws Exception {
//		// setup in memory variables
//		final PlacesPOI poi1 = createPOI("lowWeight", 3);
//		poi1.setUserIsWithin(true);
//		final PlacesPOI poi2 = createPOI("mediumWeight", 2);
//		poi2.setUserIsWithin(true);
//		final PlacesPOI poi3 = createPOI("highWeight", 1);
//		poi3.setUserIsWithin(true);
//
//		placesState.cachedPOIs = new LinkedHashMap<String, PlacesPOI>() {
//			{
//				put("lowWeight", poi1);
//				put("mediumWeight", poi2);
//				put("highWeight", poi3);
//			}
//		};
//
//		// test
//		PlacesRegion returnedRegion = placesState.processRegionEvent(prepareRegionEvent("mediumWeight", "exit"));
//
//		// verify region event
//		assertNotNull(returnedRegion);
//		assertEquals("mediumWeight", returnedRegion.getIdentifier());
//		assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion.getPlaceEventType());
//
//		// verify memory variables
//		assertEquals("highWeight", placesState.currentPOI.getIdentifier());
//		assertEquals("mediumWeight", placesState.lastExitedPOI.getIdentifier());
//
//		// verify if the containsUser is set correctly
//		assertTrue(placesState.cachedPOIs.get("lowWeight").containsUser());
//		assertFalse(placesState.cachedPOIs.get("mediumWeight").containsUser());
//		assertTrue(placesState.cachedPOIs.get("highWeight").containsUser());
//
//		// test another exit
//		PlacesRegion returnedRegion2 = placesState.processRegionEvent(prepareRegionEvent("highWeight", "exit"));
//
//		// verify region event
//		assertNotNull(returnedRegion2);
//		assertEquals("highWeight", returnedRegion2.getIdentifier());
//		assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion2.getPlaceEventType());
//
//		// verify memory variables
//		assertEquals("lowWeight", placesState.currentPOI.getIdentifier());
//		assertEquals("highWeight", placesState.lastExitedPOI.getIdentifier());
//
//		// verify if the containsUser is set correctly
//		assertTrue(placesState.cachedPOIs.get("lowWeight").containsUser());
//		assertFalse(placesState.cachedPOIs.get("mediumWeight").containsUser());
//		assertFalse(placesState.cachedPOIs.get("highWeight").containsUser());
//	}
//
//
//	// ========================================================================================
//	// getPlacesSharedState
//	// ========================================================================================
//
//	@Test
//	public void GetPlacesSharedState() throws Exception {
//		// setup
//		placesState.cachedPOIs =  getSampleCachePOIs();
//		placesState.currentPOI = sampleCurrentPOI;
//		placesState.lastEnteredPOI = sampleLastEnteredPOI;
//		placesState.lastExitedPOI = sampleLastExitedPOI;
//		placesState.authStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
//		placesState.membershipValidUntil = getUnixTimeInSeconds() + 20;  // 20 secs from now
//
//		// test
//		EventData data = placesState.getPlacesSharedState();
//
//		// verify
//		assertNotNull(data);
//		assertEquals(6, data.size());
//		assertEquals("currentPOI", data.getTypedObject(PlacesTestConstants.SharedStateKeys.CURRENT_POI,
//					 new PlacesPOIVariantSerializer()).getIdentifier());
//		assertEquals("lastEnteredPOI", data.getTypedObject(PlacesTestConstants.SharedStateKeys.LAST_ENTERED_POI,
//					 new PlacesPOIVariantSerializer()).getIdentifier());
//		assertEquals("lastExitedPOI", data.getTypedObject(PlacesTestConstants.SharedStateKeys.LAST_EXITED_POI,
//					 new PlacesPOIVariantSerializer()).getIdentifier());
//		assertEquals(2, data.getTypedList(PlacesTestConstants.SharedStateKeys.NEARBYPOIS,
//										  new PlacesPOIVariantSerializer()).size());
//		assertEquals(PlacesAuthorizationStatus.ALWAYS.stringValue(),
//					 data.getString2(PlacesTestConstants.SharedStateKeys.AUTH_STATUS));
//		assertEquals(getUnixTimeInSeconds() + 20,
//					 data.getLong(PlacesTestConstants.SharedStateKeys.VALID_UNTIL), 1);
//	}
//
//	@Test
//	public void GetPlacesSharedState_When_AllVariableNull() throws Exception {
//		// setup
//		placesState.currentPOI = null;
//		placesState.lastEnteredPOI = null;
//		placesState.lastExitedPOI = null;
//		placesState.authStatus = null;
//		placesState.membershipValidUntil = 0;
//
//		// test
//		EventData data = placesState.getPlacesSharedState();
//
//		// verify
//		assertNotNull(data);
//		assertEquals(1, data.size());
//		assertEquals(0, data.getLong(PlacesTestConstants.SharedStateKeys.VALID_UNTIL));
//	}
//
//
//	@Test
//	public void GetPlacesSharedState_ClearsMembershipDataWhenInvalid() throws Exception {
//		// setup
//		placesState.cachedPOIs =  getSampleCachePOIs();
//		placesState.currentPOI = sampleCurrentPOI;
//		placesState.lastEnteredPOI = sampleLastEnteredPOI;
//		placesState.lastExitedPOI = sampleLastExitedPOI;
//		placesState.authStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
//		placesState.membershipValidUntil = getUnixTimeInSeconds() - 10; // time before now
//
//		// test
//		EventData data = placesState.getPlacesSharedState();
//
//		// verify
//		assertNotNull(data);
//		assertEquals(3, data.size());
//		assertFalse(data.containsKey(PlacesTestConstants.SharedStateKeys.CURRENT_POI));
//		assertFalse(data.containsKey(PlacesTestConstants.SharedStateKeys.LAST_ENTERED_POI));
//		assertFalse(data.containsKey(PlacesTestConstants.SharedStateKeys.LAST_EXITED_POI));
//
//		// nearby POIs values still persist
//		assertEquals(2, data.getTypedList(PlacesTestConstants.SharedStateKeys.NEARBYPOIS,
//										  new PlacesPOIVariantSerializer()).size());
//		assertEquals(PlacesAuthorizationStatus.ALWAYS.stringValue(),
//					 data.getString2(PlacesTestConstants.SharedStateKeys.AUTH_STATUS));
//		assertEquals(0, data.getLong(PlacesTestConstants.SharedStateKeys.VALID_UNTIL));
//	}
//
//	// ========================================================================================
//	// saveLastKnownLocation
//	// ========================================================================================
//	@Test
//	public void saveAndLoadLastKnownLocation_when_validLocation() {
//		// test
//		placesState.saveLastKnownLocation(34.2, 12.3);
//
//		// verify
//		PlacesGpsLocation gpsLocation = placesState.loadLastKnownLocation();
//
//		assertEquals(34.2, gpsLocation.getLatitude(), 0.0);
//		assertEquals(12.3, gpsLocation.getLongitude(), 0.0);
//	}
//
//	@Test
//	public void saveAndLoadLastKnownLocation_when_InvalidLocation() {
//		// test
//		placesState.saveLastKnownLocation(355.2, -6612.3);
//
//		// verify
//		assertNull(placesState.loadLastKnownLocation());
//	}
//
//	@Test
//	public void saveAndLoadLastKnownLocation_when_NoDataStore() {
//		// setup
//		placesState = new PlacesState(null);
//
//		// test
//		placesState.saveLastKnownLocation(355.2, -6612.3);
//
//		// verify
//		assertNull(placesState.loadLastKnownLocation());
//	}
//
//	// ========================================================================================
//	// setAuthorizationStatus
//	// ========================================================================================
//
//	@Test
//	public void saveLocationPermissionStatus_happy() throws Exception {
//		// setup
//		String actualStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
//
//		// test
//		placesState.setAuthorizationStatus(actualStatus);
//
//		// verify
//		assertEquals(actualStatus, placesState.authStatus);
//		assertEquals(actualStatus, getPersistedPermissionStatus());
//	}
//
//	@Test
//	public void saveLocationPermissionStatus_when_noDataStore() throws Exception {
//		// setup
//		String actualStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
//		placesState = new PlacesState(null);
//
//		// test
//		placesState.setAuthorizationStatus(actualStatus);
//
//		// verify
//		assertEquals(actualStatus, placesState.authStatus);
//		assertNull(getPersistedPermissionStatus());
//	}
//
//	@Test
//	public void saveLocationPermissionStatus_when_nullStatus() throws Exception {
//		// setup
//		persistSampleData();
//
//		// test
//		placesState.setAuthorizationStatus(null);
//
//		// verify
//		assertEquals(PlacesAuthorizationStatus.UNKNOWN.stringValue(), placesState.authStatus);
//		assertEquals(PlacesAuthorizationStatus.UNKNOWN.stringValue(), getPersistedPermissionStatus());
//	}
//
//
//
//	// ========================================================================================
//	// getUserWithInPOIs
//	// ========================================================================================
//	@Test
//	public void GetUserWithInPOIs_WhenNoPOIs() throws Exception {
//		// test and verify
//		assertEquals(0, placesState.getUserWithInPOIs().size());
//	}
//
//	@Test
//	public void GetUserWithInPOIs_WhenValidPOIs() throws Exception {
//		// setup
//		placesState.cachedPOIs = getSampleCachePOIs();
//
//		// test and verify
//		assertEquals(1, placesState.getUserWithInPOIs().size());
//		assertEquals("cachedPOI1", placesState.getUserWithInPOIs().get(0).getIdentifier());
//	}
//
//
//	// ========================================================================================
//	// privacyOptedOut
//	// ========================================================================================
//	@Test
//	public void clearData_ClearsInMemoryVariables() throws Exception {
//		// setup
//		placesState.cachedPOIs =  getSampleCachePOIs();
//		placesState.currentPOI = sampleCurrentPOI;
//		placesState.lastEnteredPOI = sampleLastEnteredPOI;
//		placesState.lastExitedPOI = sampleLastExitedPOI;
//		placesState.membershipValidUntil = SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP;
//
//		// test
//		placesState.clearData();
//
//		// verify
//		assertEquals(0, placesState.cachedPOIs.size());
//		assertNull(placesState.currentPOI);
//		assertNull(placesState.lastExitedPOI);
//		assertNull(placesState.lastEnteredPOI);
//		assertEquals(0, placesState.membershipValidUntil);
//	}
//
//	@Test
//	public void clearData_ClearsPersistedVariables() throws Exception {
//		// setup
//		persistSampleData();
//
//		// test
//		placesState.clearData();
//
//		// verify
//		assertNull(getPersistedCurrentPOI());
//		assertNull(getPersistedLastEnteredPOI());
//		assertNull(getPersistedLastExitedPOI());
//		assertEquals(0, getPersistedMembershipValidUntilTimestamp());
//		assertEquals(PlacesAuthorizationStatus.UNKNOWN.stringValue(), getPersistedPermissionStatus());
//		assertEquals(0, getPersistedCachedPOI().size());
//	}
//
//	// ========================================================================================
//	// Helper methods
//	// ========================================================================================
//
//	private Event prepareRegionEvent(final String regionID, final String regionType) {
//		EventData eventData = new EventData();
//		eventData.putString(PlacesConstants.EventDataKeys.Places.REGION_ID, regionID);
//		eventData.putString(PlacesConstants.EventDataKeys.Places.REGION_EVENT_TYPE, regionType);
//		eventData.putString(PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
//							PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT);
//		final Event event = new Event.Builder("Process Region Event", EventType.PLACES, EventSource.REQUEST_CONTENT)
//		.setData(eventData)
//		.build();
//		return event;
//	}
//
//	private LinkedHashMap<String, PlacesPOI> getSampleCachePOIs() {
//		LinkedHashMap<String, PlacesPOI> sampleCachedPOIs = new LinkedHashMap<String, PlacesPOI>();
//		PlacesPOI poi1 = new PlacesPOI(createPOI("cachedPOI1", 2));
//		poi1.setUserIsWithin(true);
//		PlacesPOI poi2 = new PlacesPOI(createPOI("cachedPOI2", 2));
//		poi2.setUserIsWithin(false);
//		sampleCachedPOIs.put("cachedPOI1", poi1);
//		sampleCachedPOIs.put("cachedPOI2", poi2);
//		return sampleCachedPOIs;
//	}
//
//	private PlacesPOI createPOI(final String id, final int weight) {
//		return new PlacesPOI(id, "hidden", 34.33, -121.55, 150, "libraryName", weight, null);
//	}
//
//	private void persistSampleData() throws Exception {
//
//		// cached POI
//		Variant cachedPOIVariant = Variant.fromTypedMap(getSampleCachePOIs(), new PlacesPOIVariantSerializer());
//		String jsonString = cachedPOIVariant.toString();
//		GetFakeDataStore().setString(PlacesTestConstants.DataStoreKeys.NEARBYPOIS, jsonString);
//
//		// current POI
//		Variant currentPOIvariant = Variant.fromTypedObject(sampleCurrentPOI, new PlacesPOIVariantSerializer());
//		String currentPOIjsonString = currentPOIvariant.toString();
//		GetFakeDataStore().setString(PlacesTestConstants.DataStoreKeys.CURRENT_POI, currentPOIjsonString);
//
//		// lastEntered POI
//		Variant lastEnteredVariant = Variant.fromTypedObject(sampleLastEnteredPOI, new PlacesPOIVariantSerializer());
//		String jlastEnteredJsonString = lastEnteredVariant.toString();
//		GetFakeDataStore().setString(PlacesTestConstants.DataStoreKeys.LAST_ENTERED_POI, jlastEnteredJsonString);
//
//		// lastExited POI
//		Variant lastExitedVariant = Variant.fromTypedObject(sampleLastExitedPOI, new PlacesPOIVariantSerializer());
//		String lastExitedJsonString = lastExitedVariant.toString();
//		GetFakeDataStore().setString(PlacesTestConstants.DataStoreKeys.LAST_EXITED_POI, lastExitedJsonString);
//
//		GetFakeDataStore().setString(PlacesTestConstants.DataStoreKeys.AUTH_STATUS,
//									 PlacesAuthorizationStatus.ALWAYS.stringValue());
//
//		GetFakeDataStore().setString(PlacesTestConstants.DataStoreKeys.AUTH_STATUS,
//									 PlacesAuthorizationStatus.ALWAYS.stringValue());
//
//		GetFakeDataStore().setLong(PlacesConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL,
//								   SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP);
//	}
//
//
//	private LinkedHashMap<String, PlacesPOI> getPersistedCachedPOI() throws Exception {
//		LinkedHashMap<String, PlacesPOI>cachedPOIs = new LinkedHashMap<String, PlacesPOI>();
//		String nearbyString = GetFakeDataStore().getString(PlacesTestConstants.DataStoreKeys.NEARBYPOIS, "");
//
//		if (!StringUtils.isNullOrEmpty(nearbyString)) {
//			JsonUtilityService.JSONObject nearbyMap = platformServices.fakeJsonUtilityService.createJSONObject(nearbyString);
//
//			if (nearbyMap == null) {
//				return cachedPOIs;
//			}
//
//			Iterator<String> keys = nearbyMap.keys();
//
//			try {
//				while (keys.hasNext()) {
//					String key = keys.next();
//					cachedPOIs.put(key, new PlacesPOI(nearbyMap.getJSONObject(key), platformServices.fakeJsonUtilityService));
//				}
//			} catch (JsonException exp) {
//			}
//		}
//
//		return cachedPOIs;
//	}
//
//	private PlacesPOI getPersistedLastEnteredPOI() throws Exception {
//		String lastEnteredString = GetFakeDataStore().getString(PlacesTestConstants.DataStoreKeys.LAST_ENTERED_POI, "");
//
//		if (!StringUtils.isNullOrEmpty(lastEnteredString)) {
//			PlacesPOI lastEnteredPOI = new PlacesPOI(lastEnteredString, platformServices.fakeJsonUtilityService);
//			return lastEnteredPOI;
//		}
//
//		return null;
//	}
//
//	private PlacesPOI getPersistedLastExitedPOI() throws Exception {
//		String lastExitedString = GetFakeDataStore().getString(PlacesTestConstants.DataStoreKeys.LAST_EXITED_POI, "");
//
//		if (!StringUtils.isNullOrEmpty(lastExitedString)) {
//			PlacesPOI lastExitedPOI = new PlacesPOI(lastExitedString, platformServices.fakeJsonUtilityService);
//			return lastExitedPOI ;
//		}
//
//		return null;
//	}
//
//	private PlacesPOI getPersistedCurrentPOI() throws Exception {
//		String currentPOIString = GetFakeDataStore().getString(PlacesTestConstants.DataStoreKeys.CURRENT_POI, "");
//
//		if (!StringUtils.isNullOrEmpty(currentPOIString)) {
//			PlacesPOI currentPOI = new PlacesPOI(currentPOIString, platformServices.fakeJsonUtilityService);
//			return currentPOI;
//		}
//
//		return null;
//	}
//
//	private String getPersistedPermissionStatus() throws Exception {
//		return GetFakeDataStore().getString(PlacesTestConstants.DataStoreKeys.AUTH_STATUS, null);
//	}
//
//	private long getPersistedMembershipValidUntilTimestamp() throws Exception {
//		return GetFakeDataStore().getLong(PlacesTestConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL, 0);
//	}
//
//	private PlacesQueryResponse GetSampleSuccessPlacesRespone(final int containsUserPOICount, final int nearByPOIsCount) {
//		PlacesQueryResponse response = new PlacesQueryResponse();
//		response.containsUserPOIs = new ArrayList<PlacesPOI>();
//
//		for (int i = 0 ; i < containsUserPOICount; i++) {
//			response.containsUserPOIs.add(new PlacesPOI("containsUserPOI " + i, "hidden", 34.33, -121.55, 150, "libraryName", 2,
//										  null));
//		}
//
//		response.nearByPOIs = new ArrayList<PlacesPOI>();
//
//		for (int i = 0 ; i < nearByPOIsCount; i++) {
//			response.nearByPOIs.add(new PlacesPOI("nearByPOI" + i, "hidden", 34.33, -121.55, 150, "libraryName", 2, null));
//		}
//
//		response.isSuccess = true;
//		return response;
//	}
//
//	private Event getPlacesRequestEvent() {
//		Event requestEvent = new Event.Builder("GetUserWithin POIs response event", EventType.PLACES, EventSource
//											   .RESPONSE_CONTENT).build();
//		return requestEvent;
//	}
//
//	private LocalStorageService.DataStore GetFakeDataStore() {
//		return platformServices.fakeLocalStorageService.getDataStore(PLACES_DATA_STORE);
//	}
//
//	private long getUnixTimeInSeconds() {
//		return System.currentTimeMillis() / 1000;
//	}
//
//
//}
