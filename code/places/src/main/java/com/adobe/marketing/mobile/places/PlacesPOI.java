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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a Point of Interest (POI) in the Places extension.
 */
public class PlacesPOI {

	private String identifier;
	private String name;
	private double latitude;
	private double longitude;
	private int radius;
	private boolean userIsWithin;
	private String library;
	private int weight;
	private Map<String, String> metadata;

	/**
	 * Returns the identifier of the POI.
	 *
	 * @return the identifier of the POI
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the name of the POI.
	 *
	 * @return the name of the POI
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns whether the user is within the POI.
	 *
	 * @return true if the user is within the POI, false otherwise
	 */
	public boolean containsUser() {
		return userIsWithin;
	}

	/**
	 * Sets whether the user is within the POI.
	 *
	 * @param userIsWithin true if the user is within the POI, false otherwise
	 */
	public void setUserIsWithin(final boolean userIsWithin) {
		this.userIsWithin = userIsWithin;
	}

	/**
	 * Returns the latitude of the POI.
	 *
	 * @return the latitude of the POI
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Returns the longitude of the POI.
	 *
	 * @return the longitude of the POI
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Returns the radius of the POI.
	 *
	 * @return the radius of the POI
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Returns the metadata of the POI.
	 *
	 * @return the metadata of the POI
	 */
	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	/**
	 * Returns the weight of the POI.
	 *
	 * @return the weight of the POI
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Returns the library of the POI.
	 *
	 * @return the library of the POI
	 */
	public String getLibrary() {
		return library;
	}

	// copy constructor
	protected PlacesPOI(final PlacesPOI placesPOI) {
		if (placesPOI == null) {
			return;
		}

		this.identifier = placesPOI.identifier;
		this.name = placesPOI.name;
		this.latitude = placesPOI.latitude;
		this.longitude = placesPOI.longitude;
		this.radius = placesPOI.radius;
		this.userIsWithin = placesPOI.userIsWithin;
		this.weight = placesPOI.weight;
		this.library = placesPOI.library;
		this.metadata = placesPOI.metadata;
	}

	protected PlacesPOI(final String jsonString) throws JSONException {
		final JSONObject poiJson = new JSONObject(jsonString);

		if (poiJson == null) {
			throw new JSONException("Cannot convert json string into json object");
		}

		jsonObjectToPlacesPOI(poiJson);
	}

	protected PlacesPOI(final JSONObject poiJson) throws JSONException {
		jsonObjectToPlacesPOI(poiJson);
	}

	private void jsonObjectToPlacesPOI(final JSONObject poiJson) throws JSONException {
		this.identifier = poiJson.getString(PlacesConstants.POIKeys.IDENTIFIER);
		this.name = poiJson.getString(PlacesConstants.POIKeys.NAME);
		this.latitude = poiJson.getDouble(PlacesConstants.POIKeys.LATITUDE);
		this.longitude = poiJson.getDouble(PlacesConstants.POIKeys.LONGITUDE);
		this.radius = poiJson.getInt(PlacesConstants.POIKeys.RADIUS);
		this.userIsWithin = poiJson.getBoolean(PlacesConstants.POIKeys.USER_IS_WITHIN);
		this.weight = poiJson.getInt(PlacesConstants.POIKeys.WEIGHT);
		this.library = poiJson.optString(PlacesConstants.POIKeys.LIBRARY, "");
		final JSONObject metadataJSON = poiJson.optJSONObject(PlacesConstants.POIKeys.METADATA);

		if (metadataJSON != null) {
			this.metadata = PlacesUtil.convertPOIMetadataToStringMap(metadataJSON);
		}
	}

	protected PlacesPOI(
		final String identifier,
		final String name,
		final double latitude,
		final double longitude,
		final int radius,
		final String library,
		final int weight,
		final Map<String, String> metadata
	) {
		this.identifier = identifier;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		this.library = library;
		this.weight = weight;
		this.metadata = metadata;
	}

	protected PlacesPOI(
		final String identifier,
		final String name,
		final double latitude,
		final double longitude,
		final int radius,
		final String library,
		final int weight
	) {
		this(identifier, name, latitude, longitude, radius, library, weight, null);
	}

	protected void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	protected void setName(final String name) {
		this.name = name;
	}

	protected void setLatitude(final double latitude) {
		this.latitude = latitude;
	}

	protected void setLongitude(final double longitude) {
		this.longitude = longitude;
	}

	protected void setRadius(final int radius) {
		this.radius = radius;
	}

	protected void setMetadata(final Map<String, String> metadata) {
		this.metadata = metadata;
	}

	void setWeight(final int weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}

		if (this == o) {
			return true;
		}

		if (this.getClass() != o.getClass()) {
			return false;
		}

		PlacesPOI placesPOI = (PlacesPOI) o;

		if (!equalsWithOutMetaData(placesPOI)) {
			return false;
		}

		return metadata != null ? metadata.equals(placesPOI.metadata) : placesPOI.metadata == null;
	}

	boolean equalsWithOutMetaData(final Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PlacesPOI placesPOI = (PlacesPOI) o;

		if (Double.compare(placesPOI.latitude, latitude) != 0) {
			return false;
		}

		if (Double.compare(placesPOI.longitude, longitude) != 0) {
			return false;
		}

		if (Double.compare(placesPOI.radius, radius) != 0) {
			return false;
		}

		if (!identifier.equals(placesPOI.identifier)) {
			return false;
		}

		if (!name.equals(placesPOI.name)) {
			return false;
		}

		if (weight != placesPOI.weight) {
			return false;
		}

		if (!library.equals(placesPOI.library)) {
			return false;
		}

		return userIsWithin == placesPOI.userIsWithin;
	}

	/**
	 * Compares two POIs to determine which one has higher priority.
	 * <p>
	 * Calculation of priority based on:
	 * 1. Weight (lower number has higher priority)
	 * 2. Radius (smaller radius has higher priority)
	 * 3. Order (current object will have higher priority if weight and radius are same)
	 *
	 * @param otherPOI the PlacesDataObject to compare priority against this object's priority
	 * @return true if the calling object has higher priority than the parameter passed in
	 */
	public boolean comparePriority(final PlacesPOI otherPOI) {
		if (otherPOI != null) {
			if (otherPOI.weight < weight) {
				return false;
			} else if (otherPOI.weight == weight) {
				return otherPOI.radius >= radius;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		final int numBits = 32;
		int result;
		long temp;
		result = identifier.hashCode();
		result = prime * result + name.hashCode();
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ (temp >>> numBits));
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ (temp >>> numBits));
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> numBits));
		result = prime * result + (metadata != null ? metadata.hashCode() : 0);
		return result;
	}

	Map<String, Object> toMap() {
		final Map<String, Object> poiMap = new HashMap<>();
		poiMap.put(PlacesConstants.POIKeys.IDENTIFIER, this.identifier);
		poiMap.put(PlacesConstants.POIKeys.NAME, this.name);
		poiMap.put(PlacesConstants.POIKeys.LATITUDE, this.latitude);
		poiMap.put(PlacesConstants.POIKeys.LONGITUDE, this.longitude);
		poiMap.put(PlacesConstants.POIKeys.RADIUS, this.radius);
		poiMap.put(PlacesConstants.POIKeys.METADATA, this.metadata);
		poiMap.put(PlacesConstants.POIKeys.USER_IS_WITHIN, this.userIsWithin);
		poiMap.put(PlacesConstants.POIKeys.LIBRARY, this.library);
		poiMap.put(PlacesConstants.POIKeys.WEIGHT, this.weight);
		return poiMap;
	}

	String toJsonString() {
		final JSONObject jsonObject = new JSONObject(toMap());
		return jsonObject.toString();
	}
}
