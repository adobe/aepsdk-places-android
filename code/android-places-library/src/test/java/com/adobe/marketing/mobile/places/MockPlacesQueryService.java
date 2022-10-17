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

public class MockPlacesQueryService extends PlacesQueryService {
	JsonUtilityService jsonUtilityService;
	NetworkService networkService;


	public  MockPlacesQueryService(JsonUtilityService jsonUtilityService,
								   NetworkService networkService) throws MissingPlatformServicesException {
		super(jsonUtilityService, networkService);
		this.jsonUtilityService = jsonUtilityService;
		this.networkService = networkService;
	}

	boolean getNearByPlacesWasCalled;
	EventData getNearbyPlacesParamEventData;
	PlacesConfiguration getNearbyPlacesPlacesConfiguration;
	PlacesQueryResponse mockedResponse;
	@Override
	public PlacesQueryResponse getNearbyPlaces(EventData eventData, PlacesConfiguration placesConfig) {
		getNearByPlacesWasCalled = true;
		getNearbyPlacesParamEventData = eventData;
		getNearbyPlacesPlacesConfiguration = placesConfig;
		return mockedResponse;
	}
}
