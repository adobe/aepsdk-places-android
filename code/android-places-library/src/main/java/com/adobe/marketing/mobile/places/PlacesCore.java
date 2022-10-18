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

import java.util.ArrayList;
import java.util.List;

class PlacesCore {

	private static final String LOG_TAG = PlacesCore.class.getSimpleName();

	EventHub eventHub;

	PlacesCore(final EventHub eventHub, final ModuleDetails moduleDetails) {
		this(eventHub, moduleDetails, true);
	}

	PlacesCore(final EventHub eventHub, final ModuleDetails moduleDetails, final boolean registerExtension) {
		if (eventHub == null) {
			Log.error(LOG_TAG, "Core initialization was successful (No EventHub instance found!)");
			return;
		}

		this.eventHub = eventHub;

		if (registerExtension) {
			try {
				eventHub.registerModule(PlacesExtension.class, moduleDetails);
				Log.debug(LOG_TAG, "Registered %s extension", PlacesExtension.class.getSimpleName());
			} catch (InvalidModuleException e) {
				Log.debug(LOG_TAG, "Failed to register %s module (%s)", PlacesExtension.class.getSimpleName(), e);
			}
		}

		Log.debug(LOG_TAG, "Core initialization was successful");
	}


	/**
	 * Processes a place/geofence event in the sdk. Used for passive data collection (Analytics, Target, Audience Manager)
	 * and triggering actions using rules (In-app messaging, Postbacks, PII Request, Target profile updates, Audience Manager sync calls)
	 *
	 * NOTE: this method should NOT be called if you're using the Adobe Mobile Places Monitor
	 *
	 * @param placesRegionsData a EventData object containing geofence id and geofence eventType
	 */
	void processRegionEvent(final List<EventData> placesRegionsData) {
		for (EventData placesRegion : placesRegionsData) {
			placesRegion.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
								   PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_PROCESS_REGION_EVENT);
			final Event event = new Event.Builder(PlacesConstants.EventName.REQUEST_PROCESSREGIONEVENT, EventType.PLACES,
												  EventSource.REQUEST_CONTENT)
			.setData(placesRegion)
			.build();
			eventHub.dispatch(event);
		}
	}

	/**
	 * Requests a list of nearby points of interest for the provided search parameters.
	 *
	 * @param gpsLocation		   {@link PlacesGpsLocation} instance representing the current location of the device
	 * @param placesCount          {@code int} maximum number of nearby places to find
	 * @param successCallback 	   {@link AdobeCallback} callback containing an ordered list of nearby places
	 */
	void getNearbyPointsOfInterest(final PlacesGpsLocation gpsLocation,
								   final int placesCount,
								   final AdobeCallback<List<PlacesPOI>> successCallback) {

		getNearbyPointsOfInterest(gpsLocation, placesCount, successCallback, null);
	}


	/**
	 * Requests a list of nearby points of interest for the provided search parameters.
	 *
	 * @param gpsLocation		   {@link PlacesGpsLocation} instance representing the current location of the device
	 * @param placesCount          {@code int} maximum number of nearby places to find
	 * @param successCallback      {@link AdobeCallback} success callback delivering an ordered list of nearby places
	 * @param errorCallback		   {@link AdobeCallback} error callback describring the errorStatus
	 */
	void getNearbyPointsOfInterest(final PlacesGpsLocation gpsLocation,
								   final int placesCount,
								   final AdobeCallback<List<PlacesPOI>> successCallback,
								   final AdobeCallback<PlacesRequestError> errorCallback) {

		// Create Event Data
		EventData eventData = new EventData();
		eventData.putDouble(PlacesConstants.EventDataKeys.Places.LATITUDE, gpsLocation.getLatitude());
		eventData.putDouble(PlacesConstants.EventDataKeys.Places.LONGITUDE, gpsLocation.getLongitude());
		eventData.putInteger(PlacesConstants.EventDataKeys.Places.PLACES_COUNT, placesCount);
		eventData.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
							PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_NEARBY_PLACES);

		final Event event = new Event.Builder(PlacesConstants.EventName.REQUEST_GETNEARBYPLACES, EventType.PLACES,
											  EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		// Register for one time listener only if either of the success or error callback is not null
		if (successCallback != null || errorCallback != null) {
			eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
				@Override
				public void call(final Event e) {
					EventData eventData = e.getData();

					if (eventData == null) {
						callErrorCallback(PlacesRequestError.UNKNOWN_ERROR);
						return;
					}

					try {

						// retrieve the status and the list of POI's from eventData
						List<PlacesPOI> pois = eventData.getTypedList(PlacesConstants.EventDataKeys.Places.NEAR_BY_PLACES_LIST,
											   new PlacesPOIVariantSerializer());
						int resultStatusInteger = eventData.getInteger(PlacesConstants.EventDataKeys.Places.RESULT_STATUS);
						PlacesRequestError status = PlacesRequestError.fromInt(resultStatusInteger);

						// call the successCallback if the status is OK
						if (status == PlacesRequestError.OK) {
							callSuccessCallback(pois);
							return;
						}

						// else call the errorCallback with the obtained status
						callErrorCallback(status);


					} catch (VariantException exp) {
						callErrorCallback(PlacesRequestError.UNKNOWN_ERROR);
					}

				}

				private void callErrorCallback(final PlacesRequestError resultStatus) {
					Log.debug(PlacesConstants.LOG_TAG, "Error occurred while retrieving nearbyPOIs, Status code: " + resultStatus +
							  ". For more details refer to " + PlacesConstants.DocsLinks.GET_NEARBY_POI_EVENT);

					if (errorCallback != null) {
						errorCallback.call(resultStatus);
					}
				}

				private void callSuccessCallback(final List<PlacesPOI> pois) {
					if (successCallback != null) {
						successCallback.call(pois);
					}
				}

			});
		}

		eventHub.dispatch(event);
	}

	/**
	 * Returns a list of POIs that the user is currently within.
	 *
	 * @param callback the method to be called with a list of PlacesDataObjects representing POIs that the user is in
	 */
	void getUserWithinPointsOfInterest(final AdobeCallback<List<PlacesPOI>> callback) {

		if (callback == null) {
			Log.debug(LOG_TAG, "Called GetUserWithinPointsOfInterest with no callback.");
			return;
		}

		// Create Event Data
		EventData eventData = new EventData();
		eventData.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
							PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_USER_WITHIN_PLACES);

		final Event event = new Event.Builder(PlacesConstants.EventName.REQUEST_GETUSERWITHINPLACES, EventType.PLACES,
											  EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				EventData eventData = e.getData();

				if (eventData == null) {
					callback.call(new ArrayList<PlacesPOI>());
					return;
				}

				try {
					callback.call(eventData.getTypedList(PlacesConstants.EventDataKeys.Places.USER_WITHIN_POIS,
														 new PlacesPOIVariantSerializer()));
				} catch (VariantException exp) {
					callback.call(new ArrayList<PlacesPOI>());
				}

			}
		});

		eventHub.dispatch(event);
	}

	/**
	 * Returns the last latitude and longitude provided to the ACPPlaces Extension.
	 *
	 * <p>
	 * Returns null, if the SDK doesn't know the last known location.
	 * Returns null, if the last known location has invalid latitude/longitude.
	 *
	 * @param callback called with a Location object representing the last known lat/lon of the extension
	 */
	void getLastKnownLocation(final AdobeCallback<PlacesGpsLocation> callback) {
		if (callback == null) {
			Log.debug(LOG_TAG, "Called getLastKnownLocation with no callback. Ignoring.");
			return;
		}

		// Create Event Data
		EventData eventData = new EventData();
		eventData.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
							PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_GET_LAST_KNOWN_LOCATION);

		final Event event = new Event.Builder(PlacesConstants.EventName.REQUEST_GETLASTKNOWNLOCATION, EventType.PLACES,
											  EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();

		eventHub.registerOneTimeListener(event.getResponsePairID(), new Module.OneTimeListenerBlock() {
			@Override
			public void call(final Event e) {
				EventData eventData = e.getData();

				if (eventData == null || eventData.isEmpty()) {
					callback.call(null);
					return;
				}

				double latitude = eventData.optDouble(PlacesConstants.EventDataKeys.Places.LAST_KNOWN_LATITUDE,
													  PlacesConstants.INVALID_LAT_LON);
				double longitude = eventData.optDouble(PlacesConstants.EventDataKeys.Places.LAST_KNOWN_LONGITUDE,
													   PlacesConstants.INVALID_LAT_LON);

				if (PlacesUtil.isValidLat(latitude) && PlacesUtil.isValidLon(longitude)) {
					callback.call(new PlacesGpsLocation(latitude, longitude));
					return;
				}

				callback.call(null);

			}
		});

		eventHub.dispatch(event);

	}

	/**
	 * Sets the device's authorization state for access to use location on the device
	 *
	 * The status provided is stored in the Places shared state, and is for reference only.
	 * It does not impact the actual authorization status with the OS (only the user can provide this).
	 *
	 * @param status a {@link PlacesAuthorizationStatus} value representative of the device's location authorization
	 * @see PlacesAuthorizationStatus
	 */
	void setPlacesAuthorizationStatus(final PlacesAuthorizationStatus status) {
		if (status == null) {
			Log.warning(PlacesCore.LOG_TAG,
						"PlacesAuthorization status cannot be set to null. Provide a valid value. For more details refer to " +
						PlacesConstants.DocsLinks.AUTHORIZATION_STATUS);
			return;
		}

		EventData eventData = new EventData();
		eventData.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
							PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_SET_AUTHORIZATION_STATUS);
		eventData.putString(PlacesConstants.EventDataKeys.Places.AUTH_STATUS, status.stringValue());

		final Event event = new Event.Builder(PlacesConstants.EventName.REQUEST_SETAUTHORIZATIONSTATUS,
											  EventType.PLACES,
											  EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();
		eventHub.dispatch(event);
	}


	/**
	 * Reset the shared state, local, and persisted client-side data for the Places extension.
	 */
	void reset() {
		// Create Event Data
		EventData eventData = new EventData();
		eventData.putString(PlacesConstants.EventDataKeys.Places.REQUEST_TYPE,
							PlacesConstants.EventDataKeys.Places.REQUEST_TYPE_RESET);

		// make an event
		final Event event = new Event.Builder(PlacesConstants.EventName.REQUEST_RESET, EventType.PLACES,
											  EventSource.REQUEST_CONTENT)
		.setData(eventData)
		.build();


		// dispatch the event
		eventHub.dispatch(event);
	}
}
