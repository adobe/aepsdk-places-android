package com.adobe.marketing.mobile.places

import com.adobe.marketing.mobile.*

internal typealias ConfigurationMonitor = (firstValidConfiguration: Map<String, Any>) -> Unit

class MonitorExtension : Extension {
    val PLACES_MODULE_NAME = "com.adobe.module.places"

    companion object {
        var placesSharedState: Map<String, Any>? = null
        var latestRegionEvent: Event? = null
        private var configurationMonitor: ConfigurationMonitor? = null
        internal fun configurationAwareness(callback: ConfigurationMonitor) {
            configurationMonitor = callback
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
            placesSharedState =
                api.getSharedState(PLACES_MODULE_NAME, null, false, SharedStateResolution.ANY)?.value
        }
    }

    fun handlePlacesResponseContent(event: Event) {
        if(event.name == "responseprocessregionevent") {
            latestRegionEvent = event
        }
    }
}