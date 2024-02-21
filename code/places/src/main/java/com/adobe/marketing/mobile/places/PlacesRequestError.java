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

/**
 * Represents the status of places query service response.
 */
public enum PlacesRequestError {
	/**
	 * OK - when fetching the nearByPointsOfInterest is a success
	 */
	OK(0),

	/**
	 * CONNECTIVITY_ERROR - when the connection object is null (i.e) There is no network connectivity on the device
	 */
	CONNECTIVITY_ERROR(1),

	/**
	 * SERVER_RESPONSE_ERROR - occurs during the following scenarios
	 * <p>
	 * 1. When the connection is not 200OK
	 * 2. When the server responds with empty JSON response
	 * 3. When the server responds with Invalid JSON response
	 */
	SERVER_RESPONSE_ERROR(2),

	/**
	 * INVALID_LATLONG_ERROR - when an invalid latitude or longitude is passed to the getNearByPointsOfInterest API
	 */
	INVALID_LATLONG_ERROR(3),

	/**
	 * CONFIGURATION_ERROR - occurs when
	 * <p>
	 * 1.Places configuration is not available
	 * 2.The privacy on the SDK is opted-out
	 */
	CONFIGURATION_ERROR(4),

	/**
	 * QUERY_SERVICE_UNAVAILABLE - occurs when the network/Json service from the MobileCore is not available
	 */
	QUERY_SERVICE_UNAVAILABLE(5),

	/**
	 * PRIVACY_OPTED_OUT - occurs when the privacy on the SDK is opted-out
	 */
	PRIVACY_OPTED_OUT(6),

	/**
	 * UNKNOWN_ERROR - for any other unknown error
	 */
	UNKNOWN_ERROR(6);

	private final int value;

	PlacesRequestError(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Returns a {@link PlacesRequestError} object based on the provided {@code int} value.
	 * <p>
	 * If the value provided is not valid, {@link #UNKNOWN_ERROR} will be returned.
	 *
	 * @param value {@code int} to be converted to a {@code PlacesRequestError} object
	 * @return {@code PlacesRequestError} object equivalent to the provided int
	 */
	public static PlacesRequestError fromInt(final int value) {
		for (PlacesRequestError b : PlacesRequestError.values()) {
			if (b.value == (value)) {
				return b;
			}
		}

		return UNKNOWN_ERROR;
	}
}
