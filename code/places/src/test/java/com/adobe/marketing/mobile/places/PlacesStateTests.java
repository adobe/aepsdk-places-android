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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.location.Location;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.services.DataStoring;
import com.adobe.marketing.mobile.services.NamedCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlacesStateTests {

    @Mock private DataStoring dataStoring;

    @Mock private NamedCollection placesDataStore;

    private static final String PLACES_DATA_STORE = "placesdatastore";

    PlacesState placesState;
    PlacesPOI sampleCurrentPOI = createPOI("currentPOI", 2);
    PlacesPOI sampleLastEnteredPOI = createPOI("lastEnteredPOI", 2);
    PlacesPOI sampleLastExitedPOI = createPOI("lastExitedPOI", 2);
    private static final long SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP = 3990;

    @Before
    public void testSetup() {
        reset(dataStoring);
        when(dataStoring.getNamedCollection(PLACES_DATA_STORE)).thenReturn(placesDataStore);
        placesState = new PlacesState(dataStoring);
        placesState.cachedPOIs = new LinkedHashMap<>();
    }

    // ========================================================================================
    // Constructor
    // ========================================================================================

    @Test
    public void placesState_Constructor_When_happy() {
        // setup
        persistSampleData();

        // test
        placesState = new PlacesState(dataStoring);

        // verify load from persistence
        assertEquals("currentPOI", placesState.currentPOI.getIdentifier());
        assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
        assertEquals("lastEnteredPOI", placesState.lastEnteredPOI.getIdentifier());
        assertEquals(2, placesState.cachedPOIs.size());
        assertEquals(PlacesAuthorizationStatus.ALWAYS.stringValue(), placesState.authStatus);
        assertEquals(SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP, placesState.membershipValidUntil);
    }

    // ========================================================================================
    // processNetworkResponse
    // ========================================================================================
    @Test
    public void processNetworkResponse_happy() throws JSONException {
        // test
        placesState.processNetworkResponse(GetSampleSuccessPlacesResponse(2, 2));

        // verify memory variables
        assertEquals("containsUserPOI 0", placesState.lastEnteredPOI.getIdentifier());
        assertEquals("containsUserPOI 0", placesState.currentPOI.getIdentifier());
        assertNull(placesState.lastExitedPOI);
        assertEquals(4, placesState.cachedPOIs.size());

        // verify the persistence
        assertEquals("containsUserPOI 0", getPersistedCurrentPOI().getIdentifier());
        assertEquals("containsUserPOI 0", getPersistedLastEnteredPOI().getIdentifier());
        verify(placesDataStore, times(0))
                .setString(eq(PlacesTestConstants.DataStoreKeys.LAST_EXITED_POI), any());
        assertEquals(4, getPersistedCachedPOI().size());
    }

    @Test
    public void processNetworkResponse_When_noPOIInResponse() throws Exception {
        // test
        placesState.processNetworkResponse(GetSampleSuccessPlacesResponse(0, 0));

        // verify memory variables
        assertNull(placesState.lastEnteredPOI);
        assertNull(placesState.currentPOI);
        assertNull(placesState.lastExitedPOI);
        assertEquals(0, placesState.cachedPOIs.size());

        // verify the persistence
        verifyCurrentPOINotPersisted();
        verifyLastEnteredPOINotPersisted();
        verifyLastExcitedPOINotPersisted();
        verifyNearbyPOINotPersisted();
    }

    @Test
    public void processNetworkResponse_When_poisInCache() throws Exception {
        // setup
        LinkedHashMap<String, PlacesPOI> sampleCachedPOIs = new LinkedHashMap<String, PlacesPOI>();
        PlacesPOI poi1 =
                new PlacesPOI(
                        "containsUserPOI 0", "hidden", 34.33, -121.55, 150, "libraryName", 2, null);
        poi1.setUserIsWithin(true);
        PlacesPOI poi2 =
                new PlacesPOI(
                        "containsUserPOI 1",
                        "treasure",
                        34.33,
                        -121.55,
                        150,
                        "libraryName",
                        2,
                        null);
        poi2.setUserIsWithin(false);
        sampleCachedPOIs.put("containsUserPOI 0", poi1);
        sampleCachedPOIs.put("containsUserPOI 1", poi2);
        placesState.cachedPOIs = sampleCachedPOIs;

        // test
        placesState.processNetworkResponse(GetSampleSuccessPlacesResponse(3, 0));

        // verify memory variables
        assertEquals("containsUserPOI 0", placesState.lastEnteredPOI.getIdentifier());
        assertEquals("containsUserPOI 0", placesState.currentPOI.getIdentifier());
        assertNull(placesState.lastExitedPOI);
        assertEquals(3, placesState.cachedPOIs.size());

        // verify the persistence
        assertEquals("containsUserPOI 0", getPersistedCurrentPOI().getIdentifier());
        assertEquals("containsUserPOI 0", getPersistedLastEnteredPOI().getIdentifier());
        verifyLastExcitedPOINotPersisted();
        assertEquals(3, getPersistedCachedPOI().size());
    }

    @Test
    public void processNetworkResponse_When_poisInCache_And_NoUserWithInPOIInResponse()
            throws Exception {
        // setup
        placesState.cachedPOIs = getSampleCachePOIs();
        placesState.currentPOI = sampleCurrentPOI;
        placesState.lastEnteredPOI = sampleLastEnteredPOI;
        placesState.lastExitedPOI = sampleLastExitedPOI;

        // test
        placesState.processNetworkResponse(GetSampleSuccessPlacesResponse(0, 3));

        // verify memory variables
        assertNull(placesState.currentPOI);
        assertEquals("lastEnteredPOI", placesState.lastEnteredPOI.getIdentifier());
        assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
        assertEquals(3, placesState.cachedPOIs.size());

        // verify the persistence
        verifyCurrentPOINotPersisted();
        assertEquals("lastEnteredPOI", getPersistedLastEnteredPOI().getIdentifier());
        assertEquals("lastExitedPOI", getPersistedLastExitedPOI().getIdentifier());
        assertEquals(3, getPersistedCachedPOI().size());
    }

    @Test
    public void processNetworkResponse_updatesMembershipValidUntilTimeStamp() throws Exception {
        // setup
        placesState.membershipTtl = 500;

        // test
        placesState.processNetworkResponse(GetSampleSuccessPlacesResponse(0, 3));

        // verify memory variables
        assertEquals(getUnixTimeInSeconds() + 500, placesState.membershipValidUntil, 1);

        // verify the persistence
        assertEquals(getUnixTimeInSeconds() + 500, getPersistedMembershipValidUntilTimestamp(), 1);
    }

    // ========================================================================================
    // processRegionEvent
    // ========================================================================================

    @Test
    public void processRegionEvent_when_nullRegionID() throws Exception {
        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent(null, "entry"));

        // verify
        assertNull(returnedRegion);
    }

    @Test
    public void processRegionEvent_when_emptyRegionID() throws Exception {
        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("", "entry"));

        // verify
        assertNull(returnedRegion);
    }

    @Test
    public void processRegionEvent_when_unknownRegionType() throws Exception {
        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("regionId", "none"));

        // verify
        assertNull(returnedRegion);
    }

    @Test
    public void processRegionEvent_when_noRegionIDFoundInCache() throws Exception {
        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("regionID", "entry"));

        // verify
        assertNull(returnedRegion);
    }

    @Test
    public void processRegionEvent_when_regionEntryEvent() throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("poi1", 1);
        poi1.setUserIsWithin(false);
        final PlacesPOI poi2 = createPOI("poi2", 1);
        poi2.setUserIsWithin(false);

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("poi1", poi1);
                        put("poi2", poi2);
                    }
                };
        placesState.lastEnteredPOI = null;
        placesState.currentPOI = null;
        placesState.lastExitedPOI = null;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("poi1", "entry"));

        // verify memory variables
        assertEquals("poi1", placesState.currentPOI.getIdentifier());
        assertEquals("poi1", placesState.lastEnteredPOI.getIdentifier());
        assertNull(placesState.lastExitedPOI);
        assertEquals(2, placesState.cachedPOIs.size());

        // verify if the containsUser is set correctly
        assertTrue(placesState.cachedPOIs.get("poi1").containsUser());
        assertFalse(placesState.cachedPOIs.get("poi2").containsUser());

        // verify the persistence
        assertEquals("poi1", getPersistedCurrentPOI().getIdentifier());
        assertEquals("poi1", getPersistedLastEnteredPOI().getIdentifier());
        verifyLastExcitedPOINotPersisted();
        assertEquals(2, getPersistedCachedPOI().size());
    }

    @Test
    public void processRegionEvent_when_regionEntryEvent_updatesMembershipValidUntilTtl()
            throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("poi1", 1);
        placesState.membershipTtl = 500;

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("poi1", poi1);
                    }
                };

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("poi1", "entry"));
        assertEquals(
                getUnixTimeInSeconds() + 500,
                placesState.membershipValidUntil,
                1); // verify memory variable
        assertEquals(
                getUnixTimeInSeconds() + 500,
                getPersistedMembershipValidUntilTimestamp(),
                1); // verify the persistence
    }

    @Test
    public void processRegionEvent_when_regionEntryEvent_and_noPOIExistInCache() throws Exception {
        // setup
        placesState.cachedPOIs = getSampleCachePOIs();

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("notInCache", "entry"));

        // verify
        assertNull(returnedRegion);

        // verify memory variables
        assertNull(placesState.currentPOI);
        assertNull(placesState.lastEnteredPOI);
        assertNull(placesState.lastExitedPOI);
        assertEquals(2, placesState.cachedPOIs.size());

        // verify the persistence
        verifyCurrentPOINotPersisted();
        verifyLastEnteredPOINotPersisted();
        verifyLastExcitedPOINotPersisted();
    }

    @Test
    public void processRegionEvent_when_regionEntryEventOfHigherWeightPOI() throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("lowWeightPOI", 2);
        poi1.setUserIsWithin(true);
        final PlacesPOI poi2 = createPOI("highWeightPOI", 1);
        poi2.setUserIsWithin(false);

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("lowWeightPOI", poi1);
                        put("highWeightPOI", poi2);
                    }
                };
        placesState.lastEnteredPOI = poi1;
        placesState.currentPOI = poi1;
        placesState.lastExitedPOI = sampleLastExitedPOI;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("highWeightPOI", "entry"));

        // verify
        assertNotNull(returnedRegion);
        assertEquals("highWeightPOI", returnedRegion.getIdentifier());
        assertEquals(PlacesRegion.PLACE_EVENT_ENTRY, returnedRegion.getPlaceEventType());
        assertEquals(
                PlacesTestConstants.XDM.Location.EventType.ENTRY,
                returnedRegion.getExperienceEventType());

        // verify memory variables
        assertEquals("highWeightPOI", placesState.currentPOI.getIdentifier());
        assertEquals("highWeightPOI", placesState.lastEnteredPOI.getIdentifier());
        assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
        assertEquals(2, placesState.cachedPOIs.size());

        // verify if the containsUser is set correctly
        assertTrue(placesState.cachedPOIs.get("highWeightPOI").containsUser());
        assertTrue(placesState.cachedPOIs.get("highWeightPOI").containsUser());

        // verify the persistence
        assertEquals("highWeightPOI", getPersistedCurrentPOI().getIdentifier());
        assertEquals("highWeightPOI", getPersistedLastEnteredPOI().getIdentifier());
        assertEquals("lastExitedPOI", getPersistedLastExitedPOI().getIdentifier());
        assertEquals(2, getPersistedCachedPOI().size());
    }

    @Test
    public void processRegionEvent_when_regionEntryEventOfLowerWeightPOI() throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("lowWeightPOI", 2);
        poi1.setUserIsWithin(false);
        final PlacesPOI poi2 = createPOI("highWeightPOI", 1);
        poi2.setUserIsWithin(true);

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("lowWeightPOI", poi1);
                        put("highWeightPOI", poi2);
                    }
                };
        placesState.lastEnteredPOI = poi2;
        placesState.currentPOI = poi2;
        placesState.lastExitedPOI = sampleLastExitedPOI;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("lowWeightPOI", "entry"));

        // verify
        assertNotNull(returnedRegion);
        assertEquals("lowWeightPOI", returnedRegion.getIdentifier());
        assertEquals(PlacesRegion.PLACE_EVENT_ENTRY, returnedRegion.getPlaceEventType());
        assertEquals(
                PlacesTestConstants.XDM.Location.EventType.ENTRY,
                returnedRegion.getExperienceEventType());

        // verify memory variables
        assertEquals("highWeightPOI", placesState.currentPOI.getIdentifier());
        assertEquals("lowWeightPOI", placesState.lastEnteredPOI.getIdentifier());
        assertEquals("lastExitedPOI", placesState.lastExitedPOI.getIdentifier());
        assertEquals(2, placesState.cachedPOIs.size());

        // verify if the containsUser is set correctly
        assertTrue(placesState.cachedPOIs.get("lowWeightPOI").containsUser());
        assertTrue(placesState.cachedPOIs.get("highWeightPOI").containsUser());

        // verify the persistence
        assertEquals("highWeightPOI", getPersistedCurrentPOI().getIdentifier());
        assertEquals("lowWeightPOI", getPersistedLastEnteredPOI().getIdentifier());
        assertEquals("lastExitedPOI", getPersistedLastExitedPOI().getIdentifier());
        assertEquals(2, getPersistedCachedPOI().size());
    }

    @Test
    public void processRegionEvent_when_regionExitEvent() throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("poi1", 1);
        poi1.setUserIsWithin(true);
        final PlacesPOI poi2 = createPOI("poi2", 1);
        poi2.setUserIsWithin(false);

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("poi1", poi1);
                        put("poi2", poi2);
                    }
                };
        placesState.lastEnteredPOI = poi1;
        placesState.currentPOI = poi1;
        placesState.lastExitedPOI = null;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));

        // verify
        assertNotNull(returnedRegion);
        assertEquals("poi1", returnedRegion.getIdentifier());
        assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion.getPlaceEventType());
        assertEquals(
                PlacesTestConstants.XDM.Location.EventType.EXIT,
                returnedRegion.getExperienceEventType());

        // verify memory variables
        assertNull(placesState.currentPOI);
        assertEquals("poi1", placesState.lastEnteredPOI.getIdentifier());
        assertEquals("poi1", placesState.lastExitedPOI.getIdentifier());
        assertEquals(2, placesState.cachedPOIs.size());

        // verify if the containsUser is set correctly
        assertFalse(placesState.cachedPOIs.get("poi1").containsUser());
        assertFalse(placesState.cachedPOIs.get("poi2").containsUser());

        // verify the persistence
        verifyCurrentPOINotPersisted();
        assertEquals("poi1", getPersistedLastEnteredPOI().getIdentifier());
        assertEquals("poi1", getPersistedLastExitedPOI().getIdentifier());
        assertEquals(2, getPersistedCachedPOI().size());
    }

    @Test
    public void processRegionEvent_when_regionEntryExit_updatesMembershipValidUntilTtl()
            throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("poi1", 1);
        poi1.setUserIsWithin(true);
        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("poi1", poi1);
                    }
                };
        placesState.membershipTtl = 500;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));
        assertEquals(
                getUnixTimeInSeconds() + 500,
                placesState.membershipValidUntil,
                1); // verify memory variable
        assertEquals(
                getUnixTimeInSeconds() + 500,
                getPersistedMembershipValidUntilTimestamp(),
                1); // verify memory variable
    }

    @Test
    public void processRegionEvent_when_regionExitEvent_noPoiInCache() throws Exception {
        // setup in memory variables
        placesState.lastEnteredPOI = null;
        placesState.currentPOI = null;
        placesState.lastExitedPOI = null;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));

        // verify
        assertNull(returnedRegion);

        // verify memory variables
        assertNull(placesState.currentPOI);
        assertNull(placesState.lastEnteredPOI);
        assertNull(placesState.lastExitedPOI);
        assertEquals(0, placesState.cachedPOIs.size());

        // verify the persistence
        verifyCurrentPOINotPersisted();
        verifyLastEnteredPOINotPersisted();
        verifyLastExcitedPOINotPersisted();
        verifyNearbyPOINotPersisted();
    }

    @Test
    public void processRegionEvent_when_regionExitEvent_pOIAlreadyExited() throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("poi1", 1);
        poi1.setUserIsWithin(false);
        final PlacesPOI poi2 = createPOI("poi2", 1);
        poi2.setUserIsWithin(false);

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("poi1", poi1);
                        put("poi2", poi2);
                    }
                };
        placesState.lastEnteredPOI = null;
        placesState.currentPOI = null;
        placesState.lastExitedPOI = poi1;

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("poi1", "exit"));

        // verify
        assertNotNull(returnedRegion);
        assertEquals("poi1", returnedRegion.getIdentifier());
        assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion.getPlaceEventType());
        assertEquals(
                PlacesTestConstants.XDM.Location.EventType.EXIT,
                returnedRegion.getExperienceEventType());

        // verify memory variables
        assertNull(placesState.currentPOI);
        assertNull(placesState.lastEnteredPOI);
        assertEquals("poi1", placesState.lastExitedPOI.getIdentifier());
        assertEquals(2, placesState.cachedPOIs.size());

        // verify if the containsUser is set correctly
        assertFalse(placesState.cachedPOIs.get("poi1").containsUser());
        assertFalse(placesState.cachedPOIs.get("poi2").containsUser());

        // verify the persistence
        verifyCurrentPOINotPersisted();
        verifyLastEnteredPOINotPersisted();
        assertEquals("poi1", getPersistedLastExitedPOI().getIdentifier());
        assertEquals(2, getPersistedCachedPOI().size());
    }

    @Test
    public void processRegionEvent_when_regionExitEvent_weightPreferenceForNextPOI()
            throws Exception {
        // setup in memory variables
        final PlacesPOI poi1 = createPOI("lowWeight", 3);
        poi1.setUserIsWithin(true);
        final PlacesPOI poi2 = createPOI("mediumWeight", 2);
        poi2.setUserIsWithin(true);
        final PlacesPOI poi3 = createPOI("highWeight", 1);
        poi3.setUserIsWithin(true);

        placesState.cachedPOIs =
                new LinkedHashMap<String, PlacesPOI>() {
                    {
                        put("lowWeight", poi1);
                        put("mediumWeight", poi2);
                        put("highWeight", poi3);
                    }
                };

        // test
        PlacesRegion returnedRegion =
                placesState.processRegionEvent(prepareRegionEvent("mediumWeight", "exit"));

        // verify region event
        assertNotNull(returnedRegion);
        assertEquals("mediumWeight", returnedRegion.getIdentifier());
        assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion.getPlaceEventType());
        assertEquals(
                PlacesTestConstants.XDM.Location.EventType.EXIT,
                returnedRegion.getExperienceEventType());

        // verify memory variables
        assertEquals("highWeight", placesState.currentPOI.getIdentifier());
        assertEquals("mediumWeight", placesState.lastExitedPOI.getIdentifier());

        // verify if the containsUser is set correctly
        assertTrue(placesState.cachedPOIs.get("lowWeight").containsUser());
        assertFalse(placesState.cachedPOIs.get("mediumWeight").containsUser());
        assertTrue(placesState.cachedPOIs.get("highWeight").containsUser());

        // test another exit
        PlacesRegion returnedRegion2 =
                placesState.processRegionEvent(prepareRegionEvent("highWeight", "exit"));

        // verify region event
        assertNotNull(returnedRegion2);
        assertEquals("highWeight", returnedRegion2.getIdentifier());
        assertEquals(PlacesRegion.PLACE_EVENT_EXIT, returnedRegion2.getPlaceEventType());
        assertEquals(
                PlacesTestConstants.XDM.Location.EventType.EXIT,
                returnedRegion.getExperienceEventType());

        // verify memory variables
        assertEquals("lowWeight", placesState.currentPOI.getIdentifier());
        assertEquals("highWeight", placesState.lastExitedPOI.getIdentifier());

        // verify if the containsUser is set correctly
        assertTrue(placesState.cachedPOIs.get("lowWeight").containsUser());
        assertFalse(placesState.cachedPOIs.get("mediumWeight").containsUser());
        assertFalse(placesState.cachedPOIs.get("highWeight").containsUser());
    }

    // ========================================================================================
    // getPlacesSharedState
    // ========================================================================================

    @Test
    public void GetPlacesSharedState() {
        // setup
        placesState.cachedPOIs = getSampleCachePOIs();
        placesState.currentPOI = sampleCurrentPOI;
        placesState.lastEnteredPOI = sampleLastEnteredPOI;
        placesState.lastExitedPOI = sampleLastExitedPOI;
        placesState.authStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
        placesState.membershipValidUntil = getUnixTimeInSeconds() + 20; // 20 secs from now

        // test
        Map<String, Object> data = placesState.getPlacesSharedState();

        // verify
        assertNotNull(data);
        assertEquals(6, data.size());
        assertEquals(
                "currentPOI",
                ((Map<?, ?>) data.get(PlacesTestConstants.SharedStateKeys.CURRENT_POI))
                        .get(PlacesTestConstants.POIKeys.IDENTIFIER));
        assertEquals(
                "lastEnteredPOI",
                ((Map<?, ?>) data.get(PlacesTestConstants.SharedStateKeys.LAST_ENTERED_POI))
                        .get(PlacesTestConstants.POIKeys.IDENTIFIER));
        assertEquals(
                "lastExitedPOI",
                ((Map<?, ?>) data.get(PlacesTestConstants.SharedStateKeys.LAST_EXITED_POI))
                        .get(PlacesTestConstants.POIKeys.IDENTIFIER));
        assertEquals(
                2,
                ((Collection<?>) data.get(PlacesTestConstants.SharedStateKeys.NEARBYPOIS)).size());
        assertEquals(
                PlacesAuthorizationStatus.ALWAYS.stringValue(),
                data.get(PlacesTestConstants.SharedStateKeys.AUTH_STATUS));
        assertEquals(
                getUnixTimeInSeconds() + 20,
                (long) data.get(PlacesTestConstants.SharedStateKeys.VALID_UNTIL),
                1);
    }

    @Test
    public void getPlacesSharedState_when_allVariableNull() throws Exception {
        // setup
        placesState.currentPOI = null;
        placesState.lastEnteredPOI = null;
        placesState.lastExitedPOI = null;
        placesState.authStatus = null;
        placesState.membershipValidUntil = 0;

        // test
        Map<String, Object> data = placesState.getPlacesSharedState();

        // verify
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(0, (long) (data.get(PlacesTestConstants.SharedStateKeys.VALID_UNTIL)));
    }

    @Test
    public void getPlacesSharedState_clearsMembershipDataWhenInvalid() throws Exception {
        // setup
        placesState.cachedPOIs = getSampleCachePOIs();
        placesState.currentPOI = sampleCurrentPOI;
        placesState.lastEnteredPOI = sampleLastEnteredPOI;
        placesState.lastExitedPOI = sampleLastExitedPOI;
        placesState.authStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
        placesState.membershipValidUntil = getUnixTimeInSeconds() - 10; // time before now

        // test
        Map<String, Object> data = placesState.getPlacesSharedState();

        // verify
        assertNotNull(data);
        assertEquals(3, data.size());
        assertFalse(data.containsKey(PlacesTestConstants.SharedStateKeys.CURRENT_POI));
        assertFalse(data.containsKey(PlacesTestConstants.SharedStateKeys.LAST_ENTERED_POI));
        assertFalse(data.containsKey(PlacesTestConstants.SharedStateKeys.LAST_EXITED_POI));

        // nearby POIs values still persist
        assertEquals(
                2,
                ((Collection<?>) data.get(PlacesTestConstants.SharedStateKeys.NEARBYPOIS)).size());
        assertEquals(
                PlacesAuthorizationStatus.ALWAYS.stringValue(),
                data.get(PlacesTestConstants.SharedStateKeys.AUTH_STATUS));
        assertEquals(0, (long) data.get(PlacesTestConstants.SharedStateKeys.VALID_UNTIL));
    }

    // ========================================================================================
    // saveLastKnownLocation
    // ========================================================================================
    @Test
    public void saveLastKnownLocation_when_validLocation() {
        // setup
        reset(placesDataStore);

        // test
        placesState.saveLastKnownLocation(34.2, 12.3);

        // verify
        verify(placesDataStore, times(1))
                .setDouble(eq(PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LATITUDE), eq(34.2));
        verify(placesDataStore, times(1))
                .setDouble(eq(PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE), eq(12.3));
    }

    @Test
    public void saveAndLoadLastKnownLocation_when_invalidLocation() {
        // setup
        reset(placesDataStore);

        // test
        placesState.saveLastKnownLocation(355.2, -6612.3);

        // verify
        verify(placesDataStore, times(1))
                .remove(eq(PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LATITUDE));
        verify(placesDataStore, times(1))
                .remove(eq(PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE));
    }

    @Test
    public void saveLastKnownLocation_when_NoDataStore() {
        // setup
        reset(placesDataStore);
        when(dataStoring.getNamedCollection(any())).thenReturn(null);
        placesState = new PlacesState(dataStoring);

        // test
        placesState.saveLastKnownLocation(34.2, 12.3);

        // verify
        verifyNoInteractions(placesDataStore);
    }

    @Test
    public void loadLastKnownLocation_when_validLocation() {
        // setup
        double SAMPLE_LATITUDE = 11.11;
        double SAMPLE_LONGITUDE = -22.22;

        // Mock the construction of location.
        // A location object is constructed when loadLastKnownLocation method is called.
        MockedConstruction<Location> mockedLocationConstruction =
                mockConstruction(
                        Location.class,
                        (mock, context) -> {
                            when(mock.getLatitude()).thenReturn(11.11);
                            when(mock.getLongitude()).thenReturn(-22.22);
                        });
        when(placesDataStore.getDouble(
                        PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LATITUDE,
                        PlacesTestConstants.INVALID_LAT_LON))
                .thenReturn(SAMPLE_LATITUDE);
        when(placesDataStore.getDouble(
                        PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE,
                        PlacesTestConstants.INVALID_LAT_LON))
                .thenReturn(SAMPLE_LONGITUDE);

        // test
        Location location = placesState.loadLastKnownLocation();

        // verify
        assertEquals(SAMPLE_LATITUDE, location.getLatitude(), 0);
        assertEquals(SAMPLE_LONGITUDE, location.getLongitude(), 0);

        // verify that the interactions with the mocked location is correct
        // mocked location object gets set with the latitude and longitude from datastore.
        verify(mockedLocationConstruction.constructed().get(0)).setLatitude(SAMPLE_LATITUDE);
        verify(mockedLocationConstruction.constructed().get(0)).setLongitude(SAMPLE_LONGITUDE);

        mockedLocationConstruction.close();
    }

    @Test
    public void loadLastKnownLocation_when_InvalidLocation() {
        // setup
        when(placesDataStore.getDouble(
                        PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LATITUDE,
                        PlacesTestConstants.INVALID_LAT_LON))
                .thenReturn(1233.33);
        when(placesDataStore.getDouble(
                        PlacesTestConstants.DataStoreKeys.LAST_KNOWN_LONGITUDE,
                        PlacesTestConstants.INVALID_LAT_LON))
                .thenReturn(-333.3);

        // test
        Location location = placesState.loadLastKnownLocation();

        // verify
        assertNull(location);
    }

    // ========================================================================================
    // setAuthorizationStatus
    // ========================================================================================

    @Test
    public void saveLocationPermissionStatus_happy() {
        // setup
        String actualStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();

        // test
        placesState.setAuthorizationStatus(actualStatus);

        // verify
        assertEquals(actualStatus, placesState.authStatus);
        assertEquals(actualStatus, getPersistedPermissionStatus());
    }

    @Test
    public void saveLocationPermissionStatus_when_noDataStore() {
        // setup
        String actualStatus = PlacesAuthorizationStatus.ALWAYS.stringValue();
        when(dataStoring.getNamedCollection(any())).thenReturn(null);
        reset(placesDataStore);
        placesState = new PlacesState(dataStoring);

        // test
        placesState.setAuthorizationStatus(actualStatus);

        // verify
        assertEquals(actualStatus, placesState.authStatus);
        verifyAuthStatusNotPersisted();
    }

    @Test
    public void saveLocationPermissionStatus_when_nullStatus() throws Exception {
        // setup
        persistSampleData();

        // test
        placesState.setAuthorizationStatus(null);

        // verify
        assertEquals(PlacesAuthorizationStatus.UNKNOWN.stringValue(), placesState.authStatus);
        assertEquals(
                PlacesAuthorizationStatus.UNKNOWN.stringValue(), getPersistedPermissionStatus());
    }

    // ========================================================================================
    // getUserWithInPOIs
    // ========================================================================================
    @Test
    public void getUserWithInPOIs_whenNoPOIs() {
        // test and verify
        assertEquals(0, placesState.getUserWithInPOIs().size());
    }

    @Test
    public void getUserWithInPOIs_whenValidPOIs() {
        // setup
        placesState.cachedPOIs = getSampleCachePOIs();

        // test and verify
        assertEquals(1, placesState.getUserWithInPOIs().size());
        assertEquals("cachedPOI1", placesState.getUserWithInPOIs().get(0).getIdentifier());
    }

    // ========================================================================================
    // privacyOptedOut
    // ========================================================================================
    @Test
    public void clearData_clearsInMemoryVariables() {
        // setup
        placesState.cachedPOIs = getSampleCachePOIs();
        placesState.currentPOI = sampleCurrentPOI;
        placesState.lastEnteredPOI = sampleLastEnteredPOI;
        placesState.lastExitedPOI = sampleLastExitedPOI;
        placesState.membershipValidUntil = SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP;

        // test
        placesState.clearData();

        // verify
        assertEquals(0, placesState.cachedPOIs.size());
        assertNull(placesState.currentPOI);
        assertNull(placesState.lastExitedPOI);
        assertNull(placesState.lastEnteredPOI);
        assertEquals(0, placesState.membershipValidUntil);
    }

    @Test
    public void clearData_clearsPersistedVariables() throws Exception {
        // setup
        persistSampleData();

        // test
        placesState.clearData();

        // verify
        verifyCurrentPOINotPersisted();
        verifyLastEnteredPOINotPersisted();
        verifyLastExcitedPOINotPersisted();
        assertEquals(0, getPersistedMembershipValidUntilTimestamp());
        assertEquals(
                PlacesAuthorizationStatus.UNKNOWN.stringValue(), getPersistedPermissionStatus());
        verifyNearbyPOINotPersisted();
    }

    //	// ========================================================================================
    //	// Helper methods
    //	// ========================================================================================
    //
    private Event prepareRegionEvent(final String regionID, final String regionType) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(PlacesTestConstants.EventDataKeys.Places.REGION_ID, regionID);
        eventData.put(PlacesTestConstants.EventDataKeys.Places.REGION_EVENT_TYPE, regionType);
        eventData.put(
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE,
                PlacesTestConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT);
        final Event event =
                new Event.Builder(
                                "Process Region Event",
                                EventType.PLACES,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        return event;
    }

    private LinkedHashMap<String, PlacesPOI> getSampleCachePOIs() {
        LinkedHashMap<String, PlacesPOI> sampleCachedPOIs = new LinkedHashMap<String, PlacesPOI>();
        PlacesPOI poi1 = new PlacesPOI(createPOI("cachedPOI1", 2));
        poi1.setUserIsWithin(true);
        PlacesPOI poi2 = new PlacesPOI(createPOI("cachedPOI2", 2));
        poi2.setUserIsWithin(false);
        sampleCachedPOIs.put("cachedPOI1", poi1);
        sampleCachedPOIs.put("cachedPOI2", poi2);
        return sampleCachedPOIs;
    }

    private PlacesPOI createPOI(final String id, final int weight) {
        return new PlacesPOI(id, "hidden", 34.33, -121.55, 150, "libraryName", weight, null);
    }

    private void persistSampleData() {
        // cached POI∂
        final LinkedHashMap<String, PlacesPOI> cachedPOIs = getSampleCachePOIs();
        try {
            final JSONObject nearbyPOIsJSON = new JSONObject();
            for (final String poiID : cachedPOIs.keySet()) {
                nearbyPOIsJSON.put(poiID, new JSONObject(cachedPOIs.get(poiID).toMap()));
            }
            final String jsonString = nearbyPOIsJSON.toString();
            when(placesDataStore.getString(PlacesTestConstants.DataStoreKeys.NEARBYPOIS, ""))
                    .thenReturn(jsonString);
        } catch (final JSONException e) {
        }

        // current POI
        when(placesDataStore.getString(PlacesTestConstants.DataStoreKeys.CURRENT_POI, ""))
                .thenReturn(sampleCurrentPOI.toJsonString());

        // lastEntered POI
        when(placesDataStore.getString(PlacesTestConstants.DataStoreKeys.LAST_ENTERED_POI, ""))
                .thenReturn(sampleLastEnteredPOI.toJsonString());

        // lastExited POI
        when(placesDataStore.getString(PlacesTestConstants.DataStoreKeys.LAST_EXITED_POI, ""))
                .thenReturn(sampleLastExitedPOI.toJsonString());

        when(placesDataStore.getString(
                        PlacesTestConstants.DataStoreKeys.AUTH_STATUS,
                        PlacesAuthorizationStatus.DEFAULT_VALUE))
                .thenReturn(PlacesAuthorizationStatus.ALWAYS.stringValue());

        when(placesDataStore.getLong(PlacesTestConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL, 0))
                .thenReturn(SAMPLE_MEMBERSHIP_VALID_UNTIL_TIMESTAMP);
    }

    private LinkedHashMap<String, PlacesPOI> getPersistedCachedPOI() throws JSONException {
        ArgumentCaptor<String> persistenceValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(placesDataStore, times(1))
                .setString(
                        eq(PlacesTestConstants.DataStoreKeys.NEARBYPOIS),
                        persistenceValueCaptor.capture());
        final LinkedHashMap<String, PlacesPOI> cachedPOIs = new LinkedHashMap<>();
        final String nearbyString = persistenceValueCaptor.getValue();
        final JSONObject nearbyJSON = new JSONObject(nearbyString);
        final Iterator<String> keys = nearbyJSON.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            cachedPOIs.put(key, new PlacesPOI(nearbyJSON.getJSONObject(key)));
        }
        return cachedPOIs;
    }

    private PlacesPOI getPersistedLastEnteredPOI() throws JSONException {
        ArgumentCaptor<String> persistenceValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(placesDataStore, times(1))
                .setString(
                        eq(PlacesTestConstants.DataStoreKeys.LAST_ENTERED_POI),
                        persistenceValueCaptor.capture());
        return new PlacesPOI((persistenceValueCaptor.getValue()));
    }

    private PlacesPOI getPersistedLastExitedPOI() throws JSONException {
        ArgumentCaptor<String> persistenceValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(placesDataStore, times(1))
                .setString(
                        eq(PlacesTestConstants.DataStoreKeys.LAST_EXITED_POI),
                        persistenceValueCaptor.capture());
        return new PlacesPOI((persistenceValueCaptor.getValue()));
    }

    private PlacesPOI getPersistedCurrentPOI() throws JSONException {
        ArgumentCaptor<String> persistenceValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(placesDataStore, times(1))
                .setString(
                        eq(PlacesTestConstants.DataStoreKeys.CURRENT_POI),
                        persistenceValueCaptor.capture());
        return new PlacesPOI((persistenceValueCaptor.getValue()));
    }

    private String getPersistedPermissionStatus() {
        ArgumentCaptor<String> persistenceValueCaptor = ArgumentCaptor.forClass(String.class);
        verify(placesDataStore, times(1))
                .setString(
                        eq(PlacesTestConstants.DataStoreKeys.AUTH_STATUS),
                        persistenceValueCaptor.capture());
        return persistenceValueCaptor.getValue();
    }

    private long getPersistedMembershipValidUntilTimestamp() {
        ArgumentCaptor<Long> persistenceValueCaptor = ArgumentCaptor.forClass(Long.class);
        verify(placesDataStore, times(1))
                .setLong(
                        eq(PlacesTestConstants.DataStoreKeys.MEMBERSHIP_VALID_UNTIL),
                        persistenceValueCaptor.capture());
        return persistenceValueCaptor.getValue();
    }

    private void verifyLastEnteredPOINotPersisted() {
        verify(placesDataStore, times(0))
                .setString(eq(PlacesTestConstants.DataStoreKeys.LAST_ENTERED_POI), any());
    }

    private void verifyCurrentPOINotPersisted() {
        verify(placesDataStore, times(0))
                .setString(eq(PlacesTestConstants.DataStoreKeys.CURRENT_POI), any());
    }

    private void verifyLastExcitedPOINotPersisted() {
        verify(placesDataStore, times(0))
                .setString(eq(PlacesTestConstants.DataStoreKeys.LAST_EXITED_POI), any());
    }

    private void verifyNearbyPOINotPersisted() {
        verify(placesDataStore, times(0))
                .setString(eq(PlacesTestConstants.DataStoreKeys.NEARBYPOIS), any());
    }

    private void verifyAuthStatusNotPersisted() {
        verify(placesDataStore, times(0))
                .setString(eq(PlacesTestConstants.DataStoreKeys.AUTH_STATUS), any());
    }

    private PlacesQueryResponse GetSampleSuccessPlacesResponse(
            final int containsUserPOICount, final int nearByPOIsCount) {
        PlacesQueryResponse response = new PlacesQueryResponse();
        response.containsUserPOIs = new ArrayList<PlacesPOI>();

        for (int i = 0; i < containsUserPOICount; i++) {
            response.containsUserPOIs.add(
                    new PlacesPOI(
                            "containsUserPOI " + i,
                            "hidden",
                            34.33,
                            -121.55,
                            150,
                            "libraryName",
                            2,
                            null));
        }

        response.nearByPOIs = new ArrayList<PlacesPOI>();

        for (int i = 0; i < nearByPOIsCount; i++) {
            response.nearByPOIs.add(
                    new PlacesPOI(
                            "nearByPOI" + i,
                            "hidden",
                            34.33,
                            -121.55,
                            150,
                            "libraryName",
                            2,
                            null));
        }

        response.isSuccess = true;
        return response;
    }

    private long getUnixTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
