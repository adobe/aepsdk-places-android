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
 * VariantSerializer implementation for PlacesPOI Object.
 */
final class PlacesPOIVariantSerializer implements VariantSerializer<PlacesPOI> {

	@Override
	public Variant serialize(final PlacesPOI placesPOI) throws VariantException {
		if (placesPOI == null) {
			return Variant.fromNull();
		}

		HashMap<String, Variant> poiMap = new HashMap<String, Variant>();
		poiMap.put(PlacesConstants.POIKeys.IDENTIFIER, Variant.fromString(placesPOI.getIdentifier()));
		poiMap.put(PlacesConstants.POIKeys.NAME, Variant.fromString(placesPOI.getName()));
		poiMap.put(PlacesConstants.POIKeys.LATITUDE, Variant.fromDouble(placesPOI.getLatitude()));
		poiMap.put(PlacesConstants.POIKeys.LONGITUDE, Variant.fromDouble(placesPOI.getLongitude()));
		poiMap.put(PlacesConstants.POIKeys.RADIUS, Variant.fromDouble(placesPOI.getRadius()));
		poiMap.put(PlacesConstants.POIKeys.METADATA, Variant.fromStringMap(placesPOI.getMetadata()));
		poiMap.put(PlacesConstants.POIKeys.USER_IS_WITHIN, Variant.fromBoolean(placesPOI.containsUser()));
		poiMap.put(PlacesConstants.POIKeys.LIBRARY, Variant.fromString(placesPOI.getLibrary()));
		poiMap.put(PlacesConstants.POIKeys.WEIGHT, Variant.fromInteger(placesPOI.getWeight()));
		return Variant.fromVariantMap(poiMap);
	}

	@Override
	public PlacesPOI deserialize(final Variant serialized) throws VariantException {
		if (serialized == null) {
			throw new IllegalArgumentException();
		}

		if (serialized.getKind() == VariantKind.NULL) {
			return null;
		}

		final Map<String, Variant> variantMap = serialized.getVariantMap();

		final String identifier = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.IDENTIFIER).optString(null);
		final String name = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.NAME).optString(null);
		final double latitude = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.LATITUDE).optDouble(0.00);
		final double longitude = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.LONGITUDE).optDouble(0.00);
		final int radius = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.RADIUS).optInteger(0);
		final Map<String, String> metadata = Variant.optVariantFromMap(variantMap,
											 PlacesConstants.POIKeys.METADATA).optStringMap(null);
		final boolean containsUser = Variant.optVariantFromMap(variantMap,
									 PlacesConstants.POIKeys.USER_IS_WITHIN).optBoolean(false);
		final String library = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.LIBRARY).optString(null);
		final int weight = Variant.optVariantFromMap(variantMap, PlacesConstants.POIKeys.WEIGHT).optInteger(0);

		PlacesPOI poi = new PlacesPOI(identifier, name, latitude, longitude, radius, library, weight, metadata);
		poi.setUserIsWithin(containsUser);
		return poi;
	}
}