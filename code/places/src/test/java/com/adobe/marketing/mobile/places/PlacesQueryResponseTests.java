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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlacesQueryResponseTests {

    @Test
    public void test_FetchFailed() {
        // setup
        String errorMessage = "This error makes your computer fly";
        PlacesQueryResponse response = new PlacesQueryResponse();

        // test
        response.fetchFailed(errorMessage, PlacesRequestError.CONNECTIVITY_ERROR);

        // verify
        assertFalse(response.isSuccess);
        assertEquals("This error makes your computer fly", response.errorMessage);
        assertEquals(PlacesRequestError.CONNECTIVITY_ERROR, response.resultStatus);
    }

    @Test
    public void test_getters() {
        // setup
        PlacesQueryResponse response = new PlacesQueryResponse();
        PlacesPOI poi1 = new PlacesPOI("id1", "name1", 0.0, 0.0, 0, "lib", 10);
        PlacesPOI poi2 = new PlacesPOI("id2", "name2", 0.0, 0.0, 0, "lib2", 20);

        List<PlacesPOI> containsUserPOIS = new ArrayList<PlacesPOI>();
        containsUserPOIS.add(poi1);
        containsUserPOIS.add(poi2);

        PlacesPOI poi3 = new PlacesPOI("id3", "name3", 0.0, 0.0, 0, "lib", 10);
        PlacesPOI poi4 = new PlacesPOI("id4", "name4", 0.0, 0.0, 0, "lib2", 10);

        List<PlacesPOI> nearbyPOIS = new ArrayList<PlacesPOI>();
        nearbyPOIS.add(poi3);
        nearbyPOIS.add(poi4);

        response.nearByPOIs = nearbyPOIS;
        response.containsUserPOIs = containsUserPOIS;

        // test
        assertEquals(containsUserPOIS, response.getContainsUserPOIs());
        assertEquals(nearbyPOIS, response.getNearByPOIs());
        assertEquals(4, response.getAllPOIs().size());
    }
}
