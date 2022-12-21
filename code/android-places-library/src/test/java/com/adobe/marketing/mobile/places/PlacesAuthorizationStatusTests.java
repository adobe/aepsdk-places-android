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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlacesAuthorizationStatusTests {

    @Test
    public void test_always() {
        // test get
        assertEquals(PlacesAuthorizationStatus.ALWAYS, PlacesAuthorizationStatus.get("always"));

        // test isValid
        assertTrue(PlacesAuthorizationStatus.isValidStatus("always"));

        // test toString
        assertEquals("always", PlacesAuthorizationStatus.ALWAYS.stringValue());
    }

    @Test
    public void test_whenInUse() {
        // test get
        assertEquals(PlacesAuthorizationStatus.WHEN_IN_USE, PlacesAuthorizationStatus.get("wheninuse"));

        // test isValid
        assertTrue(PlacesAuthorizationStatus.isValidStatus("wheninuse"));

        // test toString
        assertEquals("wheninuse", PlacesAuthorizationStatus.WHEN_IN_USE.stringValue());
    }

    @Test
    public void test_denied() {
        // test get
        assertEquals(PlacesAuthorizationStatus.DENIED, PlacesAuthorizationStatus.get("denied"));

        // test isValid
        assertTrue(PlacesAuthorizationStatus.isValidStatus("denied"));

        // test toString
        assertEquals("denied", PlacesAuthorizationStatus.DENIED.stringValue());
    }

    @Test
    public void test_unknown() {
        // test get
        assertEquals(PlacesAuthorizationStatus.UNKNOWN, PlacesAuthorizationStatus.get("unknown"));

        // test isValid
        assertTrue(PlacesAuthorizationStatus.isValidStatus("unknown"));

        // test toString
        assertEquals("unknown", PlacesAuthorizationStatus.UNKNOWN.stringValue());
    }

    @Test
    public void test_restricted() {
        // test get
        assertEquals(PlacesAuthorizationStatus.RESTRICTED, PlacesAuthorizationStatus.get("restricted"));

        // test isValid
        assertTrue(PlacesAuthorizationStatus.isValidStatus("restricted"));

        // test toString
        assertEquals("restricted", PlacesAuthorizationStatus.RESTRICTED.stringValue());
    }

    @Test
    public void test_invalidString() {
        // test get
        assertEquals(null, PlacesAuthorizationStatus.get("invalidString"));

        // test isValid
        assertFalse(PlacesAuthorizationStatus.isValidStatus("invalidString"));
    }
}
