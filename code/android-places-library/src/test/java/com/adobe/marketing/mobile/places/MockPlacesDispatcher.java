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

public class MockPlacesDispatcher extends PlacesDispatcherPlacesResponseContent {
	public MockPlacesDispatcher(EventHub hub, PlacesExtension extension) {
		super(hub, extension);
	}

	boolean dispatchNearbyPlacesWasCalled = false;
	int dispatchNearbyPlacesWasCalledCount = 0;
	List<List<PlacesPOI>> dispatchNearbyPlacesParamPoiList = new ArrayList<List<PlacesPOI>>();
	List<String> dispatchNearbyPlacesParamPairId = new ArrayList<String>();
	List<PlacesRequestError> dispatchNearbyPlacesParamResponseStatus = new ArrayList<PlacesRequestError>();
	@Override
	public void dispatchNearbyPlaces(List<PlacesPOI> poiList, PlacesRequestError resultStatus, String pairId) {
		dispatchNearbyPlacesWasCalled = true;
		dispatchNearbyPlacesWasCalledCount++;
		dispatchNearbyPlacesParamPoiList.add(poiList);
		dispatchNearbyPlacesParamPairId.add(pairId);
		dispatchNearbyPlacesParamResponseStatus.add(resultStatus);;
		super.dispatchNearbyPlaces(poiList, resultStatus, pairId);
	}

	boolean dispatchRegionEventWasCalled = false;
	PlacesRegion dispatchRegionEventParamRegion;
	@Override
	public void dispatchRegionEvent(PlacesRegion region) {
		dispatchRegionEventParamRegion = region;
		dispatchRegionEventWasCalled = true;
	}

	boolean dispatchUserWithinPOIsCalled = false;
	int dispatchUserWithinPOIsCallCount = 0;
	List<List<PlacesPOI>> dispatchUserWithinPOIsEventParamPoiList = new ArrayList<>();
	List<String> dispatchUserWithinPOIsEventParamEventPaidId = new ArrayList<>();
	@Override
	public void dispatchUserWithinPOIs(List<PlacesPOI> poiList, String pairId) {
		super.dispatchUserWithinPOIs(poiList, pairId);
		dispatchUserWithinPOIsCalled = true;
		dispatchUserWithinPOIsCallCount++;
		dispatchUserWithinPOIsEventParamPoiList.add(poiList);
		dispatchUserWithinPOIsEventParamEventPaidId.add(pairId);
	}

	boolean dispatchLastKnownLocationWasCalled;
	int dispatchLastKnownLocationWasCallCount;
	List<Double> dispatchLastKnownLocationParamLatitude = new ArrayList<>();
	List<Double> dispatchLastKnownLocationParamLongitude = new ArrayList<>();
	List<String> dispatchLastKnownLocationParamPairID = new ArrayList<>();
	@Override
	public void dispatchLastKnownLocation(double latitude, double longitude, String pairId) {
		super.dispatchLastKnownLocation(latitude, longitude, pairId);
		dispatchLastKnownLocationWasCalled = true;
		dispatchLastKnownLocationWasCallCount++;
		dispatchLastKnownLocationParamLatitude.add(latitude);
		dispatchLastKnownLocationParamLongitude.add(longitude);
		dispatchLastKnownLocationParamPairID.add(pairId);
	}
}
