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

public final class PlacesConstants {

	public static final String LOG_TAG = "Places";
	static final String FRIENDLY_NAME = "Places";
	static final String MODULE_NAME = "com.adobe.module.places";

	static final int DEFAULT_NETWORK_TIMEOUT = 2;
	static final int DEFAULT_NEARBYPOI_COUNT = 20;
	static final long DEFAULT_MEMBERSHIP_TTL = 60 * 60;  // 1 hour in seconds
	static final double INVALID_LAT_LON 	= 999.999d;
	private PlacesConstants() {
	}

	public static final class EventName {
		// places request content event names
		public static final String REQUEST_GETUSERWITHINPLACES = "requestgetuserwithinplaces";
		public static final String REQUEST_GETLASTKNOWNLOCATION = "requestgetlastknownlocation";
		public static final String REQUEST_GETNEARBYPLACES = "requestgetnearbyplaces";
		public static final String REQUEST_PROCESSREGIONEVENT = "requestprocessregionevent";
		public static final String REQUEST_RESET = "requestreset";
		public static final String REQUEST_SETAUTHORIZATIONSTATUS = "requestsetauthorizationstatus";

		// places response content event names
		public static final String RESPONSE_GETNEARBYPLACES = "responsegetnearbyplaces";
		public static final String RESPONSE_PROCESSREGIONEVENT = "responseprocessregionevent";
		public static final String RESPONSE_GETUSERWITHINPLACES = "responsegetuserwithinplaces";
		public static final String RESPONSE_GETLASTKNOWNLOCATION = "responsegetlastknownlocation";

		private EventName() {
		}
	}

	static final class DataStoreKeys {
		static final String NEARBYPOIS 				= "nearbypois";
		static final String CURRENT_POI 			= "currentpoi";
		static final String LAST_ENTERED_POI 		= "lastenteredpoi";
		static final String LAST_EXITED_POI 		= "lastexitedpoi";
		static final String LAST_KNOWN_LATITUDE 	= "lastknownlatitude";
		static final String LAST_KNOWN_LONGITUDE 	= "lastknownlongitude";
		static final String AUTH_STATUS 			= "authstatus";
		static final String MEMBERSHIP_VALID_UNTIL 	= "places_membership_valid_until";

		private DataStoreKeys() {
		}
	}

	static final class POIKeys {
		static final String IDENTIFIER = "regionid";
		static final String NAME = "regionname";
		static final String LATITUDE = "latitude";
		static final String LONGITUDE = "longitude";
		static final String RADIUS = "radius";
		static final String METADATA = "regionmetadata";
		static final String USER_IS_WITHIN = "useriswithin";
		static final String LIBRARY = "libraryid";
		static final String WEIGHT = "weight";

		private POIKeys() {
		}
	}

	static final class ServerKeys {
		static final String PLACES_EDGE 	 = "placesedgequery";
		private ServerKeys() {
		}
	}

	static final class Location {
		static final String PROVIDER_TAG = "com.adobe.lastKnownLocation";
		private Location() {
		}
	}

	static final class QueryResponseJsonKeys {
		static final String PLACES = "places";
		static final String POI = "pois";
		static final String POI_MEMBERS = "userWithin";
		static final String POI_DETAILS = "p";
		static final String POI_METADATA = "x";
		private QueryResponseJsonKeys() {
		}
	}

	static final class SharedStateKeys {
		static final String NEARBYPOIS 			= "nearbypois";
		static final String CURRENT_POI 		= "currentpoi";
		static final String LAST_ENTERED_POI 	= "lastenteredpoi";
		static final String LAST_EXITED_POI 	= "lastexitedpoi";
		static final String AUTH_STATUS 		= "authstatus";
		static final String VALID_UNTIL 		= "validuntil";
		private SharedStateKeys() {
		}
	}

	public static final class EventDataKeys {
		static final String STATE_OWNER = "stateowner";


		private EventDataKeys() {
		}

		public static final class Places {
			public 	static final String MODULE_NAME = "com.adobe.module.places";

			// Places Request Content event keys
			public static final String PLACES_COUNT = "count";
			public static final String LATITUDE = "latitude";
			public static final String LONGITUDE = "longitude";

			// Places Response Content event keys
			public static final String NEAR_BY_PLACES_LIST = "nearbypois";
			public static final String RESULT_STATUS = "status";
			public static final String USER_WITHIN_POIS = "userwithinpois";
			public static final String TRIGGERING_REGION = "triggeringregion";

			// request types
			public static final String REQUEST_TYPE = "requesttype";
			public static final String REQUEST_TYPE_GET_NEARBY_PLACES = "requestgetnearbyplaces";
			public static final String REQUEST_TYPE_PROCESS_REGION_EVENT = "requestprocessregionevent";
			public static final String REQUEST_TYPE_GET_USER_WITHIN_PLACES = "requestgetuserwithinplaces";
			public static final String REQUEST_TYPE_GET_LAST_KNOWN_LOCATION = "requestgetlastknownlocation";
			public static final String REQUEST_TYPE_RESET = "requestreset";
			public static final String REQUEST_TYPE_SET_AUTHORIZATION_STATUS = "requestsetauthorizationstatus";

			// Region Keys
			static final String REGION_NAME = "regionname";
			public static final String REGION_ID = "regionid";
			public static final String REGION_EVENT_TYPE = "regioneventtype";
			static final String REGION_METADATA = "regionmetadata";
			static final String REGION_TIMESTAMP = "timestamp";

			// last known location keys
			public static final String LAST_KNOWN_LATITUDE = "lastknownlatitude";
			public static final String LAST_KNOWN_LONGITUDE = "lastknownlongitude";

			// places authorization status
			public static final String AUTH_STATUS = "authstatus";

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
