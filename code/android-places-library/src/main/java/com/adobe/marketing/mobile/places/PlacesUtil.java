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

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlacesUtil {
    private static final double MAX_LAT = 90d;
    private static final double MIN_LAT = -90d;
    private static final double MAX_LON = 180d;
    private static final double MIN_LON = -180d;

    private static final String CLASS_NAME = "PlacesUtil";

    /**
     * Verifies if the provided latitude is valid.
     *
     * @param latitude the latitude
     * @return true if latitude is in the range [-90,90]
     */
    public static boolean isValidLat(final double latitude) {
        return latitude >= MIN_LAT && latitude <= MAX_LAT;
    }

    /**
     * Verifies if the provided longitude is valid.
     *
     * @param longitude the longitude
     * @return true if longitude is in the range [-180,180]
     */
    public static boolean isValidLon(final double longitude) {
        return longitude >= MIN_LON && longitude <= MAX_LON;
    }

    /**
     * Converts the list of {@link PlacesPOI} objects to list of {@link Map} representing {@code PlacesPOI}.
     *
     * @param poiList {@link List} of {@code PlacesPOI}
     * @return {@code List} of Map representing {@code PlacesPOI} object
     */
    static List<Map<String, Object>> convertPOIListToMap(final List<PlacesPOI> poiList) {
        List<Map<String, Object>> poiMapList = new ArrayList<>();
        for (final PlacesPOI eachPOI : poiList) {
            poiMapList.add(eachPOI.toMap());
        }
        return poiMapList;
    }

    /**
     * Converts the list oflist of {@link Map} representing {@code PlacesPOI} into {@link PlacesPOI} objects.
     *
     * @param poiMap {@link List} of Map representing {@code PlacesPOI} object
     * @return {@code List} of {@code PlacesPOI}
     */
    public static List<PlacesPOI> convertMapToPOIList(final List<Map> poiMap) {
        List<PlacesPOI> poiMapList = new ArrayList<>();
        for (final Map<String, Object> eachMap : poiMap) {
            PlacesPOI poi = new PlacesPOI(
                    DataReader.optString(eachMap, PlacesConstants.POIKeys.IDENTIFIER, null),
                    DataReader.optString(eachMap, PlacesConstants.POIKeys.NAME, null),
                    DataReader.optDouble(eachMap, PlacesConstants.POIKeys.LATITUDE, 0.00),
                    DataReader.optDouble(eachMap, PlacesConstants.POIKeys.LONGITUDE, 0.00),
                    DataReader.optInt(eachMap, PlacesConstants.POIKeys.RADIUS, 0),
                    DataReader.optString(eachMap, PlacesConstants.POIKeys.LIBRARY, null),
                    DataReader.optInt(eachMap, PlacesConstants.POIKeys.WEIGHT, 0),
                    DataReader.optStringMap(eachMap, PlacesConstants.POIKeys.METADATA, null));
            poi.setUserIsWithin(DataReader.optBoolean(eachMap, PlacesConstants.POIKeys.USER_IS_WITHIN, false));
            poiMapList.add(poi);
        }
        return poiMapList;
    }

    /**
     * Converts provided metadata {@link JSONObject} into {@link Map}<String,String>
     *
     * @param jsonObject {@code JSONObject} from server response representing the POI's metadata
     * @return {@code Map} containing cleaned POIMetadata
     */
    static Map<String, String> convertPOIMetadataToStringMap(@NonNull final JSONObject jsonObject) {
        final Map<String, String> map = new HashMap<>();
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            try {
                final Object value = jsonObject.get(key);
                // Metadata should only contain values that are primitive datatype, hence ignoring Map or Array values
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, String.format("Ignoring POI metadata with key: %s which contains invalid datatype.", key));
                    continue;
                }
                map.put(key, value.toString());
            } catch (final Exception e) {
                Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "The value of [%s] is not supported: %s", key, e);
            }
        }
        return map;
    }
}
