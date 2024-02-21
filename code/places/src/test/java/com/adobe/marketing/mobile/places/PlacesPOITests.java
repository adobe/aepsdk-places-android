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

import java.util.*;
import org.json.JSONException;
import org.junit.Test;

public class PlacesPOITests {

	private static String SAMPLE_IDENTIFIER = "8d7c29b6-f900-4acc-9efe-9338aa837090";
	private static String SAMPLE_NAME = "My House";
	private static double SAMPLE_LATITUDE = 33.44;
	private static double SAMPLE_LONGITUDE = -55.66;
	private static int SAMPLE_RADIUS = 300;
	private static String SAMPLE_LIBRARY = "03d0193c-2b23-44d4-bd40-afc42e24eeea";
	private static int SAMPLE_WEIGHT = 200;
	private static Map<String, String> SAMPLE_METADATA = new HashMap<String, String>() {
		{
			put("city", "pity");
			put("state", "ate");
		}
	};

	private static String VALID_POI_JSON =
		"{\n" +
		"   \"regionid\": \"" +
		SAMPLE_IDENTIFIER +
		"\",\n" +
		"   \"useriswithin\":true,\n" +
		"   \"latitude\": " +
		SAMPLE_LATITUDE +
		",\n" +
		"   \"libraryid\": \"" +
		SAMPLE_LIBRARY +
		"\",\n" +
		"   \"regionname\":\"" +
		SAMPLE_NAME +
		"\",\n" +
		"   \"weight\":" +
		SAMPLE_WEIGHT +
		" \n," +
		"   \"regionmetadata\":{\n" +
		"      \"country\":\"US\",\n" +
		"      \"ownership\":\"CO\",\n" +
		"      \"city\":\"San Jose\",\n" +
		"      \"street\":\"540 Newhall Drive, 20\",\n" +
		"      \"state\":\"CA\",\n" +
		"      \"category\":\"\",\n" +
		"      \"brand\":\"Starbucks\"\n" +
		"   },\n" +
		"   \"radius\":" +
		SAMPLE_RADIUS +
		",\n" +
		"   \"longitude\":" +
		SAMPLE_LONGITUDE +
		"\n" +
		"}";

	@Test(expected = JSONException.class)
	public void test_Constructor_JSONStringEmpty() throws Exception {
		new PlacesPOI("");
	}

	@Test(expected = JSONException.class)
	public void test_Constructor_JSONStringInvalid() throws Exception {
		new PlacesPOI("$&W66w");
	}

	@Test
	public void test_Constructor_withJSONString() throws Exception {
		PlacesPOI poi = new PlacesPOI(VALID_POI_JSON);
		assertEquals(SAMPLE_IDENTIFIER, poi.getIdentifier());
		assertEquals(SAMPLE_NAME, poi.getName());
		assertEquals(SAMPLE_LATITUDE, poi.getLatitude(), 0.0);
		assertEquals(SAMPLE_LONGITUDE, poi.getLongitude(), 0.0);
		assertEquals(SAMPLE_RADIUS, poi.getRadius(), 0.0);
		assertEquals(7, poi.getMetadata().size());
		assertEquals(SAMPLE_LIBRARY, poi.getLibrary());
		assertEquals(SAMPLE_WEIGHT, poi.getWeight());
		assertTrue(poi.containsUser());
	}

	@Test
	public void test_Getters() {
		// setup
		PlacesPOI poi = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
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
	public void test_Setters() {
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
		PlacesPOI poi = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiSame = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiIDChange = new PlacesPOI(
			"idChange",
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiNameChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			"name",
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiLatChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			32.44,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiLongChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			50.44,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiRadiusChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			600,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		PlacesPOI poiMetadataChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poiWeightChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			2222,
			SAMPLE_METADATA
		);
		PlacesPOI poiLibraryChange = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			"librarycbahge",
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);

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
		PlacesPOI poi1 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);
		poi1.setUserIsWithin(true);
		PlacesPOI poi2 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		poi2.setUserIsWithin(true);
		PlacesPOI poi3 = new PlacesPOI(
			"idChange",
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poi4 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			"name",
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poi5 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			32.44,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poi6 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			50.44,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poi7 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			600,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poi8 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			"libChange",
			SAMPLE_WEIGHT,
			null
		);
		PlacesPOI poi9 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			300,
			null
		);
		PlacesPOI poi10 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			300,
			null
		);
		poi10.setUserIsWithin(false);

		PlacesPOI poi11 = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			300,
			null
		);
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
		PlacesPOI poi = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT
		);
		assertNotNull(poi.hashCode());
		assertEquals(poi.hashCode(), poi.hashCode());
	}

	// ========================================================================================
	// comparePriority
	// ========================================================================================
	// Rule : Return true if the calling object has higher priority than the parameter passed in
	@Test
	public void test_comparePriority_when_differentWeight() {
		// setup
		PlacesPOI highPriorityPOI = new PlacesPOI("id2", "", 0, 0, 0, "", 22, null);
		PlacesPOI lowPriorityPOI = new PlacesPOI("id2", "", 0, 0, 0, "", 33, null);

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

	@Test
	public void test_toMap() {
		// setup
		PlacesPOI poi = new PlacesPOI(
			SAMPLE_IDENTIFIER,
			SAMPLE_NAME,
			SAMPLE_LATITUDE,
			SAMPLE_LONGITUDE,
			SAMPLE_RADIUS,
			SAMPLE_LIBRARY,
			SAMPLE_WEIGHT,
			SAMPLE_METADATA
		);

		// test
		assertEquals(SAMPLE_IDENTIFIER, poi.toMap().get(PlacesTestConstants.POIKeys.IDENTIFIER));
		assertEquals(SAMPLE_NAME, poi.toMap().get(PlacesTestConstants.POIKeys.NAME));
		assertEquals(SAMPLE_LATITUDE, poi.toMap().get(PlacesTestConstants.POIKeys.LATITUDE));
		assertEquals(SAMPLE_LONGITUDE, poi.toMap().get(PlacesTestConstants.POIKeys.LONGITUDE));
		assertEquals(SAMPLE_RADIUS, poi.toMap().get(PlacesTestConstants.POIKeys.RADIUS));
		assertEquals(SAMPLE_LIBRARY, poi.toMap().get(PlacesTestConstants.POIKeys.LIBRARY));
		assertEquals(SAMPLE_WEIGHT, poi.toMap().get(PlacesTestConstants.POIKeys.WEIGHT));
		assertEquals(SAMPLE_METADATA, poi.toMap().get(PlacesTestConstants.POIKeys.METADATA));
	}

	@Test
	public void test_toMap_nullMetaData() {
		// setup
		PlacesPOI poi = new PlacesPOI(SAMPLE_IDENTIFIER, SAMPLE_NAME, 0.00, 999.999, 0, null, SAMPLE_WEIGHT, null);

		// test
		assertEquals(0.00, poi.toMap().get(PlacesTestConstants.POIKeys.LATITUDE));
		assertEquals(999.999, poi.toMap().get(PlacesTestConstants.POIKeys.LONGITUDE));
		assertEquals(0, poi.toMap().get(PlacesTestConstants.POIKeys.RADIUS));
		assertNull(poi.toMap().get(PlacesTestConstants.POIKeys.LIBRARY));
		assertNull(poi.toMap().get(PlacesTestConstants.POIKeys.METADATA));
	}
}
