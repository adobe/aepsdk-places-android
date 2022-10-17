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

import com.adobe.marketing.mobile.JsonUtilityService.JSONArray;
import com.adobe.marketing.mobile.JsonUtilityService.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PlacesQueryService {

	private static final int POI_DETAIL_MIN_ARRAY_LENGTH = 7;
	private static final int DEFAULT_POI_RADIUS = 100;
	private static final int DEFAULT_POI_WEIGHT = 1000;

	// array index constants
	private static final int POI_INDEX_IDENTIFIER = 0;
	private static final int POI_INDEX_NAME = 1;
	private static final int POI_INDEX_LATITUDE = 2;
	private static final int POI_INDEX_LONGITUDE = 3;
	private static final int POI_INDEX_RADIUS = 4;
	private static final int POI_INDEX_LIBRARY = 5;
	private static final int POI_INDEX_WEIGHT = 6;

	private JsonUtilityService jsonUtilityService;
	private NetworkService networkService;


	PlacesQueryService(final JsonUtilityService jsonUtilityService,
					   final NetworkService networkService) throws MissingPlatformServicesException {
		if (jsonUtilityService == null) {
			throw new MissingPlatformServicesException("JsonUtilityService not found!");
		}

		if (networkService == null) {
			throw new MissingPlatformServicesException("LocalStorageService not found!");
		}

		this.jsonUtilityService = jsonUtilityService;
		this.networkService = networkService;
	}


	/**
	 *
	 * TODO: Doc Me
	 *
	 * @param eventData the {@link EventData} related to the get nearby places event
	 * @param placesConfig an instance of valid {@link PlacesConfiguration}
	 * @return A {@link PlacesQueryResponse} instance containing the places data
	 */
	// Pass non null event data.
	PlacesQueryResponse getNearbyPlaces(final EventData eventData, final PlacesConfiguration placesConfig) {
		PlacesQueryResponse placesResponse = new PlacesQueryResponse();

		String queryURL = getQueryURL(eventData, placesConfig);

		if (queryURL == null) {
			placesResponse.fetchFailed("Ignoring the get nearby places event, unable to form query URL",
									   PlacesRequestError.INVALID_LATLONG_ERROR);
			return placesResponse;
		}

		// add the library query parameter
		queryURL = queryURL + placesConfig.getLibrariesQueryString();

		Log.debug(PlacesConstants.LOG_TAG, "Getting nearby places:  %s", queryURL);
		NetworkService.HttpConnection connection = networkService.connectUrl(queryURL,  NetworkService.HttpCommand.GET, null,
				null, PlacesConstants.DEFAULT_NETWORK_TIMEOUT, PlacesConstants.DEFAULT_NETWORK_TIMEOUT);

		if (connection == null) {
			placesResponse.fetchFailed("Unable to get nearby places, connection is null", PlacesRequestError.CONNECTIVITY_ERROR);
			return placesResponse;
		}

		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			connection.close();
			String message = String.format("Unable to get nearby places, connection failed with status %s, message %s",
										   connection.getResponseCode(), connection.getResponseMessage());
			placesResponse.fetchFailed(message, PlacesRequestError.CONNECTIVITY_ERROR);
			return placesResponse;
		}

		try {
			String serverResponse = NetworkConnectionUtil.readFromInputStream(connection.getInputStream());

			if (StringUtils.isNullOrEmpty(serverResponse)) {
				placesResponse.fetchFailed("Unable to get nearby places, server response is empty",
										   PlacesRequestError.SERVER_RESPONSE_ERROR);
				return placesResponse;
			}

			Log.debug(PlacesConstants.LOG_TAG, "Got Response : %s", serverResponse);
			JSONObject responseJson = jsonUtilityService.createJSONObject(serverResponse);

			if (responseJson == null) {
				placesResponse.fetchFailed("Unable to get nearby places, invalid json from server response",
										   PlacesRequestError.SERVER_RESPONSE_ERROR);
				return placesResponse;
			}

			JSONObject placesJson = responseJson.getJSONObject(PlacesConstants.QueryResponseJsonKeys.PLACES);
			placesResponse.nearByPOIs = getNearbyPOIs(placesJson);
			placesResponse.containsUserPOIs = getContainsUserPOIs(placesJson);
			placesResponse.isSuccess = true;
			placesResponse.resultStatus = PlacesRequestError.OK;
			return placesResponse;


		} catch (Exception exception) {
			String message = String.format("Unable to get nearby places, Failed with exception: %s", exception);
			placesResponse.fetchFailed(message, PlacesRequestError.SERVER_RESPONSE_ERROR);
			return placesResponse;
		}

		finally {
			connection.close();
		}
	}

	/**
	 * TODO: Doc Me
	 *
	 * @param eventData the {@link EventData} related to the get nearby places event
	 * @param placesConfig an instance of valid {@link PlacesConfiguration}
	 * @return a url {@link String} to make the places query
	 */
	private String getQueryURL(final EventData eventData, final PlacesConfiguration placesConfig) {
		// Grab data from eventData
		double latitude = eventData.optDouble(PlacesConstants.EventDataKeys.Places.LATITUDE, PlacesConstants.INVALID_LAT_LON);
		double longitude = eventData.optDouble(PlacesConstants.EventDataKeys.Places.LONGITUDE, PlacesConstants.INVALID_LAT_LON);
		int count = eventData.optInteger(PlacesConstants.EventDataKeys.Places.PLACES_COUNT,
										 PlacesConstants.DEFAULT_NEARBYPOI_COUNT);

		if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
			Log.debug(PlacesConstants.LOG_TAG, "Unable to get nearby places, invalid latitude/longitude");
			return null;
		}


		// library=x&library=y
		// https://iu2jpewkm4.execute-api.us-east-1.amazonaws.com/dev/placesedgequery?latitude=37.338735&longitude=-121.904516&limit=15&library=738
		return new URLBuilder().enableSSL(true)
			   .setServer(placesConfig.getEndpoint())
			   .addPath(PlacesConstants.ServerKeys.PLACES_EDGE)
			   .addQueryParameter("latitude", Double.toString(latitude))
			   .addQueryParameter("longitude", Double.toString(longitude))
			   .addQueryParameter("limit", Integer.toString(count))
			   .build();
	}

	//	/**
	//	 * TODO: Doc Me
	//	 *
	//	 */
	private List<PlacesPOI> getContainsUserPOIs(final JsonUtilityService.JSONObject placesJson) throws JsonException {
		List<PlacesPOI> containsUserPOIList = new ArrayList<PlacesPOI>();

		JSONArray containsUserPOIArray = placesJson.optJSONArray(PlacesConstants.QueryResponseJsonKeys.POI_MEMBERS);

		if (containsUserPOIArray == null || containsUserPOIArray.length() == 0) {
			return containsUserPOIList;
		}

		for (int i = 0; i < containsUserPOIArray.length(); i++) {
			JSONObject poiJson = containsUserPOIArray.getJSONObject(i);
			PlacesPOI poi = createPlacesPOIFromJson(poiJson);

			if (poi != null) {
				poi.setUserIsWithin(true);
				containsUserPOIList.add(poi);
			}
		}

		return containsUserPOIList;
	}

	//	/**
	//	 * TODO: Doc Me
	//	 */
	private List<PlacesPOI> getNearbyPOIs(final JsonUtilityService.JSONObject placesJson) throws JsonException {
		List<PlacesPOI> nearByPOIList = new ArrayList<PlacesPOI>();

		JSONArray nearByPOIArray = placesJson.optJSONArray(PlacesConstants.QueryResponseJsonKeys.POI);

		if (nearByPOIArray == null || nearByPOIArray.length() == 0) {
			return nearByPOIList;
		}

		for (int i = 0; i < nearByPOIArray.length(); i++) {
			JSONObject poiJson = nearByPOIArray.getJSONObject(i);
			PlacesPOI poi = createPlacesPOIFromJson(poiJson);

			if (poi != null) {
				poi.setUserIsWithin(false);
				nearByPOIList.add(poi);
			}
		}

		return nearByPOIList;
	}

	//	/**
	//	 * TODO: Doc Me
	//	 * hint return null when
	//	 * 1. identifier is invalid for the poiJson
	//	 * 2. invalid latitude/ longitude in poiJson
	//	 * 3. poiJson is not in the expected format.
	//	 */
	private PlacesPOI createPlacesPOIFromJson(final JsonUtilityService.JSONObject poiJson) throws JsonException {

		JSONArray poiDetails = poiJson.getJSONArray(PlacesConstants.QueryResponseJsonKeys.POI_DETAILS);

		// bail out by returning null if array length for a poi is not equal to 8
		if (poiDetails.length() != POI_DETAIL_MIN_ARRAY_LENGTH) {
			Log.debug(PlacesConstants.LOG_TAG, "poiJson does not have the expected format");
			return null;
		}

		// retrieve the respective data from the array
		String identifier = poiDetails.optString(POI_INDEX_IDENTIFIER, null);

		if (identifier == null) {
			Log.debug(PlacesConstants.LOG_TAG, "Ignoring a POI, invalid identifier");
			return null;
		}

		String name = poiDetails.optString(POI_INDEX_NAME, "unnamed");


		double latitude;
		double longitude;

		try {
			latitude = Double.parseDouble(poiDetails.optString(POI_INDEX_LATITUDE,
										  String.valueOf(PlacesConstants.INVALID_LAT_LON)));
			longitude = Double.parseDouble(poiDetails.optString(POI_INDEX_LONGITUDE,
										   String.valueOf(PlacesConstants.INVALID_LAT_LON)));
		} catch (Exception exp) {
			// catch the numberFormat and nullPointer Exception
			Log.debug(PlacesConstants.LOG_TAG,
					  "Ignoring POI with identifier %s, exception occurred while reading latitude/ longitude", identifier);
			return null;
		}


		if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
			Log.debug(PlacesConstants.LOG_TAG, "Ignoring POI with identifier %s, invalid latitude/ longitude", identifier);
			return null;
		}

		int radius = poiDetails.optInt(POI_INDEX_RADIUS, DEFAULT_POI_RADIUS);
		String library = poiDetails.optString(POI_INDEX_LIBRARY, "");
		int weight = poiDetails.optInt(POI_INDEX_WEIGHT, DEFAULT_POI_WEIGHT);



		PlacesPOI placesPOI = new PlacesPOI(identifier, name, latitude, longitude, radius, library, weight);

		JSONObject poiMetadata = poiJson.optJSONObject(PlacesConstants.QueryResponseJsonKeys.POI_METADATA);

		if (poiMetadata != null) {
			Map<String, String> metadata = jsonUtilityService.mapFromJsonObject(poiMetadata);
			placesPOI.setMetadata(metadata);
		}

		return placesPOI;
	}


}
