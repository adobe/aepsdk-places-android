package com.adobe.placestestapp;

import android.app.Application;
import android.util.Log;

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
		MobileCore.registerExtensions(Arrays.asList(Places.EXTENSION), null);

		try {
			Places.registerExtension();

		} catch (Exception e) {
			Log.e("", "");
		}

		MobileCore.start(null);
		MobileCore.configureWithAppID("launch-EN81be7cedb7f14132968641a6ec683adf");
		HashMap<String, Object> cong = new HashMap<String, Object>();
		cong.put("places.membershipttl", 50);
		MobileCore.updateConfiguration(cong);
	}
}
