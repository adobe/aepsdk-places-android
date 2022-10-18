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

import static org.junit.Assert.assertEquals;


public class PlacesGPSLocationTests {

	// ========================================================================================
	// Constructor
	// ========================================================================================
	@Test
	public void placesGpsLocation_constructor() {
		// test
		PlacesGpsLocation location = new PlacesGpsLocation(22.22, 33.33);

		// verify
		assertEquals(22.22, location.getLatitude(), 0);
		assertEquals(33.33, location.getLongitude(), 0);
		assertEquals(TimeUtil.getUnixTimeInSeconds(), location.getTimestamp(), 1);
	}

	// ========================================================================================
	// Getters and Setters
	// ========================================================================================
	@Test
	public void placesGpsLocation_gettersAndSetter() {
		// test
		PlacesGpsLocation location = new PlacesGpsLocation(22.22, 33.33);

		// verify
		location.setSpeed(20f);
		location.setAccuracy(30f);
		location.setAltitude(40.0);
		assertEquals(20f, location.getSpeed(), 0);
		assertEquals(30f, location.getAccuracy(), 0);
		assertEquals(40.0f, location.getAltitude(), 0);
	}

}
