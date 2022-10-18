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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PlacesConfiguration {

	private List<PlacesLibrary> libraries;
	private String endpoint;
	private long membershipTtl;
	private boolean isValid;

	PlacesConfiguration(final EventData configData, final PlatformServices services) {
		this();

		if (services == null || services.getJsonUtilityService() == null) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, "Places Configuration : JSONService not available");
			return;
		}

		if (configData == null) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, "Places Configuration : Configuration eventData is null");
			return;
		}

		// initiate the libraries list
		libraries = new ArrayList<PlacesLibrary>();

		// read the libraries from the configuration
		final List<Variant> libraryList = configData.optVariantList(
											  PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES,
											  null);

		// bail out if there is no places libraries
		if (libraryList == null) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, "Places Configuration : No places libraries found in configuration");
			return;
		}

		for (Variant eachVariant : libraryList) {
			final Map<String, String> libraryMap = eachVariant.optStringMap(null);

			if (libraryMap != null && !libraryMap.isEmpty()) {
				String id = libraryMap.get(PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID);

				if (!StringUtils.isNullOrEmpty(id)) {
					// create new library and add them to the library list
					libraries.add(new PlacesLibrary(id));
				}
			}
		}

		// bail out if there is no valid places libraries after parsing the json
		if (libraries.isEmpty()) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, "Places Configuration : No valid libraries found in configuration");
			return;
		}

		endpoint = configData.optString(PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, "");

		if (StringUtils.isNullOrEmpty(endpoint)) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, "Places Configuration : No valid endpoint found in configuration");
			return;
		}

		membershipTtl = configData.optLong(PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL,
										   PlacesConstants.DEFAULT_MEMBERSHIP_TTL);
		isValid = true;
	}

	String getLibrariesQueryString() {
		StringBuilder builder = new StringBuilder();

		for (PlacesLibrary library : libraries) {
			builder.append("&library=");
			builder.append(library.getLibraryId());
		}

		return builder.toString();
	}

	String getEndpoint() {
		return endpoint;
	}

	boolean isValid() {
		return isValid;
	}

	long getMembershipTtl() {
		return membershipTtl;
	}

	// hiding the default constructor
	private PlacesConfiguration() { }


}
