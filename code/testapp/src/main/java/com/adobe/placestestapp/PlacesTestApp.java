package com.adobe.placestestapp;

import android.app.Application;

import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.Places;

import java.util.Arrays;
import java.util.HashMap;

public class PlacesTestApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		MobileCore.setApplication(this);
		MobileCore.setLogLevel(LoggingMode.VERBOSE);
		MobileCore.registerExtensions(Arrays.asList(Places.EXTENSION, Assurance.EXTENSION), (AdobeCallback) o -> {
			MobileCore.configureWithAppID("");
			HashMap<String, Object> cong = new HashMap<>();
			cong.put("places.membershipttl", 50);
			MobileCore.updateConfiguration(cong);
		});
	}
}
