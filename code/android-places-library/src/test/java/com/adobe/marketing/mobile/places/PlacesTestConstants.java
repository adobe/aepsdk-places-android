/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2018 Adobe Inc.
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

final class PlacesTestConstants {

	static final String LOG_TAG = PlacesExtension.class.getSimpleName();
	static final int DEFAULT_RADIUS = 500;
	static final int DEFAULT_NETWORK_TIMEOUT = 2;
	static final int DEFAULT_NEARBYPOI_COUNT = 50;
	static final long DEFAULT_MEMBERSHIP_TTL = 60 * 60;  // 1 hour in seconds
	static final double INVALID_LAT_LON 	= 999.999d;
	private PlacesTestConstants() {
	}

	static final class EventName {
		// places request content event names
		static final String REQUEST_GETUSERWITHINPLACES = "requestgetuserwithinplaces";
		static final String REQUEST_GETLASTKNOWNLOCATION = "requestgetlastknownlocation";
		static final String REQUEST_GETNEARBYPLACES = "requestgetnearbyplaces";
		static final String REQUEST_PROCESSREGIONEVENT = "requestprocessregionevent";
		static final String REQUEST_RESET = "requestreset";
		static final String REQUEST_SETAUTHORIZATIONSTATUS = "requestsetauthorizationstatus";

		// places response content event names
		static final String RESPONSE_GETNEARBYPLACES = "responsegetnearbyplaces";
		static final String RESPONSE_PROCESSREGIONEVENT = "responseprocessregionevent";
		static final String RESPONSE_GETUSERWITHINPLACES = "responsegetuserwithinplaces";
		static final String RESPONSE_GETLASTKNOWNLOCATION = "responsegetlastknownlocation";

		private EventName() {
		}
	}

	static final class DataStoreKeys {
		static final String NEARBYPOIS 	= "nearbypois";
		static final String CURRENT_POI = "currentpoi";
		static final String LAST_ENTERED_POI = "lastenteredpoi";
		static final String LAST_EXITED_POI = "lastexitedpoi";
		static final String LAST_KNOWN_LATITUDE = "lastknownlatitude";
		static final String LAST_KNOWN_LONGITUDE = "lastknownlongitude";
		static final String AUTH_STATUS = "authstatus";
		static final String MEMBERSHIP_VALID_UNTIL 	= "places_membership_valid_until";

		private DataStoreKeys() {
		}
	}

	static final class ServerKeys {
		static final String PLACES_EDGE 	 = "placesedgequery";
		private ServerKeys() {
		}
	}

	static final class SharedStateKeys {
		static final String NEARBYPOIS 	= "nearbypois";
		static final String CURRENT_POI = "currentpoi";
		static final String LAST_ENTERED_POI = "lastenteredpoi";
		static final String LAST_EXITED_POI = "lastexitedpoi";
		static final String AUTH_STATUS = "authstatus";
		static final String VALID_UNTIL 		= "validuntil";
		private SharedStateKeys() {
		}
	}

	static final class EventDataKeys {
		static final String STATE_OWNER = "stateowner";


		private EventDataKeys() {
		}

		static final class Places {
			static final String MODULE_NAME = "com.adobe.module.places";

			// Places Request Content event keys
			static final String PLACES_COUNT = "count";
			static final String LATITUDE = "latitude";
			static final String LONGITUDE = "longitude";

			// Places Response Content event keys
			static final String NEAR_BY_PLACES_LIST = "nearbypois";
			static final String USER_WITHIN_POIS = "userwithinpois";
			static final String RESULT_STATUS = "status";
			static final String TRIGGERING_REGION = "triggeringregion";

			// request types
			static final String REQUEST_TYPE = "requesttype";
			static final String REQUEST_TYPE_GET_NEARBY_PLACES = "requestgetnearbyplaces";
			static final String REQUEST_TYPE_PROCESS_REGION_EVENT = "requestprocessregionevent";
			static final String REQUEST_TYPE_GET_USER_WITHIN_PLACES = "requestgetuserwithinplaces";
			static final String REQUEST_TYPE_GET_LAST_KNOWN_LOCATION = "requestgetlastknownlocation";
			static final String REQUEST_TYPE_RESET = "requestreset";
			static final String REQUEST_TYPE_SET_AUTHORIZATION_STATUS = "requestsetauthorizationstatus";

			// Region Keys
			static final String REGION_NAME = "regionname";
			static final String REGION_ID = "regionid";
			static final String REGION_EVENT_TYPE = "regioneventtype";
			static final String REGION_METADATA = "regionmetadata";
			static final String REGION_TIMESTAMP = "timestamp";

			// last known location keys
			static final String LAST_KNOWN_LATITUDE = "lastknownlatitude";
			static final String LAST_KNOWN_LONGITUDE = "lastknownlongitude";

			// places authorization status
			static final String AUTH_STATUS = "authstatus";

			private Places() {
			}
		}


		static final class Configuration {
			static final String EXTENSION_NAME = "com.adobe.module.configuration";

			// config  keys
			static final String CONFIG_KEY_GLOBAL_PRIVACY = "global.privacy";
			static final String CONFIG_KEY_PLACES_LIBRARIES = "places.libraries";
			static final String CONFIG_KEY_LIBRARY_ID = "id";
			static final String CONFIG_KEY_PLACES_ENDPOINT = "places.endpoint";
			static final String CONFIG_KEY_PLACES_MEMBERSHIP_TTL = "places.membershipttl";
			private Configuration() {
			}
		}

	}
}
