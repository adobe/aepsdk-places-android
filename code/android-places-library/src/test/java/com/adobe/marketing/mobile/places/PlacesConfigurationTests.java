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
package com.adobe.marketing.mobile;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.*;

public class PlacesConfigurationTests extends BaseTest {

	private static String SAMPLE_ENDPOINT 		= "serverEndpoint";
	private static long SAMPLE_MEMBERSHIP_TTL   = 2343;

	@Before
	public void testSetup() {
		super.beforeEach();
	}

	@Test
	public void testConfiguration_emptyConfigEventData() throws Exception {
		// test
		PlacesConfiguration configuration = new PlacesConfiguration(null, platformServices);

		// verify
		assertFalse(configuration.isValid());
		assertNull(configuration.getEndpoint());
	}

	@Test
	public void testConfiguration_emptyServices() throws Exception {
		// setup
		EventData eventData = createConfigData(2, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, null);
		platformServices.fakeJsonUtilityService = null;

		// verify
		assertFalse(configuration.isValid());
		assertNull(configuration.getEndpoint());
	}

	@Test
	public void testConfiguration_emptyJSONServices() throws Exception {
		// setup
		EventData eventData = createConfigData(2, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

		// test
		platformServices.fakeJsonUtilityService = null;
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertFalse(configuration.isValid());
		assertNull(configuration.getEndpoint());
	}

	@Test
	public void testConfiguration_NoLibraryKey() throws Exception {
		// setup
		EventData configData = new EventData();
		configData.putString(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, SAMPLE_ENDPOINT);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(configData, platformServices);

		// verify
		assertFalse(configuration.isValid());
		assertNull(configuration.getEndpoint());
	}

	@Test
	public void testConfiguration_EmptyLibraries() throws Exception {
		// setup
		EventData eventData = createConfigData(0, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertFalse(configuration.isValid());
		assertNull(configuration.getEndpoint());
	}


	@Test
	public void testConfiguration_WithOneLibrary() throws Exception {
		// setup
		EventData eventData = createConfigData(1, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertTrue(configuration.isValid());
		assertEquals(SAMPLE_ENDPOINT, configuration.getEndpoint());
		assertEquals("&library=lib1", configuration.getLibrariesQueryString());
	}

	@Test
	public void testConfiguration_WithFiveLibrary() throws Exception {
		// setup
		EventData eventData = createConfigData(5, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertTrue(configuration.isValid());
		assertEquals(SAMPLE_ENDPOINT, configuration.getEndpoint());
		assertEquals("&library=lib1&library=lib2&library=lib3&library=lib4&library=lib5",
					 configuration.getLibrariesQueryString());
	}


	@Test
	public void testConfiguration_When_EmptyEndpointValue() throws Exception {
		// setup
		EventData eventData = createConfigData(2, "", SAMPLE_MEMBERSHIP_TTL);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertFalse(configuration.isValid());
		assertEquals("", configuration.getEndpoint());
	}


	@Test
	public void testConfiguration_WhenMembershipTtlIsNegative() throws Exception {
		// setup
		EventData eventData = createConfigData(2, SAMPLE_ENDPOINT, -200);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertTrue(configuration.isValid());
		assertEquals(-200, configuration.getMembershipTtl());
	}

	@Test
	public void testConfiguration_WhenMembershipTtlIsZero() throws Exception {
		// setup
		EventData eventData = createConfigData(2, SAMPLE_ENDPOINT, 0);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertTrue(configuration.isValid());
		assertEquals(0, configuration.getMembershipTtl());
	}


	@Test
	public void testConfiguration_WhenMembershipTtlNotPresent() throws Exception {
		// test
		EventData configData = new EventData();
		Map<String, String> library = new HashMap<String, String>();
		library.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "lib1");
		List<Variant> libraries = new ArrayList<Variant>();
		libraries.add(Variant.fromStringMap(library));
		configData.putVariantList(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, libraries);
		configData.putString(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, SAMPLE_ENDPOINT);

		PlacesConfiguration configuration = new PlacesConfiguration(configData, platformServices);

		// verify
		assertTrue(configuration.isValid());
		assertEquals(PlacesTestConstants.DEFAULT_MEMBERSHIP_TTL, configuration.getMembershipTtl());
	}


	@Test
	public void testConfiguration_Happy() throws Exception {
		// setup
		EventData eventData = createConfigData(2, SAMPLE_ENDPOINT, SAMPLE_MEMBERSHIP_TTL);

		// test
		PlacesConfiguration configuration = new PlacesConfiguration(eventData, platformServices);

		// verify
		assertTrue(configuration.isValid());
		assertEquals(SAMPLE_ENDPOINT, configuration.getEndpoint());
		assertEquals(SAMPLE_MEMBERSHIP_TTL, configuration.getMembershipTtl());
		assertEquals("&library=lib1&library=lib2", configuration.getLibrariesQueryString());
	}


	private EventData createConfigData(final int noOfLibraries, final String endPoint, final long membershipTtl) {

		List<Variant> libraries = new ArrayList<Variant>();

		for (int i = 0; i < noOfLibraries; i++) {
			Map<String, String> library = new HashMap<String, String>();
			library.put(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "lib" + (i + 1));
			libraries.add(Variant.fromStringMap(library));
		}

		EventData configData = new EventData();
		configData.putVariantList(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, libraries);
		configData.putString(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, endPoint);
		configData.putLong(PlacesTestConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL, membershipTtl);
		return configData;
	}


}
