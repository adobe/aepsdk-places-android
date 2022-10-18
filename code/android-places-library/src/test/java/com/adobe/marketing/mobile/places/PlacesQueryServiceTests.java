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

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class PlacesQueryServiceTests extends BaseTest {

	private PlacesQueryService queryService;
	private MockNetworkService networkService;
	private JsonUtilityService jsonUtilityService;

	private static double SAMPLE_LATITUDE       = 31.44234;
	private static double SAMPLE_LONGITUDE      = -121.06527;
	private static int SAMPLE_COUNT          = 15;

	@Before
	public void setUp() throws Exception {
		super.beforeEach();
		networkService = (MockNetworkService) platformServices.getNetworkService();
		jsonUtilityService = platformServices.getJsonUtilityService();
		queryService = new PlacesQueryService(platformServices.getJsonUtilityService(), networkService);
	}

	@Test(expected = MissingPlatformServicesException.class)
	public void placesQueryService_when_noNetworkService() throws Exception {
		// test
		queryService = new PlacesQueryService(jsonUtilityService, null);
	}


	@Test(expected = MissingPlatformServicesException.class)
	public void placesQueryService_when_noJSONUtilityService() throws Exception {
		// test
		queryService = new PlacesQueryService(null, networkService);
	}


	@Test
	public void getNearByPlaces_when_EventDataWithNoLatitude() {
		// setup
		EventData eventData = new EventData();
		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, SAMPLE_LONGITUDE);
		eventData.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, SAMPLE_COUNT);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(eventData, validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("has the correct error message", "Ignoring the get nearby places event, unable to form query URL",
					 response.errorMessage);
		assertFalse("network request is not made", networkService.connectUrlWasCalled);
	}

	@Test
	public void getNearByPlaces_when_EventDataWithNoLongitude() {
		// setup
		EventData eventData = new EventData();
		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, SAMPLE_LATITUDE);
		eventData.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, SAMPLE_COUNT);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(eventData, validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.INVALID_LATLONG_ERROR,
					 response.resultStatus);
		assertEquals("has the correct error message", response.errorMessage,
					 "Ignoring the get nearby places event, unable to form query URL");
		assertFalse("network request is not made", networkService.connectUrlWasCalled);
	}

	@Test
	public void getNearByPlaces_happy_makesNetworkRequest() {
		// setup
		PlacesConfiguration configuration = validConfiguration();

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), configuration);

		// verify
		assertTrue("network request is made", networkService.connectUrlWasCalled);
		assertEquals("network request has the correct httpcommand", NetworkService.HttpCommand.GET,
					 networkService.connectUrlParametersCommand);
		assertEquals("network request has the correct URL",
					 "https://" + configuration.getEndpoint() + "" +
					 "/" + PlacesTestConstants.ServerKeys.PLACES_EDGE + "" +
					 "?latitude=" + SAMPLE_LATITUDE + "&longitude=" + SAMPLE_LONGITUDE + "&limit=" + SAMPLE_COUNT +
					 configuration.getLibrariesQueryString(),
					 networkService.connectUrlParametersUrl);
		assertNull("network request has the correct request property", networkService.connectUrlParametersRequestProperty);
		assertNull("network request has the correct connect payload", networkService.connectUrlParametersConnectPayload);
		assertEquals("network request has the correct read timeout", PlacesTestConstants.DEFAULT_NETWORK_TIMEOUT,
					 networkService.connectUrlParametersReadTimeout);
		assertEquals("network request has the correct connect timeout", PlacesTestConstants.DEFAULT_NETWORK_TIMEOUT,
					 networkService.connectUrlParametersConnectTimeout);
	}

	@Test
	public void getNearByPlaces_when_nullConnection() {
		// setup
		networkService.connectUrlReturnValue = null;

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.CONNECTIVITY_ERROR,
					 response.resultStatus);
		assertEquals("has the correct error message", "Unable to get nearby places, connection is null",
					 response.errorMessage);
	}

	@Test
	public void getNearByPlaces_when_HttpNot200OK() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection("", 400, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertEquals("the query service should have the correct result status", PlacesRequestError.CONNECTIVITY_ERROR,
					 response.resultStatus);
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("has the correct error message",
					 "Unable to get nearby places, connection failed with status 400, message messageText", response.errorMessage);
	}

	@Test
	public void getNearByPlaces_when_NullResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(null, 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.SERVER_RESPONSE_ERROR,
					 response.resultStatus);
		assertEquals("has the correct error message", "Unable to get nearby places, invalid json from server response",
					 response.errorMessage);
	}

	@Test
	public void getNearByPlaces_when_EmptyResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection("", 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.SERVER_RESPONSE_ERROR,
					 response.resultStatus);
		assertEquals("has the correct error message", "Unable to get nearby places, server response is empty",
					 response.errorMessage);
	}

	@Test
	public void getNearByPlaces_when_invalidJsonResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection("invalidJSON", 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.SERVER_RESPONSE_ERROR,
					 response.resultStatus);
		assertEquals("has the correct error message", "Unable to get nearby places, invalid json from server response",
					 response.errorMessage);
	}

	@Test
	public void getNearByPlaces_when_invalidJsonKey() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(responseWithInvalidKey(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertFalse("the query service should not succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.SERVER_RESPONSE_ERROR,
					 response.resultStatus);
		assertEquals("has the correct error message",
					 "Unable to get nearby places, Failed with exception: java.lang.NullPointerException",
					 response.errorMessage);
	}


	@Test
	public void getNearByPlaces_when_EmptyPOIsResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(emptyPOIResponse(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertTrue("the query service should succeed", response.isSuccess);
		assertEquals("the query service should have the correct result status", PlacesRequestError.OK, response.resultStatus);

		// verify nearbyPois
		assertNotNull(response.nearByPOIs);
		assertEquals(0, response.nearByPOIs.size());


		// verify containsUserpois
		assertNotNull(response.containsUserPOIs);
		assertEquals(0, response.containsUserPOIs.size());
	}


	@Test
	public void getNearByPlaces_when_InvalidLatitudeInResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(poiWithInValidLatitude(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertTrue("the query service should succeed", response.isSuccess);

		// verify nearbyPois
		assertNotNull(response.nearByPOIs);
		assertEquals(0, response.nearByPOIs.size());


		// verify containsUserpois
		assertNotNull(response.containsUserPOIs);
		assertEquals(0, response.containsUserPOIs.size());
	}

	@Test
	public void getNearByPlaces_when_InvalidLongitudeInResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(poiWithInvalidLongitude(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertTrue("the query service should succeed", response.isSuccess);

		// verify nearbyPois
		assertNotNull(response.nearByPOIs);
		assertEquals(0, response.nearByPOIs.size());


		// verify containsUserpois
		assertNotNull(response.containsUserPOIs);
		assertEquals(0, response.containsUserPOIs.size());
	}


	@Test
	public void getNearByPlaces_when_invalidPOIStructureInResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(poiWithInvalidStructure(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertTrue("the query service should succeed", response.isSuccess);

		// verify nearbyPois
		assertNotNull(response.nearByPOIs);
		assertEquals(0, response.nearByPOIs.size());


		// verify containsUserpois
		assertNotNull(response.containsUserPOIs);
		assertEquals(0, response.containsUserPOIs.size());
	}

	@Test
	public void getNearByPlaces_when_ValidResponse() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(validQueryResponse(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertTrue("the query service should succeed", response.isSuccess);

		// verify nearbyPois
		assertNotNull(response.nearByPOIs);

		assertEquals(1, response.nearByPOIs.size());
		assertEquals("f051420f-44cb-410b-ae23-9bf35ca70e76", response.nearByPOIs.get(0).getIdentifier());
		assertEquals("Marriott Marquis - Street Level", response.nearByPOIs.get(0).getName());
		assertEquals(40.7579918, response.nearByPOIs.get(0).getLatitude(), 0);
		assertEquals(-73.9856339, response.nearByPOIs.get(0).getLongitude(), 0);
		assertEquals(58, response.nearByPOIs.get(0).getRadius());
		assertEquals("libraryName", response.nearByPOIs.get(0).getLibrary());
		assertEquals(200, response.nearByPOIs.get(0).getWeight());
		assertNotNull(response.nearByPOIs.get(0).getMetadata());
		assertEquals(6, response.nearByPOIs.get(0).getMetadata().size());

		// verify containsUserpois
		assertNotNull(response.containsUserPOIs);
		assertEquals(1, response.containsUserPOIs.size());
		assertEquals("558cdf00-11ec-4abb-9c4e-17f937556377", response.containsUserPOIs.get(0).getIdentifier());
		assertEquals("Adobe NY Office", response.containsUserPOIs.get(0).getName());
		assertEquals(40.7580460, response.containsUserPOIs.get(0).getLatitude(), 0);
		assertEquals(-73.9848600, response.containsUserPOIs.get(0).getLongitude(), 0);
		assertEquals(20, response.containsUserPOIs.get(0).getRadius());
		assertEquals("libraryName", response.nearByPOIs.get(0).getLibrary());
		assertEquals(200, response.nearByPOIs.get(0).getWeight());

		assertNotNull(response.containsUserPOIs.get(0).getMetadata());
		assertEquals(7, response.containsUserPOIs.get(0).getMetadata().size());
	}


	@Test
	public void getNearByPlaces_when_queryResponseWithInvalidID() {
		// setup
		networkService.connectUrlReturnValue = new MockConnection(queryResponseWithInvalidPOIID(), 200, "messageText", null);

		// test
		PlacesQueryResponse response = queryService.getNearbyPlaces(validEventData(), validConfiguration());

		// verify
		assertTrue("the query service should succeed", response.isSuccess);

		// verify nearbyPois
		assertNotNull(response.nearByPOIs);
		assertEquals(1, response.nearByPOIs.size());

		// verify containsUserpois
		assertNotNull(response.containsUserPOIs);
		assertEquals(0, response.containsUserPOIs.size());
	}


	private PlacesConfiguration validConfiguration() {
		return new MockPlacesConfigurartion("&library=lib1&library=lib2", "endPoint", "path", true);
	}

	private PlacesConfiguration inValidConfiguration() {
		return new MockPlacesConfigurartion("&library=lib1&library=lib2", "endPoint", "path", false);
	}

	private EventData validEventData() {
		EventData eventData = new EventData();
		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LATITUDE, SAMPLE_LATITUDE);
		eventData.putDouble(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, SAMPLE_LONGITUDE);
		eventData.putInteger(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, SAMPLE_COUNT);
		return eventData;
	}

	private String validQueryResponse() {
		return "{\n" +
			   "  \"places\": {\n" +
			   "    \"userWithin\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"558cdf00-11ec-4abb-9c4e-17f937556377\",\n" +
			   "          \"Adobe NY Office\",\n" +
			   "          \"40.7580460\",\n" +
			   "          \"-73.9848600\",\n" +
			   "          20,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"Adobe\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1540 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Adobe\",\n" +
			   "          \"Color\": \"Blue\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ],\n" +
			   "    \"pois\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			   "          \"Marriott Marquis - Street Level\",\n" +
			   "          \"40.7579918\",\n" +
			   "          \"-73.9856339\",\n" +
			   "          58,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"LS\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1535 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Starbucks\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ]\n" +
			   "  },\n" +
			   "  \"input\": null\n" +
			   "}";
	}

	private String queryResponseWithInvalidPOIID() {
		return "{\n" +
			   "  \"places\": {\n" +
			   "    \"userWithin\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          20,\n" +
			   "          \"Adobe NY Office\",\n" +
			   "          \"40.7580460\",\n" +
			   "          \"-73.9848600\",\n" +
			   "          20,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"Adobe\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1540 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Adobe\",\n" +
			   "          \"Color\": \"Blue\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ],\n" +
			   "    \"pois\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			   "          \"Marriott Marquis - Street Level\",\n" +
			   "          \"40.7579918\",\n" +
			   "          \"-73.9856339\",\n" +
			   "          58,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"LS\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1535 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Starbucks\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ]\n" +
			   "  },\n" +
			   "  \"input\": null\n" +
			   "}";
	}

	private String emptyPOIResponse() {
		return "{\n" +
			   "  \"places\": {\n" +
			   "    \"userWithin\": [],\n" +
			   "    \"pois\": []\n" +
			   "  },\n" +
			   "  \"input\": null\n" +
			   "}";
	}

	private String poiWithInValidLatitude() {
		return "{\n" +
			   "  \"places\": {\n" +
			   "    \"userWithin\": [],\n" +
			   "    \"pois\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			   "          \"Marriott Marquis - Street Level\",\n" +
			   "          \"1440.7579918\",\n" +
			   "          \"-73.9856339\"" +
			   ",\n" +
			   "          58,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"LS\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1535 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Starbucks\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ]\n" +
			   "  },\n" +
			   "  \"input\": null\n" +
			   "}";
	}

	private String poiWithInvalidLongitude() {
		return "{\n" +
			   "  \"places\": {\n" +
			   "    \"userWithin\": [],\n" +
			   "    \"pois\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			   "          \"Marriott Marquis - Street Level\",\n" +
			   "          \"40.7579918\",\n" +
			   "          \"-773.9856339\",\n" +
			   "          58,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"LS\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1535 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Starbucks\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ]\n" +
			   "  },\n" +
			   "  \"input\": null\n" +
			   "}";
	}

	private String poiWithInvalidStructure() {
		return "{\n" +
			   "  \"places\": {\n" +
			   "    \"userWithin\": [],\n" +
			   "    \"pois\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			   "          \"Marriott Marquis - Street Level\"\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"LS\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1535 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Starbucks\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ]\n" +
			   "  }\n" +
			   "}";
	}

	private String responseWithInvalidKey() {
		return "{\n" +
			   "  \"invalidKey\": {\n" +
			   "    \"userWithin\": [],\n" +
			   "    \"pois\": [\n" +
			   "      {\n" +
			   "        \"p\": [\n" +
			   "          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			   "          \"Marriott Marquis - Street Level\",\n" +
			   "          \"140.7579918\",\n" +
			   "          \"-73.9856339\"" +
			   ",\n" +
			   "          58,\n" +
			   "           \"libraryName\",\n" +
			   "          200\n" +
			   "        ],\n" +
			   "        \"x\": {\n" +
			   "          \"country\": \"US\",\n" +
			   "          \"ownership\": \"LS\",\n" +
			   "          \"city\": \"New York\",\n" +
			   "          \"street\": \"1535 Broadway\",\n" +
			   "          \"state\": \"NY\",\n" +
			   "          \"brand\": \"Starbucks\"\n" +
			   "        }\n" +
			   "      }\n" +
			   "    ]\n" +
			   "  },\n" +
			   "  \"input\": null\n" +
			   "}";
	}

}