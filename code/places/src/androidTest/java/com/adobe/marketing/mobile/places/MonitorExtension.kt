/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.places

import com.adobe.marketing.mobile.*
import java.util.concurrent.CountDownLatch

internal typealias ConfigurationMonitor = (firstValidConfiguration: Map<String, Any>) -> Unit

class MonitorExtension : Extension {
    val PLACES_MODULE_NAME = "com.adobe.module.places"
    val CURRENTPOI = "currentpoi"
    val LASTENTEREDPOI = "lastenteredpoi"
    val LASTEXITEDPOI = "lastexitedpoi"

    companion object {
        var placesSharedState: Map<String, Any>? = null
        var latestRegionEvent: Event? = null
        var capturedNearByPOIEvent: Event? = null
        var capturedLastKnownLocationEvent: Event? = null
        var capturedUserWithinPlacesEvent: Event? = null
        private var configurationMonitor: ConfigurationMonitor? = null
        var waitForNearByPOIExternalEvent = CountDownLatch(1)
        var waitForLastKnownLocationExternalEvent = CountDownLatch(1)
        var waitForUserWithInPOIExternalEvent = CountDownLatch(1)
        var waitForSharedStateToSet = CountDownLatch(1)
        var waitForRegionEvent = CountDownLatch(1)
        internal fun configurationAwareness(callback: ConfigurationMonitor) {
            configurationMonitor = callback
        }

        internal fun reset() {
            placesSharedState = null
            latestRegionEvent = null
            capturedNearByPOIEvent = null
            capturedLastKnownLocationEvent = null
            capturedUserWithinPlacesEvent = null
            waitForNearByPOIExternalEvent = CountDownLatch(1)
            waitForLastKnownLocationExternalEvent = CountDownLatch(1)
            waitForUserWithInPOIExternalEvent = CountDownLatch(1)
            waitForSharedStateToSet = CountDownLatch(1)
            waitForRegionEvent = CountDownLatch(1)
        }
    }

    private val extensionApi: ExtensionApi

    constructor(extensionApi: ExtensionApi) : super(extensionApi) {
        this.extensionApi = extensionApi
    }

    override fun getName(): String {
        return "MonitorExtension"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun getFriendlyName(): String {
        return "MonitorExtension"
    }

    override fun onRegistered() {
        api.registerEventListener(EventType.HUB, EventSource.SHARED_STATE) {
            handleStateChange(it)
        }
        api.registerEventListener(EventType.PLACES, EventSource.RESPONSE_CONTENT) {
            handlePlacesResponseContent(it)
        }
        api.registerEventListener(EventType.WILDCARD, EventSource.WILDCARD) { event ->
            val result = api.getSharedState(
                "com.adobe.module.configuration",
                event,
                false,
                SharedStateResolution.LAST_SET
            )
            val configuration = result?.value
            configuration?.let {
                configurationMonitor?.let { it(configuration) }
            }
        }
    }

    override fun readyForEvent(event: Event): Boolean {
        return true
    }

    fun handleStateChange(event: Event) {
        if (event.eventData["stateowner"] == PLACES_MODULE_NAME) {
            placesSharedState = api.getSharedState(PLACES_MODULE_NAME, null, false, SharedStateResolution.ANY)?.value
            if(placesSharedState?.containsKey(CURRENTPOI) == true
                || placesSharedState?.containsKey(LASTENTEREDPOI) == true
                || placesSharedState?.containsKey(LASTEXITEDPOI) == true )  {
                waitForSharedStateToSet.countDown()
            }
        }
    }

    fun handlePlacesResponseContent(event: Event) {
        if(event.name == "responseprocessregionevent") {
            latestRegionEvent = event
            waitForRegionEvent.countDown()
        }

        if(event.name == "responsegetlastknownlocation") {
            capturedLastKnownLocationEvent = event
            waitForLastKnownLocationExternalEvent.countDown()
        }

        if(event.name == "responsegetnearbyplaces") {
            capturedNearByPOIEvent = event
            waitForNearByPOIExternalEvent.countDown()
        }

        if(event.name == "responsegetuserwithinplaces") {
            capturedUserWithinPlacesEvent = event
            waitForUserWithInPOIExternalEvent.countDown()
        }
    }
}