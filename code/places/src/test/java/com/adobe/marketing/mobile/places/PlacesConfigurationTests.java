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

import static org.junit.Assert.*;

import java.util.*;

public class PlacesConfigurationTests {

    private static String SAMPLE_ENDPOINT = "serverEndpoint";
    private static long SAMPLE_MEMBERSHIP_TTL = 2343;

    @Test
    public void testConfiguration_emptyConfigEventData() {
        // test
        PlacesConfiguration configuration = new PlacesConfiguration(null);

        // verify
        assertFalse(configuration.isValid());
        assertNull(configuration.getEndpoint());
    }

    @Test
    public void testConfiguration_NoLibraryKey() {
        // setup
        Map<String, Object> configData = new HashMap<>();
        configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, SAMPLE_ENDPOINT);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(configData);

        // verify
        assertFalse(configuration.isValid());
        assertNull(configuration.getEndpoint());
    }

    @Test
    public void testConfiguration_EmptyLibraries() {
        // setup
        final Map<String, Object> eventData = createConfigData(0, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertFalse(configuration.isValid());
        assertNull(configuration.getEndpoint());
    }


    @Test
    public void testConfiguration_WithOneLibrary() {
        // setup
        final Map<String, Object> eventData = createConfigData(1, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertTrue(configuration.isValid());
        assertEquals(SAMPLE_ENDPOINT, configuration.getEndpoint());
        assertEquals("&library=lib1", configuration.getLibrariesQueryString());
    }

    @Test
    public void testConfiguration_WithFiveLibrary() {
        // setup
        final Map<String, Object> eventData = createConfigData(5, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertTrue(configuration.isValid());
        assertEquals(SAMPLE_ENDPOINT, configuration.getEndpoint());
        assertEquals("&library=lib1&library=lib2&library=lib3&library=lib4&library=lib5",
                configuration.getLibrariesQueryString());
    }


    @Test
    public void testConfiguration_When_EmptyEndpointValue() {
        // setup
        final Map<String, Object> eventData = createConfigData(2, "", SAMPLE_MEMBERSHIP_TTL);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertFalse(configuration.isValid());
        assertEquals("", configuration.getEndpoint());
    }


    @Test
    public void testConfiguration_WhenMembershipTtlIsNegative() {
        // setup
        final Map<String, Object> eventData = createConfigData(2, SAMPLE_ENDPOINT, -200);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertTrue(configuration.isValid());
        assertEquals(-200, configuration.getMembershipTtl());
    }

    @Test
    public void testConfiguration_WhenMembershipTtlIsZero() {
        // setup
        final Map<String, Object> eventData = createConfigData(2, SAMPLE_ENDPOINT, 0);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertTrue(configuration.isValid());
        assertEquals(0, configuration.getMembershipTtl());
    }


    @Test
    public void testConfiguration_WhenMembershipTtlNotPresent() {
        // test
        final Map<String, Object> configData = new HashMap<>();
        Map<String, String> library = new HashMap<String, String>();
        library.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "lib1");
        List<Map<String, String>> libraries = new ArrayList<>();
        libraries.add(library);
        configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, libraries);
        configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, SAMPLE_ENDPOINT);

        PlacesConfiguration configuration = new PlacesConfiguration(configData);

        // verify
        assertTrue(configuration.isValid());
        assertEquals(PlacesTestConstants.DEFAULT_MEMBERSHIP_TTL, configuration.getMembershipTtl());
    }

    @Test
    public void testConfiguration_Happy() {
        // setup
        final Map<String, Object> eventData = createConfigData(2, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

        // test
        PlacesConfiguration configuration = new PlacesConfiguration(eventData);

        // verify
        assertTrue(configuration.isValid());
        assertEquals(SAMPLE_ENDPOINT, configuration.getEndpoint());
        assertEquals(SAMPLE_MEMBERSHIP_TTL, configuration.getMembershipTtl());
        assertEquals("&library=lib1&library=lib2", configuration.getLibrariesQueryString());
    }


    private Map<String, Object> createConfigData(final int noOfLibraries, final String endPoint, final long membershipTtl) {

        List<Map<String, String>> libraries = new ArrayList<>();

        for (int i = 0; i < noOfLibraries; i++) {
            Map<String, String> library = new HashMap<>();
            library.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "lib" + (i + 1));
            libraries.add(library);
        }

        final Map<String, Object> configData = new HashMap<>();
        configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, libraries);
        configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, endPoint);
        configData.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL, membershipTtl);
        return configData;
    }


}
