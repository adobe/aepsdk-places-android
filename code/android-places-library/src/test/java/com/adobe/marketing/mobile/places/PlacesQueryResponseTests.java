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

package com.adobe.marketing.mobile.places;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

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
