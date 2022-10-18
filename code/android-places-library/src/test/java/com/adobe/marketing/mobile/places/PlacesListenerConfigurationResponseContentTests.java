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


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlacesListenerConfigurationResponseContentTests extends BaseTest {
	private PlacesListenerConfigurationResponseContent listener;
	private MockPlacesExtension placesExtension;

	@Before
	public void testSetup() throws Exception {
		super.beforeEach();
		placesExtension = new MockPlacesExtension(eventHub, platformServices);
		listener = new PlacesListenerConfigurationResponseContent(placesExtension, EventType.CONFIGURATION,
				EventSource.RESPONSE_CONTENT);
	}

	@Test
	public void testListener_NullEvent() throws Exception {
		// test
		listener.hear(null);
		waitForExecutor(placesExtension.getExecutor());

		// verify
		assertFalse(placesExtension.isHandleConfigurationEventCalled);
	}

	@Test
	public void testListener_Happy() throws Exception {
		// setup
		EventData data = new EventData();
		data.putString("testConfigKey", "testValue");

		Event testEvent = new Event.Builder("Configuration response event", EventType.CONFIGURATION,
											EventSource.RESPONSE_CONTENT).setData(data).build();

		// test
		listener.hear(testEvent);
		waitForExecutor(placesExtension.getExecutor());

		// verify
		assertTrue(placesExtension.isHandleConfigurationEventCalled);
	}

}
