/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Adobe Inc.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Inc. and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Inc..
 **************************************************************************/

package com.adobe.marketing.mobile;

import android.location.Location;

import com.adobe.marketing.mobile.places.BuildConfig;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Places {
	private final static String TAG = Places.class.getSimpleName();

	//Links to documentation
	private final static String REGISTER_MOBILECORE_DOC =
		"https://aep-sdks.gitbook.io/docs/using-mobile-extensions/mobile-core/configuration/configuration-api-reference#configurewithappid";
	private final static String REGISTER_PLACES_DOC =
		"https://docs.adobe.com/content/help/en/places/using/places-ext-aep-sdks/places-extension/places-extension.html#register-places-mobile-core";
	private final static String GET_NEARBY_POI_DOC =
		"https://docs.adobe.com/help/en/places/using/configure-places-with-sdk/places-extension/places-api-reference.html#getnearbypointsofinterest-android";
	private final static String PROCESS_GEOFENCE_EVENT_DOC =
		"https://docs.adobe.com/help/en/places/using/configure-places-with-sdk/places-extension/places-api-reference.html#processgeofencingevent-android";
	private final static String PROCESS_GEOFENCE_DOC =
		"https://docs.adobe.com/help/en/places/using/configure-places-with-sdk/places-extension/places-api-reference.html#processgeofence-android";
	private final static String GET_CURRENT_POI_DOC =
		"https://docs.adobe.com/help/en/places/using/configure-places-with-sdk/places-extension/places-api-reference.html#getcurrentpointsofinterest-android";

	private static PlacesCore placesCore;

	private Places() { }

	// =======================================================================
	// Places Public API
	// =======================================================================

	/**
	 * Registers the Places extension with the {@code MobileCore}
	 * <p>
	 * This will allow the extension to send and receive events to and from the SDK.
	 *
	 * @throws InvalidInitException If the registration was not successful
	 */
	public static void registerExtension() throws InvalidInitException {
		Core core = MobileCore.getCore();

		if (core == null) {
			Log.error(TAG,
					  "Unable to register Places since MobileCore is not initialized properly. For more details refer to " +
					  REGISTER_MOBILECORE_DOC);
			throw  new InvalidInitException();
		}

		try {
			// MobileCore may not be loaded or present (maybe because Core extension was not
			// available?). In that case, the Places extension will not initialize itself
			placesCore = new PlacesCore(core.eventHub, new PlacesModuleDetails());
		} catch (Exception e) {
			Log.error(TAG, "Unable to register Places since MobileCore is not initialized properly. Exception: " +
					  e.getLocalizedMessage());
			throw new InvalidInitException();
		}
	}

	/**
	 * Returns the version of the {@link Places} Extension
	 *
	 * @return A {@link String} representing the extension version
	 */
	public static String extensionVersion() {
		return BuildConfig.EXTENSION_VERSION;
	}

	/**
	 * Requests a list of nearby Points of Interest (POI) and returns them in a success callback.
	 *
	 * <p>
	 * In case of any error in retrieving the nearby points of interest. The successCallback is called with empty poi list.
	 * If your application needs to read the error code use getNearbyPointsOfInterest API with errorCallback.
	 *
	 * @param location a {@link Location} object represent the current location of the device
	 * @param limit a non-negative number representing the number of nearby POI to return from the request
	 * @param successCallback called with list of {@link PlacesPOI} objects that represent the nearest POI to the device
	 */
	public static void getNearbyPointsOfInterest(final Location location,
			final int limit,
			final AdobeCallback<List<PlacesPOI>> successCallback) {

		if (!placesIsRegistered()) {
			return;
		}


		placesCore.getNearbyPointsOfInterest(createPlaceGpsLocation(location), limit, successCallback,
		new AdobeCallback<PlacesRequestError>() {
			@Override
			public void call(PlacesRequestError placesRequestError) {
				Log.warning(TAG, "Error occurred in fetching the nearbyPointsOfInterest :" + placesRequestError.toString() +
							". For more details refer to " + GET_NEARBY_POI_DOC);

				if (successCallback != null) {
					successCallback.call(new ArrayList<PlacesPOI>());
				}
			}
		});
	}

	/**
	 * Requests a list of nearby Points of Interest (POI) and returns them in a success callback.
	 *
	 * <p>
	 * Either successCallback or errorCallback is called depending upon the status of the get near by points of interest call.
	 * Look for {@link PlacesRequestError} in errorCallback to get more information about the reason for the failure.
	 *
	 * @param location a {@link Location} object represent the current location of the device
	 * @param limit a non-negative number representing the number of nearby POI to return from the request
	 * @param successCallback called on success, with list of {@link PlacesPOI} objects that represent the nearest POI to the device
	 * @param errorCallback called on failure, with {@link PlacesRequestError} representing the cause of failure
	 */
	public static void getNearbyPointsOfInterest(final Location location,
			final int limit,
			final AdobeCallback<List<PlacesPOI>> successCallback,
			final AdobeCallback<PlacesRequestError> errorCallback) {

		if (!placesIsRegistered()) {
			return;
		}


		placesCore.getNearbyPointsOfInterest(createPlaceGpsLocation(location), limit, successCallback, errorCallback);
	}

	/**
	 * Pass a {@link GeofencingEvent} to be processed by the SDK.
	 * <p>
	 * Calling this method will result in an Event being dispatched in the SDK, allowing for rules to be processed
	 * as a result of the triggering event.
	 *
	 * @param geofencingEvent the {@link GeofencingEvent} object that occurred while entering/exiting a GeoFence
	 */
	public static void processGeofenceEvent(final GeofencingEvent geofencingEvent) {
		if (!placesIsRegistered()) {
			return;
		}

		if (geofencingEvent.hasError()) {
			Log.warning(TAG, "Ignoring call to processGeofenceEvent (provided geofencingEvent has an error: " +
						geofencingEvent.getErrorCode() + " ). For more details refer to " + PROCESS_GEOFENCE_EVENT_DOC);
			return;
		}

		String regionEventType = getRegionTransitionType(geofencingEvent.getGeofenceTransition());

		if (PlacesRegion.PLACE_EVENT_NONE.equals(regionEventType)) {
			Log.debug(TAG,
					  "Ignoring call to processGeofenceEvent (transition type of geofencingEvent is unknown). For more details refer to " +
					  PROCESS_GEOFENCE_EVENT_DOC);
			return;
		}

		List<EventData> regions = createRegionsEventData(geofencingEvent.getTriggeringGeofences(), regionEventType);

		if (regions == null) {
			Log.debug(TAG, "No valid Places region found for the provided GeofenceEvent. For more details refer to " +
					  PROCESS_GEOFENCE_EVENT_DOC);
			return;
		}

		placesCore.processRegionEvent(regions);
	}

	/**
	 * Pass a {@link Geofence} and transition type to be processed by the SDK
	 * <p>
	 * Calling this method will result in an Event being dispatched in the SDK, allowing for rules to be processed
	 * as a result of the triggering event.
	 * <p>
	 * Use this API to pass only the required {@code Geofence} obtained from {@link GeofencingEvent#getTriggeringGeofences()}
	 * which the Places SDK needs to process.
	 * <p>
	 * Pass the transition type from {@link GeofencingEvent#getGeofenceTransition()}.
	 * Currently we support {@link Geofence#GEOFENCE_TRANSITION_ENTER} and {@link Geofence#GEOFENCE_TRANSITION_EXIT} transitions.
	 *
	 * @param geofence the {@code Geofence} object obtained from the {@link GeofencingEvent}
	 * @param transitionType an {@code int} representing the transition type for the passed geofence
	 */
	public static void processGeofence(final Geofence geofence, final int transitionType) {
		if (!placesIsRegistered()) {
			return;
		}

		String regionEventType = getRegionTransitionType(transitionType);

		if (PlacesRegion.PLACE_EVENT_NONE.equals(regionEventType)) {
			Log.debug(TAG, "Ignoring call to processGeofence (transitionType is unknown). For more details refer to " +
					  PROCESS_GEOFENCE_DOC);
			return;
		}

		ArrayList<Geofence> geofences = new ArrayList<>();
		geofences.add(geofence);

		List<EventData> regions = createRegionsEventData(geofences, regionEventType);

		if (regions == null) {
			Log.debug(TAG, "No valid Places region found for the provided Geofence. For more details refer to " +
					  PROCESS_GEOFENCE_DOC);
			return;
		}

		placesCore.processRegionEvent(regions);
	}

	/**
	 * Returns all Points of Interest (POI) in which the device is currently known to be within.
	 *
	 * <p>
	 * Returns empty array when the device is not within any of the configured POIs.
	 *
	 * @param callback called with the list of {@link PlacesPOI} objects that represent the POIs within which the user is currently in
	 */
	public static void getCurrentPointsOfInterest(final AdobeCallback<List<PlacesPOI>> callback) {
		if (callback == null) {
			Log.debug(TAG, "Ignoring call to getCurrentPointsOfInterest (callback is null). For more details refer to " +
					  GET_CURRENT_POI_DOC);
			return;
		}

		if (placesIsRegistered()) {
			placesCore.getUserWithinPointsOfInterest(callback);
		}
	}

	/**
	 * Returns the last latitude and longitude provided to the ACPPlaces Extension.
	 *
	 * <p>
	 * Returns null, if the SDK doesn't know the last known location.
	 * Returns null, if the last known location has invalid latitude/longitude.
	 *
	 * @param callback called with a {@link Location} object representing the last known lat/lon provided to the extension
	 */
	public static void getLastKnownLocation(final AdobeCallback<Location> callback) {
		if (!placesIsRegistered()) {
			return;
		}

		placesCore.getLastKnownLocation(new AdobeCallback<PlacesGpsLocation>() {
			@Override
			public void call(PlacesGpsLocation placesGpsLocation) {
				if (placesGpsLocation == null) {
					callback.call(null);
					return;
				}

				Location lastKnownLocation = new Location("com.adobe.places.lastknownlocation");
				lastKnownLocation.setLatitude(placesGpsLocation.getLatitude());
				lastKnownLocation.setLongitude(placesGpsLocation.getLongitude());
				callback.call(lastKnownLocation);
			}
		});
	}

	/**
	 * Clears out the client-side data for Places in shared state, local storage, and in-memory.
	 */
	public static void clear() {
		if (placesIsRegistered()) {
			placesCore.reset();
		}
	}

	/**
	 * Sets the authorization status in the Places extension.
	 *
	 * The status provided is stored in the Places shared state, and is for reference only.
	 * Calling this method does not impact the actual location authorization status for this device.
	 *
	 * @param status the PlacesAuthorizationStatus to be set for this device
	 */
	public static void setAuthorizationStatus(final PlacesAuthorizationStatus status) {
		if (placesIsRegistered()) {
			placesCore.setPlacesAuthorizationStatus(status);
		}
	}

	// =======================================================================
	// private methods
	// =======================================================================
	/**
	 * Convert android native {@link Location} instance to {@link PlacesGpsLocation}.
	 * <p>
	 * Latitude, longitude, accuracy, altitude and speed are obtained from the {@code Location} object.
	 *
	 * @param location an Android native {@link Location} object
	 * @return a valid {@link PlacesGpsLocation} instance
	 */
	private static PlacesGpsLocation createPlaceGpsLocation(final Location location) {
		PlacesGpsLocation gpsLocation = new PlacesGpsLocation(location.getLatitude(), location.getLongitude());
		gpsLocation.setAccuracy(location.getAccuracy());
		gpsLocation.setAltitude(location.getAltitude());
		gpsLocation.setSpeed(location.getSpeed());
		return gpsLocation;
	}

	/**
	 * Creates a list of event data representing region events for the given list of {@link Geofence} and regionEventType.
	 *
	 * @param geofences {@code List} of {@link Geofence} instances
	 * @param regionEventType eventType for all the geofences in the list
	 * @return a {@link List} of {@link EventData} containing geofence event details
	 */
	private static List<EventData> createRegionsEventData(final List<Geofence> geofences, final String regionEventType) {
		List<EventData> placesRegions = new ArrayList<>();

		if (geofences == null) {
			return placesRegions;
		}

		for (Geofence geofence : geofences) {
			if (geofence == null) {
				continue;
			}

			EventData eventData = new EventData();
			eventData.putString(PlacesConstants.EventDataKeys.Places.REGION_ID, geofence.getRequestId());
			eventData.putString(PlacesConstants.EventDataKeys.Places.REGION_EVENT_TYPE, regionEventType);
			placesRegions.add(eventData);
		}

		return placesRegions;
	}

	/**
	 * Converts the geofenceTransitionEventType to PlacesEventType.
	 *
	 * @param geofenceTransitionType an {@code int} representing geofenceTransitionType
	 * @return an {@code int} representing PlacesEventType
	 */
	private static String getRegionTransitionType(final int geofenceTransitionType) {
		switch (geofenceTransitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return PlacesRegion.PLACE_EVENT_ENTRY;

			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return PlacesRegion.PLACE_EVENT_EXIT;

			default:
				return PlacesRegion.PLACE_EVENT_NONE;
		}
	}

	private static boolean placesIsRegistered() {
		if (placesCore == null) {
			Log.error(TAG,
					  "Places not initialized. Is Adobe Places extension registered with MobileCore? For more details refer to " +
					  REGISTER_PLACES_DOC);
			return false;
		}

		return true;
	}
}

