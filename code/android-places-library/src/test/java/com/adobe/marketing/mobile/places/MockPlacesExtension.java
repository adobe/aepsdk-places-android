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

import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.ExtensionApi;

public class MockPlacesExtension extends PlacesExtension {

	MockPlacesExtension(final ExtensionApi extensionApi) {
		super(extensionApi);
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
