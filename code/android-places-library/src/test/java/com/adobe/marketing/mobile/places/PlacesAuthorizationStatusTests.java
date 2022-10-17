/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Adobe Inc.
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
