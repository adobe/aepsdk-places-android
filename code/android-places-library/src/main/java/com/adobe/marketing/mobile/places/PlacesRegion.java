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

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

final class PlacesRegion extends PlacesPOI {

	private final String              placeEventType;
	private final long                timestamp;
	private final PlacesPOI 			poi;

	static final String PLACE_EVENT_NONE  = "none";
	static final String PLACE_EVENT_ENTRY = "entry";
	static final String PLACE_EVENT_EXIT  = "exit";

	long getTimestamp() {
		return timestamp;
	}

	Map<String,Object> getRegionEventData() {
		Map<String,Object> data = new HashMap<>();
		data.put(PlacesConstants.EventDataKeys.Places.TRIGGERING_REGION, poi.toMap());
		data.put(PlacesConstants.EventDataKeys.Places.REGION_EVENT_TYPE, getPlaceEventType());
		data.put(PlacesConstants.EventDataKeys.Places.REGION_TIMESTAMP, getTimestamp());
		return data;
	}

	PlacesRegion(@NonNull final PlacesPOI placesPOI,
				 @NonNull final String placeEventType,
				 final long timestamp) {
		super(placesPOI);
		this.poi = placesPOI;
		this.placeEventType = placeEventType;
		this.timestamp = timestamp;
	}


	String getPlaceEventType() {
		return placeEventType;
	}
}


