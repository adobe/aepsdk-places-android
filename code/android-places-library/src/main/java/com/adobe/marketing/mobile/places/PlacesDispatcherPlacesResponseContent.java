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

import java.util.List;

class PlacesDispatcherPlacesResponseContent extends ModuleEventDispatcher<PlacesExtension> {

	/**
	 * Constructs a new instance of {@code PlacesDispatcherPlacesResponseContent}.
	 *
	 * @param hub    the event hub that this dispatcher will inter-operate with
	 * @param extension PlacesExtension module that owns this dispatcher.
	 */
	PlacesDispatcherPlacesResponseContent(final EventHub hub, final PlacesExtension extension) {
		super(hub, extension);
	}

	void dispatchNearbyPlaces(final List<PlacesPOI> poiList, final PlacesRequestError resultStatus,
							  final String pairId) {
		EventData responseEventData = new EventData();
		responseEventData.putTypedList(PlacesConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST, poiList,
									   new PlacesPOIVariantSerializer());
		responseEventData.putInteger(PlacesConstants.EventDataKeys.Places.RESULT_STATUS, resultStatus.getValue());
		Log.debug(PlacesConstants.LOG_TAG, "Dispatching nearby places event with %s POIs", poiList.size());
		Event responseEvent = new Event.Builder(PlacesConstants.EventName.RESPONSE_GETNEARBYPLACES, EventType.PLACES,
												EventSource
												.RESPONSE_CONTENT)
		.setData(responseEventData)
		.setPairID(pairId)
		.build();
		dispatch(responseEvent);
	}

	void dispatchRegionEvent(final PlacesRegion region) {
		if (region == null) {
			return;
		}

		EventData regionData;

		try {
			regionData = region.getRegionEventData();
		} catch (VariantException exp) {
			Log.warning(PlacesConstants.LOG_TAG,
						"Exception occurred while preparing eventData for region event. For more details refer to " +
						PlacesConstants.DocsLinks.PROCESS_REGION_EVENT);
			return;
		}

		Event regionEvent = new Event.Builder(PlacesConstants.EventName.RESPONSE_PROCESSREGIONEVENT, EventType.PLACES,
											  EventSource
											  .RESPONSE_CONTENT)
		.setData(regionData)
		.build();
		Log.debug(PlacesConstants.LOG_TAG, "Dispatching Places Region Event for %s with eventType %s", region.getName(),
				  region.getPlaceEventType());
		dispatch(regionEvent);
	}

	void dispatchUserWithinPOIs(final List<PlacesPOI> poiList, final String pairId) {
		EventData responseEventData = new EventData();
		responseEventData.putTypedList(PlacesConstants.EventDataKeys.Places.USER_WITHIN_POIS, poiList,
									   new PlacesPOIVariantSerializer());
		Log.debug(PlacesConstants.LOG_TAG, "Dispatching user within POIs event with %s POIs", poiList.size());
		Event responseEvent = new Event.Builder(PlacesConstants.EventName.RESPONSE_GETUSERWITHINPLACES, EventType.PLACES,
												EventSource
												.RESPONSE_CONTENT)
		.setData(responseEventData)
		.setPairID(pairId)
		.build();
		dispatch(responseEvent);
	}

	void dispatchLastKnownLocation(final double latitude, final double longitude, final String pairId) {
		EventData responseEventData = new EventData();
		responseEventData.putDouble(PlacesConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, latitude);
		responseEventData.putDouble(PlacesConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, longitude);
		Log.debug(PlacesConstants.LOG_TAG, "Dispatching last known location event with latitude: %s and longitude: %s",
				  latitude, longitude);
		Event responseEvent = new Event.Builder(PlacesConstants.EventName.RESPONSE_GETLASTKNOWNLOCATION, EventType.PLACES,
												EventSource
												.RESPONSE_CONTENT)
		.setData(responseEventData)
		.setPairID(pairId)
		.build();
		dispatch(responseEvent);
	}
}
