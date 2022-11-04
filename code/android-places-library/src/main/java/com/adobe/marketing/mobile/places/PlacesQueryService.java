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

import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;
import com.adobe.marketing.mobile.services.ServiceProvider;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StreamUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import com.adobe.marketing.mobile.util.URLBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PlacesQueryService {
	private static final String CLASS_NAME = "PlacesQueryService";

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


	/**
	 *
	 * TODO: Doc Me
	 *
	 * @param eventData the {@link Map} containing the parameters to get nearby places.
	 * @param placesConfig an instance of valid {@link PlacesConfiguration}
	 * @return A {@link PlacesQueryResponse} instance containing the places data
	 */
	// Pass non null event data.
	void getNearbyPlaces(final Map<String,Object> eventData, final PlacesConfiguration placesConfig, final PlacesQueryResponseCallback responseCallback) {
		PlacesQueryResponse placesResponse = new PlacesQueryResponse();
		Networking networking = ServiceProvider.getInstance().getNetworkService();

		if (networking == null) {
			placesResponse.fetchFailed("Ignoring the get nearby places event, Networking services not available.",
					PlacesRequestError.INVALID_LATLONG_ERROR);
			responseCallback.call(placesResponse);
			return;
		}

		String queryURL = getQueryURL(eventData, placesConfig);

		if (queryURL == null) {
			placesResponse.fetchFailed("Ignoring the get nearby places event, unable to form query URL",
					PlacesRequestError.INVALID_LATLONG_ERROR);
			responseCallback.call(placesResponse);
			return;
		}

		// add the library query parameter
		queryURL = queryURL + placesConfig.getLibrariesQueryString();

		Log.debug(PlacesConstants.LOG_TAG, "Getting nearby places:  %s", queryURL);
		NetworkRequest request = new NetworkRequest(queryURL, HttpMethod.GET, null, null, PlacesConstants.DEFAULT_NETWORK_TIMEOUT, PlacesConstants.DEFAULT_NETWORK_TIMEOUT);
		networking.connectAsync(request, new NetworkCallback() {
			@Override
			public void call(HttpConnecting connection) {
				if (connection == null) {
					placesResponse.fetchFailed("Unable to get nearby places, connection is null", PlacesRequestError.CONNECTIVITY_ERROR);
					responseCallback.call(placesResponse);
					return;
				}

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					connection.close();
					String message = String.format("Unable to get nearby places, connection failed with status %s, message %s",
							connection.getResponseCode(), connection.getResponseMessage());
					placesResponse.fetchFailed(message, PlacesRequestError.CONNECTIVITY_ERROR);
					responseCallback.call(placesResponse);
					connection.close();
					return;
				}

				try {
					String serverResponse = StreamUtils.readAsString(connection.getInputStream());

					if (StringUtils.isNullOrEmpty(serverResponse)) {
						placesResponse.fetchFailed("Unable to get nearby places, server response is empty",
								PlacesRequestError.SERVER_RESPONSE_ERROR);
						responseCallback.call(placesResponse);
						return;
					}

					Log.debug(PlacesConstants.LOG_TAG, "Got Response : %s", serverResponse);
					JSONObject responseJson = new JSONObject(serverResponse);

					if (responseJson == null) {
						placesResponse.fetchFailed("Unable to get nearby places, invalid json from server response",
								PlacesRequestError.SERVER_RESPONSE_ERROR);
						responseCallback.call(placesResponse);
						return;
					}

					JSONObject placesJson = responseJson.getJSONObject(PlacesConstants.QueryResponseJsonKeys.PLACES);
					placesResponse.nearByPOIs = getNearbyPOIs(placesJson);
					placesResponse.containsUserPOIs = getContainsUserPOIs(placesJson);
					placesResponse.isSuccess = true;
					placesResponse.resultStatus = PlacesRequestError.OK;
					responseCallback.call(placesResponse);
					return;


				} catch (Exception exception) {
					String message = String.format("Unable to get nearby places, Failed with exception: %s", exception);
					placesResponse.fetchFailed(message, PlacesRequestError.SERVER_RESPONSE_ERROR);
					responseCallback.call(placesResponse);
					return;
				}

				finally {
					connection.close();
				}
			}
		});



	}

	/**
	 * TODO: Doc Me
	 *
	 * @param eventData the {@link Map<String,Object>} related to the get nearby places event
	 * @param placesConfig an instance of valid {@link PlacesConfiguration}
	 * @return a url {@link String} to make the places query
	 */
	private String getQueryURL(final Map<String,Object> eventData, final PlacesConfiguration placesConfig) {
		// Grab data from eventData
		double latitude = DataReader.optDouble(eventData, PlacesConstants.EventDataKeys.Places.LATITUDE, PlacesConstants.INVALID_LAT_LON);
		double longitude = DataReader.optDouble(eventData, PlacesConstants.EventDataKeys.Places.LONGITUDE, PlacesConstants.INVALID_LAT_LON);
		int count = DataReader.optInt(eventData, PlacesConstants.EventDataKeys.Places.PLACES_COUNT,
										 PlacesConstants.DEFAULT_NEARBYPOI_COUNT);

		if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "Unable to get nearby places, invalid latitude/longitude");
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
	private List<PlacesPOI> getContainsUserPOIs(final JSONObject placesJson) throws JSONException {
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
	private List<PlacesPOI> getNearbyPOIs(final JSONObject placesJson) throws JSONException {
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
	private PlacesPOI createPlacesPOIFromJson(final JSONObject poiJson) throws JSONException {

		JSONArray poiDetails = poiJson.getJSONArray(PlacesConstants.QueryResponseJsonKeys.POI_DETAILS);

		// bail out by returning null if array length for a poi is not equal to 8
		if (poiDetails.length() != POI_DETAIL_MIN_ARRAY_LENGTH) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "poiJson does not have the expected format");
			return null;
		}

		// retrieve the respective data from the array
		String identifier = poiDetails.optString(POI_INDEX_IDENTIFIER, null);

		if (identifier == null) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "Ignoring a POI, invalid identifier");
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
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME,
					  "Ignoring POI with identifier %s, exception occurred while reading latitude/ longitude", identifier);
			return null;
		}


		if (!(PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude))) {
			Log.debug(PlacesConstants.LOG_TAG, CLASS_NAME, "Ignoring POI with identifier %s, invalid latitude/ longitude", identifier);
			return null;
		}

		int radius = poiDetails.optInt(POI_INDEX_RADIUS, DEFAULT_POI_RADIUS);
		String library = poiDetails.optString(POI_INDEX_LIBRARY, "");
		int weight = poiDetails.optInt(POI_INDEX_WEIGHT, DEFAULT_POI_WEIGHT);



		PlacesPOI placesPOI = new PlacesPOI(identifier, name, latitude, longitude, radius, library, weight);

		JSONObject poiMetadata = poiJson.optJSONObject(PlacesConstants.QueryResponseJsonKeys.POI_METADATA);

		if (poiMetadata != null) {
			Map<String, String> metadata = JSONUtils.convertJsonObjectToStringMap(poiMetadata);
			placesPOI.setMetadata(metadata);
		}

		return placesPOI;
	}
}
