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

final class PlacesRegion extends PlacesPOI {

	private String              placeEventType;
	private long                timestamp;
	private PlacesPOI 			poi;

	public static final String PLACE_EVENT_NONE  = "none";
	public static final String PLACE_EVENT_ENTRY = "entry";
	public static final String PLACE_EVENT_EXIT  = "exit";

	public long getTimestamp() {
		return timestamp;
	}

	public EventData getRegionEventData() throws VariantException {
		EventData data = new EventData();
		data.putTypedObject(PlacesConstants.EventDataKeys.Places.TRIGGERING_REGION, poi, new PlacesPOIVariantSerializer());
		data.putString(PlacesConstants.EventDataKeys.Places.REGION_EVENT_TYPE, getPlaceEventType());
		data.putLong(PlacesConstants.EventDataKeys.Places.REGION_TIMESTAMP, getTimestamp());
		return data;
	}

	protected PlacesRegion(final PlacesPOI placesPOI,
						   final String placeEventType,
						   final long timestamp) {
		super(placesPOI);
		this.poi = placesPOI;
		this.placeEventType = placeEventType;
		this.timestamp = timestamp;
	}


	protected String getPlaceEventType() {
		return placeEventType;
	}

}


