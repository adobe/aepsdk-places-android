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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the device's authorization status for access to use location on the device.
 */
public enum PlacesAuthorizationStatus {

	/**
	 * DENIED - The app can never use your location, even when you’re using the app.
	 */
	DENIED("denied"),

	/**
	 * ALWAYS - The app can use your location at any time.
	 */
	ALWAYS("always"),

	/**
	 * UNKNOWN - The location authorization status is unknown. This is the default authorization status.
	 */
	UNKNOWN("unknown"),

	/**
	 * RESTRICTED - The location authorization status is restricted.
	 */
	RESTRICTED("restricted"),

	/**
	 * WHEN_IN_USE - The app can use your location only when you're using that app.
	 */
	WHEN_IN_USE("wheninuse");

	private final String authStatus;
	static String DEFAULT_VALUE = PlacesAuthorizationStatus.UNKNOWN.stringValue();

	PlacesAuthorizationStatus(final String authStatus) {
		this.authStatus = authStatus;
	}

	/**
	 * Returns the string value of the PlacesAuthorizationStatus.
	 *
	 * @return {@link String} representation of {@link PlacesAuthorizationStatus}
	 */
	public String stringValue() {
		return authStatus;
	}

	/**
	 * Returns {@link PlacesAuthorizationStatus} value of the provided string.
	 * <p>
	 * Returns null if the provided string is not a valid {@link PlacesAuthorizationStatus} enum value
	 *
	 * @return {@link PlacesAuthorizationStatus} value for provided status string
	 */
	static PlacesAuthorizationStatus get(final String statusString) {
		return lookup.get(statusString);
	}


	/**
	 * Checks the validity of the status string.
	 *
	 * @return {@code boolean} indicating the validity of the provided status string.
	 */
	static boolean isValidStatus(final String statusString) {
		return lookup.containsKey(statusString);
	}


	// generate look up table on load time
	private static final Map<String, PlacesAuthorizationStatus> lookup = new HashMap<>();
	static
	{
		for (PlacesAuthorizationStatus env : PlacesAuthorizationStatus.values()) {
			lookup.put(env.stringValue(), env);
		}
	}
}
