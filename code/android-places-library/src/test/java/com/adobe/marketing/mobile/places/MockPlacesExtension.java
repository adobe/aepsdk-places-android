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

public class MockPlacesExtension extends PlacesExtension {

	EventHub hub;

	MockPlacesExtension(final EventHub hub, final PlatformServices services) {
		super(hub, services);
		this.hub = hub;
	}

	boolean isQueueEventCalled;
	Event queueEventParamEvent;
	@Override
	void queueEvent(Event event) {
		super.queueEvent(event);
		isQueueEventCalled = true;
		queueEventParamEvent = event;
	}

	boolean isHandleConfigurationEventCalled;
	Event handleConfigurationEventParamEvent;
	@Override
	void handleConfigurationEvent(Event event) {
		isHandleConfigurationEventCalled = true;
		handleConfigurationEventParamEvent = event;
	}

	boolean ishandleSetLocationPermissionStatusEventCalled;
	Event ishandleSetLocationPermissionStatusEventEvent;
	@Override
	void handleSetAuthorizationStatusEvent(Event event) {
		ishandleSetLocationPermissionStatusEventCalled = true;
		ishandleSetLocationPermissionStatusEventEvent = event;
		super.handleSetAuthorizationStatusEvent(event);
	}

	boolean isHandleGetUserWithinPOIsEventCalled;
	Event handleGetUserWithinPOIsEventParamEvent;
	@Override
	void handleGetUserWithinPOIsEvent(Event event) {
		super.handleGetUserWithinPOIsEvent(event);
		isHandleGetUserWithinPOIsEventCalled = true;
		handleGetUserWithinPOIsEventParamEvent = event;
	}

	boolean handleGetLastKnownLocationEventCalled;
	Event handleGetLastKnownLocationParamEvent;
	@Override
	void handleGetLastKnownLocation(Event event) {
		super.handleGetLastKnownLocation(event);
		handleGetLastKnownLocationEventCalled = true;
		handleGetLastKnownLocationParamEvent = event;
	}

	boolean saveLastKnownLocationWasCalled;
	Event saveLastKnownLocationParamEvent;
	@Override
	void saveLastKnownLocation(Event event) {
		super.saveLastKnownLocation(event);
		saveLastKnownLocationParamEvent = event;
		saveLastKnownLocationWasCalled = true;
	}

	boolean resetWasCalled;
	@Override
	void reset() {
		super.reset();
		resetWasCalled = true;
	}
}
