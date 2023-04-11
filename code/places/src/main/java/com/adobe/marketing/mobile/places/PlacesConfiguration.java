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

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PlacesConfiguration {

	private static final String CLASS_NAME = "PlacesConfiguration";

	private List<PlacesLibrary> libraries;
	private String endpoint;
	private long membershipTtl;
	private boolean isValid;
	private String experienceEventDataset;

	PlacesConfiguration(final Map<String, Object> configData) {
		this();

		if (configData == null) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "Constructor - Places Configuration : Configuration eventData is null");
			return;
		}

		// initiate the libraries list
		libraries = new ArrayList<>();

		// read the libraries from the configuration
		final List<Map> libraryList = DataReader.optTypedList(Map.class, configData, PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES, null);

		// bail out if there is no places libraries
		if (libraryList == null) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "Constructor - No places libraries found in configuration");
			return;
		}

		for (final Map eachLibrary : libraryList) {
			if (eachLibrary != null && !eachLibrary.isEmpty()) {
				final String libraryId = DataReader.optString(eachLibrary, PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID, "");
				if (!StringUtils.isNullOrEmpty(libraryId)) {
					// create new library and add them to the library list
					libraries.add(new PlacesLibrary(libraryId));
				} else {
					Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "Constructor - Invalid places library Id.");
				}
			}
		}

		// bail out if there is no valid places libraries after parsing the json
		if (libraries.isEmpty()) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "Constructor - Places Configuration : No valid libraries found in configuration");
			return;
		}

		endpoint = DataReader.optString(configData,PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT, "");

		if (StringUtils.isNullOrEmpty(endpoint)) {
			isValid = false;
			Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "Constructor - Places Configuration : No valid endpoint found in configuration");
			return;
		}

		membershipTtl = DataReader.optLong(configData, PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL,
				PlacesConstants.DEFAULT_MEMBERSHIP_TTL);
		isValid = true;

		experienceEventDataset = DataReader.optString(configData, PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_EXPERIENCE_EVENT_DATASET, "");
	}

	String getLibrariesQueryString() {
		final StringBuilder builder = new StringBuilder();

		for (final PlacesLibrary library : libraries) {
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

	String getExperienceEventDataset() {
		return experienceEventDataset;
	}

	// hiding the default constructor
	private PlacesConfiguration() { }
}
