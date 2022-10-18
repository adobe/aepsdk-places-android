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

import java.util.ArrayList;
import java.util.List;

public class MockPlacesState extends PlacesState {

	public MockPlacesState(PlatformServices platformServices) {
		super(platformServices);
	}

	boolean processNetworkResponseWasCalled = false;
	PlacesQueryResponse processNetworkResponseParamResponse;
	Event processNetworkResponseParamEvent;
	@Override
	public void processNetworkResponse(PlacesQueryResponse response, Event event) {
		processNetworkResponseWasCalled = true;
		processNetworkResponseParamResponse = response;
		processNetworkResponseParamEvent = event;
		return;
	}

	ArrayList<PlacesPOI> getUserWithInPOIsReturnValue;
	boolean getUserWithInPOIsWasCalled;
	@Override
	public List<PlacesPOI> getUserWithInPOIs() {
		getUserWithInPOIsWasCalled = true;
		return getUserWithInPOIsReturnValue;
	}

	PlacesGpsLocation loadLastKnownLocationReturnValue;
	Boolean loadLastKnownLocationWasCalled;
	@Override
	public PlacesGpsLocation loadLastKnownLocation() {
		loadLastKnownLocationWasCalled = true;
		return loadLastKnownLocationReturnValue;
	}

	boolean saveLastKnownLocationWasCalled;
	double saveLastKnownLocationParamLatitude;
	double saveLastKnownLocationParamLongitude;
	@Override
	public void saveLastKnownLocation(double latitude, double longitude) {
		saveLastKnownLocationWasCalled = true;
		saveLastKnownLocationParamLatitude = latitude;
		saveLastKnownLocationParamLongitude = longitude;
	}

	boolean saveLocationPermissionStatusWasCalled;
	String saveLocationPermissionStatusParamStatus;
	@Override
	public void setAuthorizationStatus(String status) {
		saveLocationPermissionStatusWasCalled = true;
		saveLocationPermissionStatusParamStatus = status;
		super.setAuthorizationStatus(status);
	}

	boolean processRegionEventWasCalled = false;
	Event processRegionEventParamEvent;
	PlacesRegion processRegionEventReturnValue;
	@Override
	public PlacesRegion processRegionEvent(Event event) {
		processRegionEventWasCalled = true;
		processRegionEventParamEvent = event;
		return processRegionEventReturnValue;
	}

	boolean getPlacesSharedStateWasCalled = false;
	EventData getPlacesSharedStateReturnValue;
	@Override
	public EventData getPlacesSharedState() {
		getPlacesSharedStateWasCalled = true;
		return getPlacesSharedStateReturnValue;
	}

	boolean clearDataOutCalled;
	@Override
	public void clearData() {
		clearDataOutCalled = true;
	}
}
