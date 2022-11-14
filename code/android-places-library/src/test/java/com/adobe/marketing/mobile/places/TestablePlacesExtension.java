///* **************************************************************************
// *
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2019 Adobe Inc.
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Inc. and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Inc. and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Inc..
// **************************************************************************/
//
//package com.adobe.marketing.mobile.places;
//
//
//public class TestablePlacesExtension extends PlacesExtension {
//	public TestablePlacesExtension(EventHub hub, PlatformServices services) {
//		super(hub, services);
//	}
//
//	@Override
//	void handleConfigurationEvent(Event event) {
//		super.handleConfigurationEvent(event);
//	}
//
//	@Override
//	void handleGetUserWithinPOIsEvent(Event event) {
//		super.handleGetUserWithinPOIsEvent(event);
//	}
//
//	PlacesQueryService mockPlacesQueryService;
//	@Override
//	PlacesQueryService getPlacesQueryService() {
//		return mockPlacesQueryService;
//	}
//
//	PlacesQueryService testableGetPlacesQueryService() {
//		return super.getPlacesQueryService();
//	}
//
//	ConcurrentLinkedQueue<Event> getEventQueue() {
//		return eventsQueue;
//	}
//
//}
