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
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PlacesDispatcher {

    private static final String CLASS_NAME = "PlacesDispatcher";
    private final ExtensionApi extensionApi;

    PlacesDispatcher(final ExtensionApi extensionApi) {
        this.extensionApi = extensionApi;
    }

    void dispatchNearbyPlaces(
            final List<PlacesPOI> poiList,
            final PlacesRequestError resultStatus,
            final Event event) {
        final Map<String, Object> responseEventData = new HashMap<>();
        responseEventData.put(
                PlacesConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST,
                PlacesUtil.convertPOIListToMap(poiList));
        responseEventData.put(
                PlacesConstants.EventDataKeys.Places.RESULT_STATUS, resultStatus.getValue());
        if (event != null) {
            Log.debug(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "dispatchNearbyPlaces - Dispatching nearby places response event for"
                            + " `getNearbyPointsOfInterest` API callback with %d POIs",
                    poiList.size());
            final Event responseEvent =
                    new Event.Builder(
                                    PlacesConstants.EventName.RESPONSE_GETNEARBYPLACES,
                                    EventType.PLACES,
                                    EventSource.RESPONSE_CONTENT)
                            .setEventData(responseEventData)
                            .inResponseToEvent(event)
                            .build();
            extensionApi.dispatch(responseEvent);
        } else {
            Log.debug(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "dispatchNearbyPlaces - Dispatching nearby places response event for all other"
                            + " listeners with %d POIs",
                    poiList.size());
            final Event responseEvent =
                    new Event.Builder(
                                    PlacesConstants.EventName.RESPONSE_GETNEARBYPLACES,
                                    EventType.PLACES,
                                    EventSource.RESPONSE_CONTENT)
                            .setEventData(responseEventData)
                            .build();
            extensionApi.dispatch(responseEvent);
        }
    }

    void dispatchRegionEvent(final PlacesRegion region) {
        if (region == null) {
            return;
        }
        final Map<String, Object> regionData = region.getRegionEventData();
        final Event regionEvent =
                new Event.Builder(
                                PlacesConstants.EventName.RESPONSE_PROCESSREGIONEVENT,
                                EventType.PLACES,
                                EventSource.RESPONSE_CONTENT)
                        .setEventData(regionData)
                        .build();
        Log.debug(
                PlacesConstants.LOG_TAG,
                CLASS_NAME,
                "dispatchRegionEvent - Dispatching Places Region Event for %s with eventType %s",
                region.getName(),
                region.getPlaceEventType());
        extensionApi.dispatch(regionEvent);
    }

    void dispatchUserWithinPOIs(final List<PlacesPOI> poiList, final Event event) {
        final Map<String, Object> responseEventData = new HashMap<>();
        responseEventData.put(
                PlacesConstants.EventDataKeys.Places.USER_WITHIN_POIS,
                PlacesUtil.convertPOIListToMap(poiList));
        if (event != null) {
            Log.debug(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "dispatchUserWithinPOIs - Dispatching user within POIs event for"
                            + " `getCurrentPointsOfInterest` API callback with %d POIs",
                    poiList.size());
            final Event responseEvent =
                    new Event.Builder(
                                    PlacesConstants.EventName.RESPONSE_GETUSERWITHINPLACES,
                                    EventType.PLACES,
                                    EventSource.RESPONSE_CONTENT)
                            .setEventData(responseEventData)
                            .inResponseToEvent(event)
                            .build();
            extensionApi.dispatch(responseEvent);
        } else {
            Log.debug(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "dispatchUserWithinPOIs - Dispatching user within POIs event for other"
                            + " listeners with %d POIs",
                    poiList.size());
            final Event responseEvent =
                    new Event.Builder(
                                    PlacesConstants.EventName.RESPONSE_GETUSERWITHINPLACES,
                                    EventType.PLACES,
                                    EventSource.RESPONSE_CONTENT)
                            .setEventData(responseEventData)
                            .build();
            extensionApi.dispatch(responseEvent);
        }
    }

    void dispatchLastKnownLocation(
            final double latitude, final double longitude, final Event event) {
        final Map<String, Object> responseEventData = new HashMap<>();
        responseEventData.put(PlacesConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE, latitude);
        responseEventData.put(PlacesConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE, longitude);
        if (event != null) {
            Log.debug(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "dispatchLastKnownLocation - Dispatching last known location event for"
                        + " `getLastKnownLocation` API callback with latitude: %s and longitude:"
                        + " %s",
                    latitude,
                    longitude);
            final Event responseEvent =
                    new Event.Builder(
                                    PlacesConstants.EventName.RESPONSE_GETLASTKNOWNLOCATION,
                                    EventType.PLACES,
                                    EventSource.RESPONSE_CONTENT)
                            .setEventData(responseEventData)
                            .inResponseToEvent(event)
                            .build();
            extensionApi.dispatch(responseEvent);
        } else {
            Log.debug(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "dispatchLastKnownLocation - Dispatching last known location event for other"
                            + " listeners with latitude: %s and longitude: %s",
                    latitude,
                    longitude);
            final Event responseEvent =
                    new Event.Builder(
                                    PlacesConstants.EventName.RESPONSE_GETLASTKNOWNLOCATION,
                                    EventType.PLACES,
                                    EventSource.RESPONSE_CONTENT)
                            .setEventData(responseEventData)
                            .build();
            extensionApi.dispatch(responseEvent);
        }
    }

    /**
     * Sends an experience event to the edge server for the specified geofence entry/exit location
     * event.
     *
     * @param regionEvent The region event representing geofence entry/exit.
     */
    void dispatchExperienceEventToEdge(@NonNull final PlacesRegion regionEvent) {
        final String experienceEventType = regionEvent.getExperienceEventType();
        if (StringUtils.isNullOrEmpty(experienceEventType)) {
            Log.warning(
                    PlacesConstants.LOG_TAG,
                    CLASS_NAME,
                    "Invalid region type : %s, Ignoring to send places experience edge event.",
                    regionEvent.getPlaceEventType());
            return;
        }

        final PlacesPOI poi = regionEvent.getPoi();

        final Map<String, Object> poiInteraction =
                new HashMap<String, Object>() {
                    {
                        put(PlacesConstants.XDM.Key.POI_DETAIL, createXDMPOIDetail(poi));
                    }
                };

        final Map<String, Object> xdmMap =
                new HashMap<String, Object>() {
                    {
                        put(PlacesConstants.XDM.Key.EVENT_TYPE, experienceEventType);
                        put(
                                PlacesConstants.XDM.Key.PLACE_CONTEXT,
                                new HashMap<String, Object>() {
                                    {
                                        put(
                                                PlacesConstants.XDM.Key.POI_INTERACTION,
                                                poiInteraction);
                                    }
                                });
                    }
                };

        final Map<String, Object> xdmEventData =
                new HashMap<String, Object>() {
                    {
                        put(PlacesConstants.XDM.Key.XDM, xdmMap);
                    }
                };

        final String[] mask = {
            PlacesConstants.EventMask.EVENT_TYPE, PlacesConstants.EventMask.POI_ID
        };
        final Event experienceEvent =
                new Event.Builder(
                                PlacesConstants.EventName.LOCATION_TRACKING,
                                EventType.EDGE,
                                EventSource.REQUEST_CONTENT,
                                mask)
                        .setEventData(xdmEventData)
                        .build();

        extensionApi.dispatch(experienceEvent);
    }

    /**
     * Creates an XDM (Experience Data Model) representation of a Point of Interest (POI) detail
     * based on the provided POI object.
     *
     * @param poi The {@link PlacesPOI} object for which the XDM POI detail needs to be created.
     */
    private Map<String, Object> createXDMPOIDetail(final PlacesPOI poi) {
        return new HashMap<String, Object>() {
            {
                put(PlacesConstants.XDM.Key.POI_ID, poi.getIdentifier());
                put(PlacesConstants.XDM.Key.NAME, poi.getName());
                put(PlacesConstants.XDM.Key.METADATA, createPOIMetadata(poi));
            }
        };
    }

    /**
     * Creates metadata for a Point of Interest (POI) based on the provided POI object.
     *
     * @param poi The {@link PlacesPOI} object for which the metadata needs to be created.
     */
    private Map<String, Object> createPOIMetadata(final PlacesPOI poi) {
        final List<Map<String, Object>> metadataList = new ArrayList<>();
        for (final Map.Entry<String, String> entry : poi.getMetadata().entrySet()) {
            final Map<String, Object> metadataMap =
                    new HashMap<String, Object>() {
                        {
                            put(PlacesConstants.XDM.Key.KEY, entry.getKey());
                            put(PlacesConstants.XDM.Key.VALUE, entry.getValue());
                        }
                    };
            metadataList.add(metadataMap);
        }

        return new HashMap<String, Object>() {
            {
                put(PlacesConstants.XDM.Key.LIST, metadataList);
            }
        };
    }
}
