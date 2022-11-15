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

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.services.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class JSONUtils {
    private static final String CLASS_NAME = "JSONUtils";

    static Map<String, String> convertJsonObjectToStringMap(@NonNull final JSONObject jsonObject) {
        final Map<String, String> map = new HashMap<>();
        final Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            try {
                final Object value = jsonObject.get(key);
                if (value instanceof String) {
                    final String stringValue = (String) value;
                    map.put(key, stringValue);
                } else {
                    Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "The value of [%s] is not String: %s", key);
                }
            } catch (final Exception e) {
                Log.warning(PlacesConstants.LOG_TAG, CLASS_NAME, "The value of [%s] is not supported: %s", key, e);
            }
        }
        return map;
    }
}
