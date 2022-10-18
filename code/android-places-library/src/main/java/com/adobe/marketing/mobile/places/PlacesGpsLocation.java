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

class PlacesGpsLocation {
	private double latitude;
	private double longitude;
	private double altitude;
	private float accuracy;
	private float speed;
	private long timestamp;


	PlacesGpsLocation(final double latitude, final double longitude) {
		this();
		this.latitude = latitude;
		this.longitude = longitude;
		this.timestamp = TimeUtil.getUnixTimeInSeconds();
	}


	double getLatitude() {
		return latitude;
	}

	double getLongitude() {
		return longitude;
	}

	double getAltitude() {
		return altitude;
	}

	float getAccuracy() {
		return accuracy;
	}

	float getSpeed() {
		return speed;
	}

	long getTimestamp() {
		return timestamp;
	}

	void setAltitude(final double altitude) {
		this.altitude = altitude;
	}

	void setAccuracy(final float accuracy) {
		this.accuracy = accuracy;
	}

	void setSpeed(final float speed) {
		this.speed = speed;
	}

	private PlacesGpsLocation() {}

}
