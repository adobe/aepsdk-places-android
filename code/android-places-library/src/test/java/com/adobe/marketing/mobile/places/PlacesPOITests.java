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

import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;


public class PlacesPOITests {

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


	@Test
	public void test_Getters() throws Exception {
		// setup
		PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									  SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		poi.setUserIsWithin(true);

		// verify
		assertEquals(SAMPLE_IDENTIFIER, poi.getIdentifier());
		assertEquals(SAMPLE_NAME, poi.getName());
		assertEquals(SAMPLE_LATITUDE, poi.getLatitude(), 0.0);
		assertEquals(SAMPLE_LONGITUDE, poi.getLongitude(), 0.0);
		assertEquals(SAMPLE_RADIUS, poi.getRadius(), 0.0);
		assertEquals(SAMPLE_METADATA, poi.getMetadata());
		assertEquals(SAMPLE_LIBRARY, poi.getLibrary());
		assertEquals(SAMPLE_WEIGHT, poi.getWeight());
		assertTrue(poi.containsUser());
	}

	@Test
	public void test_Setters() throws Exception {
		// setup
		PlacesPOI poi = new PlacesPOI("id2", "", 0, 0, 0, "", 0, null);


		// verify
		assertEquals("id2", poi.getIdentifier());
		assertEquals("", poi.getName());
		assertEquals(0, poi.getLatitude(), 0.0);
		assertEquals(0, poi.getLongitude(), 0.0);
		assertEquals(0, poi.getRadius(), 0.0);
		assertEquals(0, poi.getWeight());
		assertEquals("", poi.getLibrary());
		assertNull(poi.getMetadata());

		// test
		poi.setIdentifier(SAMPLE_IDENTIFIER);
		poi.setName(SAMPLE_NAME);
		poi.setLatitude(SAMPLE_LATITUDE);
		poi.setLongitude(SAMPLE_LONGITUDE);
		poi.setRadius(SAMPLE_RADIUS);
		poi.setMetadata(SAMPLE_METADATA);
		poi.setUserIsWithin(true);

		// verify
		assertEquals(SAMPLE_IDENTIFIER, poi.getIdentifier());
		assertEquals(SAMPLE_NAME, poi.getName());
		assertEquals(SAMPLE_LATITUDE, poi.getLatitude(), 0.0);
		assertEquals(SAMPLE_LONGITUDE, poi.getLongitude(), 0.0);
		assertEquals(SAMPLE_RADIUS, poi.getRadius(), 0.0);
		assertEquals(SAMPLE_METADATA, poi.getMetadata());
		assertTrue(poi.containsUser());
	}

	@Test
	public void test_Equals() {
		// setup
		PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									  SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiSame = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
										  SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiIDChange = new PlacesPOI("idChange", SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
											  SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiNameChange = new PlacesPOI(SAMPLE_IDENTIFIER, "name", SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
												SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiLatChange = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, 32.44, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
											   SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiLongChange = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, 50.44, SAMPLE_RADIUS,
												SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiRadiusChange = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, 600,
				SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		PlacesPOI poiMetadataChange = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE,
				SAMPLE_RADIUS,
				SAMPLE_LIBRARY, SAMPLE_WEIGHT, null);
		PlacesPOI poiWeightChange = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE,
				SAMPLE_RADIUS,
				SAMPLE_LIBRARY, 2222, SAMPLE_METADATA);
		PlacesPOI poiLibraryChange = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE,
				SAMPLE_RADIUS,
				"librarycbahge", SAMPLE_WEIGHT, SAMPLE_METADATA);

		PlacesPOI poiDitto = new PlacesPOI(poi);

		// verify
		assertTrue(poi.equals(poiSame));
		assertTrue(poi.equals(poi));
		assertTrue(poi.equals(poiDitto));
		assertFalse(poi.equals(null));
		assertFalse(poi.equals(poiIDChange));
		assertFalse(poi.equals(poiNameChange));
		assertFalse(poi.equals(poiLatChange));
		assertFalse(poi.equals(poiLongChange));
		assertFalse(poi.equals(poiRadiusChange));
		assertFalse(poi.equals(poiWeightChange));
		assertFalse(poi.equals(poiLibraryChange));
		assertFalse(poi.equals(poiMetadataChange));
	}

	@Test
	public void test_EqualsWithoutMetaData() {
		// setup
		PlacesPOI poi1 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									   SAMPLE_LIBRARY, SAMPLE_WEIGHT, SAMPLE_METADATA);
		poi1.setUserIsWithin(true);
		PlacesPOI poi2 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									   SAMPLE_LIBRARY, SAMPLE_WEIGHT, null);
		poi2.setUserIsWithin(true);
		PlacesPOI poi3 = new PlacesPOI("idChange", SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									   SAMPLE_LIBRARY, SAMPLE_WEIGHT, null);
		PlacesPOI poi4 = new PlacesPOI(SAMPLE_IDENTIFIER, "name", SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									   SAMPLE_LIBRARY, SAMPLE_WEIGHT, null);
		PlacesPOI poi5 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, 32.44, SAMPLE_LONGITUDE, SAMPLE_RADIUS, SAMPLE_LIBRARY,
									   SAMPLE_WEIGHT, null);
		PlacesPOI poi6 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, 50.44, SAMPLE_RADIUS, SAMPLE_LIBRARY,
									   SAMPLE_WEIGHT, null);
		PlacesPOI poi7 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, 600, SAMPLE_LIBRARY,
									   SAMPLE_WEIGHT, null);
		PlacesPOI poi8 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									   "libChange", SAMPLE_WEIGHT, null);
		PlacesPOI poi9 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									   SAMPLE_LIBRARY, 300, null);
		PlacesPOI poi10 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
										SAMPLE_LIBRARY, 300, null);
		poi10.setUserIsWithin(false);

		PlacesPOI poi11 = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
										SAMPLE_LIBRARY, 300, null);
		poi11.setWeight(3);

		// verify
		assertTrue(poi1.equalsWithOutMetaData(poi2));
		assertTrue(poi1.equalsWithOutMetaData(poi1));
		assertFalse(poi1.equalsWithOutMetaData(poi3));
		assertFalse(poi1.equalsWithOutMetaData(poi4));
		assertFalse(poi1.equalsWithOutMetaData(poi5));
		assertFalse(poi1.equalsWithOutMetaData(poi6));
		assertFalse(poi1.equalsWithOutMetaData(poi7));
		assertFalse(poi1.equalsWithOutMetaData(poi8));
		assertFalse(poi1.equalsWithOutMetaData(poi9));
		assertFalse(poi1.equalsWithOutMetaData(poi10));
		assertFalse(poi1.equalsWithOutMetaData(poi11));

	}

	@Test
	public void test_HashCode() {
		PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, SAMPLE_LATITUDE, SAMPLE_LONGITUDE, SAMPLE_RADIUS,
									  SAMPLE_LIBRARY, SAMPLE_WEIGHT);
		assertNotNull(poi.hashCode());

	}

	// ========================================================================================
	// comparePriority
	// ========================================================================================
	// Rule : Return true if the calling object has higher priority than the parameter passed in
	@Test
	public void test_comparePriority_when_differentWeight() {
		// setup
		PlacesPOI highPriorityPOI = new PlacesPOI("id2", "", 0, 0, 0, "", 22, null);
		PlacesPOI lowPriorityPOI  = new PlacesPOI("id2", "", 0, 0, 0, "", 33, null);

		// test
		assertTrue(highPriorityPOI.comparePriority(lowPriorityPOI));
		assertFalse(lowPriorityPOI.comparePriority(highPriorityPOI));
	}

	@Test
	public void test_comparePriority_when_sameWeightDiffRadius() {
		// setup
		PlacesPOI highPriorityPOI = new PlacesPOI("id2", "", 0, 0, 20, "", 30, null);
		PlacesPOI lowPriorityPOI = new PlacesPOI("id2", "", 0, 0, 30, "", 30, null);

		// test
		assertTrue(highPriorityPOI.comparePriority(lowPriorityPOI));
		assertFalse(lowPriorityPOI.comparePriority(highPriorityPOI));
	}

	@Test
	public void test_comparePriority_when_sameWeightSameRadius() {
		// setup
		PlacesPOI poi = new PlacesPOI("id2", "", 0, 0, 30, "", 30, null);
		PlacesPOI otherPOI = new PlacesPOI("id2", "", 0, 0, 30, "", 30, null);

		// test
		assertTrue(poi.comparePriority(otherPOI));
		assertTrue(otherPOI.comparePriority(poi));
	}

	@Test
	public void test_comparePriority_when_null() {
		// setup
		PlacesPOI poi = new PlacesPOI("id2", "", 0, 0, 0, "", 22, null);

		// test
		assertTrue(poi.comparePriority(null));
	}

}
