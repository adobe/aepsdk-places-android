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

import android.app.Application
import android.location.Location
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.services.*
import com.google.android.gms.location.Geofence
import junit.framework.Assert.*
import org.junit.*
import org.junit.runner.RunWith
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

typealias NetworkMonitor = (request: NetworkRequest) -> Unit

private const val PLACES_DATA_STORE = "placesdatastore"
private const val CONFIG_DATA_STORE = "AdobeMobile_ConfigState"

@RunWith(AndroidJUnit4::class)
class PlacesIntegrationTest {
    companion object {
        private var networkMonitor: NetworkMonitor? = null
        private var dataStore: NamedCollection? = null
        var networkResponseCode = 200
        var networkResponseData = readFile("response_t-1_c-0.json")


        private fun setupNetwork() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                var connection: HttpConnecting? = MockedHttpConnecting(networkResponseCode, networkResponseData)

                if (callback != null && connection != null) {
                    callback.call(connection)
                } else {
                    // If no callback is passed by the client, close the connection.
                    connection?.close()
                }
                networkMonitor?.let { it(request) }
            }
        }

        private fun readFile(fileName : String) : String {
            return PlacesIntegrationTest::class.java.getResource("/$fileName")?.readText() ?: ""
        }
    }

    @Before
    fun setup() {
        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

        val countDownLatch = CountDownLatch(1)
        setupNetwork()

        // initialize Places extension
        MobileCore.setApplication(appContext)
        MobileCore.setLogLevel(LoggingMode.DEBUG)
        MobileCore.registerExtensions(listOf(Places.EXTENSION, MonitorExtension::class.java)) {
            countDownLatch.countDown()
        }
        countDownLatch.await(100, TimeUnit.MILLISECONDS)

        setNetworkResponse()
    }

    @After
    fun reset() {
        dataStore = ServiceProvider.getInstance().dataStoreService?.getNamedCollection(PLACES_DATA_STORE)
        dataStore?.removeAll()

        dataStore = ServiceProvider.getInstance().dataStoreService?.getNamedCollection(CONFIG_DATA_STORE)
        dataStore?.removeAll()

        SDKHelper.resetSDK()
        networkMonitor = null
    }

    @Test
    fun test_extensionVersion() {
        assertEquals( "2.0.0", Places.extensionVersion())
    }

    //---------------------------------------------------------------------------------------------
    // GetNearByPOI tests
    //---------------------------------------------------------------------------------------------
    @Test
    fun test_getNearByPOIs_verify_networkRequest() {
        // setup
        val countDownLatch = CountDownLatch(2)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{
            countDownLatch.countDown()
        }, {})

        // verify
        networkMonitor = { request ->
            // verify network request url
            assertEquals("https://placesendpoint/placesedgequery?latitude=22.22&longitude=33.33&limit=20&library=library1",  request.url)
            assertEquals(HttpMethod.GET,  request.method)
            assertNull(request.body)
            countDownLatch.countDown()
        }
        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS))
    }

    @Test
    fun test_getNearByPOIs_whenReturns_validResponse() {
        // setup
        setNetworkResponse(fileName = "validQuery.json")
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{ pois ->
            assertEquals(10, pois.size)
            countDownLatch.countDown()
        }, { error ->
            countDownLatch.countDown()
        })

        // wait
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)

        // verify
        assertEquals("Cityview Plaza", getCurrentPOIName())
        assertEquals("Cityview Plaza", getLastEnteredPOIName())
        assertNull(getLastExitedPOI())

        // verify response event is dispatched for external listeners
        Assert.assertTrue(MonitorExtension.waitForNearByPOIExternalEvent.await(3, TimeUnit.SECONDS))
        assertNotNull(MonitorExtension.capturedNearByPOIEvent)
    }

    @Test
    fun test_getNearByPOIs_when_invalidLatLong() {
        // setup
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        val invalidLocation = Location("invalid")
        invalidLocation.latitude = -1111.2222
        invalidLocation.longitude = 400.00
        Places.getNearbyPointsOfInterest(invalidLocation, 20,{}, {
                error ->
            assertEquals(PlacesRequestError.INVALID_LATLONG_ERROR, error)
            countDownLatch.countDown()
        })

        // verify
        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        // verify no event is dispatched for external listeners
        Assert.assertFalse(MonitorExtension.waitForNearByPOIExternalEvent.await(2, TimeUnit.SECONDS))
        assertNull(MonitorExtension.capturedNearByPOIEvent)
    }

    @Test
    fun test_getNearByPOIs_whenReturns_invalidJSON() {

        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()
        networkResponseData = readFile("invalid.json")

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{ pois ->
        }, { error ->
            assertEquals(PlacesRequestError.SERVER_RESPONSE_ERROR, error)
            countDownLatch.countDown()
        })

        // verify
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
        assertNull(getCurrentPOIName())
        assertNull(getLastEnteredPOIName())
        assertNull(getLastExitedPOI())
    }

    @Test
    fun test_getNearByPOIs_when_responseWIthInvalidPOIArrayKeys() {

        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()
        networkResponseData = readFile("invalidkeys.json")

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{ pois ->
            assertEquals(0,pois.size)
            countDownLatch.countDown()
        }, {})

        // verify
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
        assertNull(getCurrentPOIName())
        assertNull(getLastEnteredPOIName())
        assertNull(getLastExitedPOI())
    }

    @Test
    fun test_getNearByPOIs_when_response_withInvalidNearbyPOIValues_and_validUserWithinPOI() {

        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()
        networkResponseData = readFile("invalidPOIsValue.json")

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{ pois ->
            assertEquals(1 , pois.size)
            countDownLatch.countDown()
        }, {})

        // wait
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
        MonitorExtension.waitForSharedStateToSet.await(3,TimeUnit.SECONDS)

        // verify
        assertEquals("poiName1", getCurrentPOIName())
        assertEquals("poiName1", getLastEnteredPOIName())
        assertNull(getLastExitedPOI())
    }

    @Test
    fun test_getNearByPOIs_when_privacyOptedOut() {
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration(privacyStatus = MobilePrivacyStatus.OPT_OUT.value)

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{}, { error ->
            assertEquals(PlacesRequestError.PRIVACY_OPTED_OUT, error)
            countDownLatch.countDown()
        })

        // verify
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    @Test
    fun test_getNearByPOIs_when_privacyOptUnknown() {
        // setup
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration(privacyStatus = MobilePrivacyStatus.UNKNOWN.value)

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{}, {})

        networkMonitor = { request ->
            // verify network request made
            assertEquals("https://placesendpoint/placesedgequery?latitude=22.22&longitude=33.33&limit=20&library=library1",  request.url)
            countDownLatch.countDown()
        }

        // verify
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    @Test
    fun test_getNearByPOIs_when_multipleLibrary() {
        val countDownLatch = CountDownLatch(2)

        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration(libraries = listOf("lib1", "lib2", "lib3"))

        networkMonitor = { request ->
            // verify network request made
            assertEquals(request.url, "https://placesendpoint/placesedgequery?latitude=22.22&longitude=33.33&limit=20&library=lib1&library=lib2&library=lib3")
            countDownLatch.countDown()
        }

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{
            countDownLatch.countDown()
        }, {})

        // verify
        Assert.assertTrue(countDownLatch.await(2, TimeUnit.SECONDS))
    }

    @Test
    fun test_getNearByPOIs_when_placesNotConfigured() {
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration(libraries = listOf(), endpoint = "")


        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{}, { error ->
            assertEquals(PlacesRequestError.CONFIGURATION_ERROR, error)
            countDownLatch.countDown()
        })

        // verify
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    //---------------------------------------------------------------------------------------------
    // Get Last Known Location Tests
    //---------------------------------------------------------------------------------------------

    @Test
    fun test_getLastKnownLocation() {
        // setup
        val countDownLatch1 = CountDownLatch(1)
        val countDownLatch2 = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20, {
            countDownLatch1.countDown()
        }, {})
        Assert.assertTrue(countDownLatch1.await(1, TimeUnit.SECONDS))

        Places.getLastKnownLocation { location ->
            assertEquals(mockLocation().latitude, location.latitude)
            assertEquals(mockLocation().longitude, location.longitude)
            countDownLatch2.countDown()
        }

        Assert.assertTrue(countDownLatch2.await(1, TimeUnit.SECONDS))

        // verify response event is dispatched for external listeners
        Assert.assertTrue(MonitorExtension.waitForLastKnownLocationExternalEvent.await(5, TimeUnit.SECONDS))
        assertNotNull(MonitorExtension.capturedLastKnownLocationEvent)
    }

    @Test
    fun test_getLastKnownLocation_whenNoLocationSet() {
        // setup
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getLastKnownLocation { location ->
            assertNull(location)
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    //---------------------------------------------------------------------------------------------
    // Reset
    //---------------------------------------------------------------------------------------------
    @Test
    fun test_placesClear() {
        // setup
        var countDownLatch = CountDownLatch(1)
        setNetworkResponse(fileName = "validQuery.json")
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20,{ pois ->
            countDownLatch.countDown()
        }, {})

        // wait
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)

        // verify
        assertEquals("Cityview Plaza", getCurrentPOIName())
        assertEquals("Cityview Plaza", getLastEnteredPOIName())
        assertNull(getLastExitedPOI())

        // reset
        MonitorExtension.reset()

        // test
        Places.clear()
        countDownLatch = CountDownLatch(1)

        // wait
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)

        // verify shared states are reset
        assertNull(getCurrentPOIName())
        assertNull(getLastEnteredPOIName())
        assertNull(getLastExitedPOI())

        // verify last known location is reset
        Places.getLastKnownLocation { location ->
            assertNull(location)
            countDownLatch.countDown()
        }

        // wait and verify
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    //---------------------------------------------------------------------------------------------
    // SetAuthorizationStatus
    //---------------------------------------------------------------------------------------------
    @Test
    fun test_setAuthorizationStatus() {
        // setup
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.setAuthorizationStatus(PlacesAuthorizationStatus.WHEN_IN_USE)
        Thread.sleep(1000)

        // verify
        assertEquals("wheninuse",getAuthStatus())

        // now upgrades to always
        Places.setAuthorizationStatus(PlacesAuthorizationStatus.ALWAYS)
        Thread.sleep(1000)

        // verify
        assertEquals("always",getAuthStatus())
    }

    //---------------------------------------------------------------------------------------------
    // GetCurrentPOITests
    //---------------------------------------------------------------------------------------------

    @Test
    fun test_getCurrentPOI() {
        // setup
        val countDownLatch = CountDownLatch(1)
        setNetworkResponse(fileName = "response_t-2_c-1.json")
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getNearbyPointsOfInterest(mockLocation(), 20, {}, {})
        Places.getCurrentPointsOfInterest() { pois ->
            assertEquals(1 , pois.size)
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))

        // verify response event is dispatched for external listeners
        Assert.assertTrue(MonitorExtension.waitForUserWithInPOIExternalEvent.await(5, TimeUnit.SECONDS))
        assertNotNull(MonitorExtension.waitForUserWithInPOIExternalEvent)
    }

    @Test
    fun test_getCurrentPOI_whenNotInAnyPOI() {
        // setup
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()

        // test
        Places.getCurrentPointsOfInterest() { pois ->
            assertEquals(0 , pois.size)
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    //---------------------------------------------------------------------------------------------
    // Process Region Entry Exit
    //---------------------------------------------------------------------------------------------
    @Test
    fun test_regionEntryEvent() {
        // setup
        val countDownLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()
        setNetworkResponse(fileName = "validQuery.json")

        // test
        Places.getNearbyPointsOfInterest(mockLocation(),20,{
            countDownLatch.countDown()
        },{})
        Assert.assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))

        // wait
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)

        // verify
        assertEquals("Cityview Plaza", getCurrentPOIName())
        assertEquals("Cityview Plaza", getLastEnteredPOIName())

        val geofence = Geofence.Builder().setCircularRegion(
            22.22,
            33.33,
            200.0f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .setNotificationResponsiveness(0)
            .setRequestId("a5f6cd21-3acb-4c76-90d3-52bfea5aa1ad")// for POI Meridian and SanCarlos
            .build()

        // prepare
        MonitorExtension.reset()

        // enter into a geofence
        Places.processGeofence(geofence, Geofence.GEOFENCE_TRANSITION_ENTER)

        // wait for response process region event
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)
        MonitorExtension.waitForRegionEvent.await(3, TimeUnit.SECONDS)

        // verify the current and last entered POI
        assertEquals("Meridian & SanCarlos", getCurrentPOIName())
        assertEquals("Meridian & SanCarlos", getLastEnteredPOIName())

        // verify the region entry event dispatched
        assertNotNull(MonitorExtension.latestRegionEvent)
        assertEquals("entry",getLastRegionEventType())
        assertEquals("Meridian & SanCarlos",getLastRegionEventPOIName())

        // wait for experience event
        assertTrue(MonitorExtension.waitForExperienceEvent.await(3, TimeUnit.SECONDS))

        // verify the edge event dispatched
        assertNotNull(MonitorExtension.latestEdgeEvent)
        assertEquals("location.entry", getEdgeEventType())
        assertEquals("Meridian & SanCarlos", getEdgeEventPOIName())
        assertEquals("a5f6cd21-3acb-4c76-90d3-52bfea5aa1ad", getEdgeEventPoiId())
    }


    @Test
    fun test_regionExitEvent() {
        // setup
        val callbackLatch = CountDownLatch(1)
        val configurationLatch = CountDownLatch(1)
        configurationAwareness { configurationLatch.countDown() }
        setupConfiguration()
        setNetworkResponse(fileName = "validQuery.json")

        // test
        Places.getNearbyPointsOfInterest(mockLocation(),20,{
            callbackLatch.countDown()
        },{})

        // wait
        Assert.assertTrue(callbackLatch.await(3, TimeUnit.SECONDS))
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)

        // verify
        assertEquals("Cityview Plaza", getCurrentPOIName())
        assertEquals("Cityview Plaza", getLastEnteredPOIName())

        val geofence = Geofence.Builder().setCircularRegion(
            22.22,
            33.33,
            200.0f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
            .setNotificationResponsiveness(0)
            .setRequestId("d74cb328-d2f3-4ea9-9af8-7dc8c3393280")// exit Cityview Plaza
            .build()

        // prepare
        MonitorExtension.reset()

        // enter into a geofence
        Places.processGeofence(geofence, Geofence.GEOFENCE_TRANSITION_EXIT)

        // wait
        MonitorExtension.waitForSharedStateToSet.await(3, TimeUnit.SECONDS)
        MonitorExtension.waitForRegionEvent.await(3, TimeUnit.SECONDS)

        // verify the current, last entered and last exited POIs
        assertNull(getCurrentPOIName())
        assertEquals("Cityview Plaza", getLastEnteredPOIName())
        assertEquals("Cityview Plaza", getLastExitedPOI())

        // verify the region exit event dispatched
        assertNotNull(MonitorExtension.latestRegionEvent)
        assertEquals("exit",getLastRegionEventType())
        assertEquals("Cityview Plaza",getLastRegionEventPOIName())


        // wait for experience event
        assertTrue(MonitorExtension.waitForExperienceEvent.await(3, TimeUnit.SECONDS))

        // verify the edge event dispatched
        assertNotNull(MonitorExtension.latestEdgeEvent)
        assertEquals("location.exit", getEdgeEventType())
        assertEquals("Cityview Plaza", getEdgeEventPOIName())
        assertEquals("d74cb328-d2f3-4ea9-9af8-7dc8c3393280", getEdgeEventPoiId())
    }

    //---------------------------------------------------------------------------------------------
    // Private methods
    //---------------------------------------------------------------------------------------------

    private fun setupConfiguration(libraries: List<String>? = listOf<String>("library1"),
                                   endpoint: String? = "placesendpoint",
                                   privacyStatus: String? = MobilePrivacyStatus.OPT_IN.value,
                                   membershipTtl: Long? = 20,
                                   datasetId: String = "1234") {
        val libraryConfig : MutableList<Map<String,String>> = ArrayList()
        if (libraries != null) {
            for (library in libraries) {
                libraryConfig.add(mapOf(
                    PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_LIBRARY_ID to library
                ))
            }
        }
        MobileCore.updateConfiguration(mapOf(
            PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_ENDPOINT to endpoint,
            PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_MEMBERSHIP_TTL to membershipTtl,
            PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_GLOBAL_PRIVACY to privacyStatus,
            PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_PLACES_LIBRARIES to libraryConfig,
            PlacesConstants.EventDataKeys.Configuration.CONFIG_KEY_EXPERIENCE_EVENT_DATASET to datasetId
        ))
    }


    private fun mockLocation() : Location {
        val location = Location("deviceLocation")
        location.latitude = 22.22
        location.longitude = 33.33
        return location
    }

    private fun setNetworkResponse(code : Int = 200,
                                   fileName: String = "noPOIResponse.json") {
        networkResponseData = readFile(fileName)
        networkResponseCode = code
    }

    private fun getCurrentPOIName() : String? {
        val currentPoi = MonitorExtension.placesSharedState?.get("currentpoi") as Map<*, *>?
        return currentPoi?.get("regionname") as String?
    }

    private fun getLastEnteredPOIName() : String? {
        val lastEnteredPOI = MonitorExtension.placesSharedState?.get("lastenteredpoi") as Map<*, *>?
        return lastEnteredPOI?.get("regionname") as String?
    }

    private fun getLastExitedPOI() : String? {
        val lastExitedPOI = MonitorExtension.placesSharedState?.get("lastexitedpoi") as Map<*, *>?
        return lastExitedPOI?.get("regionname") as String?
    }

    private fun getAuthStatus() : String? {
        return MonitorExtension.placesSharedState?.get("authstatus") as String?
    }

    private fun getLastRegionEventType() : String? {
        return MonitorExtension.latestRegionEvent?.eventData?.get("regioneventtype") as String?
    }

    private fun getLastRegionEventPOIName() : String? {
        val triggeringRegion = MonitorExtension.latestRegionEvent?.eventData?.get("triggeringregion") as Map<*, *>?
        return triggeringRegion?.get("regionname") as String?
    }

    private fun getEdgeEventType() : String? {
        val xdm = MonitorExtension.latestEdgeEvent?.eventData?.get("xdm") as Map<*, *>?
        return xdm?.get("eventType") as String?
    }

    private fun getEdgeEventPOIName() : String? {
        val xdm = MonitorExtension.latestEdgeEvent?.eventData?.get("xdm") as Map<*, *>?
        val poiDetail = ((xdm?.get("placeContext") as Map<*, *>?)
            ?.get("POIinteraction") as Map<*, *>?)?.get("poiDetail") as Map<*, *>?
        return poiDetail?.get("name") as String?
    }

    private fun getEdgeEventPoiId() : String? {
        val xdm = MonitorExtension.latestEdgeEvent?.eventData?.get("xdm") as Map<*, *>?
        val poiEntries = ((xdm?.get("placeContext") as Map<*, *>?)
            ?.get("POIinteraction") as Map<*, *>?)?.get("poiDetail") as Map<*, *>?
        return poiEntries?.get("poiID") as String?
    }
}

private fun configurationAwareness(callback: ConfigurationMonitor) {
    MonitorExtension.configurationAwareness(callback)
}

private class MockedHttpConnecting (val mockResponseCode: Int, val mockedResponse: String) : HttpConnecting {

    override fun getInputStream(): InputStream? {
        return mockedResponse.byteInputStream()
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return mockResponseCode
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return ""
    }

    override fun close() {}

}