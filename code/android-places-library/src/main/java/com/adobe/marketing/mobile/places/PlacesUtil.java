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

class PlacesUtil {
	private static final double MAX_LAT		= 90d;
	private static final double MIN_LAT		= -90d;
	private static final double MAX_LON		= 180d;
	private static final double MIN_LON		= -180d;

	/**
	 * Verifies if the provided latitude is valid.
	 *
	 * @param latitude the latitude
	 * @return true if latitude is in the range [-90,90]
	 */
	static boolean isValidLat(final double latitude) {
		return latitude >= MIN_LAT && latitude <= MAX_LAT;
	}


	/**
	 * Verifies if the provided longitude is valid.
	 *
	 * @param longitude the longitude
	 * @return true if longitude is in the range [-180,180]
	 */
	static boolean isValidLon(final double longitude) {
		return longitude >= MIN_LON && longitude <= MAX_LON;
	}
}
