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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.adobe.marketing.mobile.services.HttpConnecting;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.NetworkCallback;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.Networking;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlacesQueryServiceTests {

	private PlacesQueryService queryService;

	@Mock
	private Networking networking;

	@Mock
	private HttpConnecting connecting;

	@Mock
	private PlacesConfiguration configuration;

	private static final double SAMPLE_LATITUDE = 31.44234;
	private static final double SAMPLE_LONGITUDE = -121.06527;
	private static final int SAMPLE_COUNT = 15;

	@Before
	public void setUp() {
		reset(networking);
		queryService = new PlacesQueryService(networking);
	}

	@Test
	public void getNearByPlaces_when_EventDataWithNoLatitude() {
		// setup
		HashMap<String, Object> eventData = new HashMap<>();
		eventData.put(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, SAMPLE_LONGITUDE);
		eventData.put(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, SAMPLE_COUNT);

		// test
		queryService.getNearbyPlaces(
			eventData,
			validConfiguration(),
			response -> {
				// since we are mocking network layer, the callback executes synchronously and the assertions are executed.
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"has the correct error message",
					"Ignoring the get nearby places event, unable to form query URL",
					response.errorMessage
				);
			}
		);

		// verify
		verifyNoInteractions(networking);
	}

	@Test
	public void getNearByPlaces_when_EventDataWithNoLongitude() {
		// setup
		HashMap<String, Object> eventData = new HashMap<>();
		eventData.put(PlacesTestConstants.EventDataKeys.Places.LATITUDE, SAMPLE_LATITUDE);
		eventData.put(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, SAMPLE_COUNT);

		// test
		queryService.getNearbyPlaces(
			eventData,
			validConfiguration(),
			response -> {
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"has the correct error message",
					"Ignoring the get nearby places event, unable to form query URL",
					response.errorMessage
				);
			}
		);

		// verify
		verifyNoInteractions(networking);
	}

	@Test
	public void getNearByPlaces_happy_makesNetworkRequest() {
		// setup
		PlacesConfiguration configuration = validConfiguration();
		ArgumentCaptor<NetworkRequest> networkResponseCapture = ArgumentCaptor.forClass(NetworkRequest.class);

		// test
		queryService.getNearbyPlaces(validEventData(), validConfiguration(), response -> {});

		// verify
		verify(networking, times(1)).connectAsync(networkResponseCapture.capture(), any());
		assertEquals(
			"network request has the correct httpcommand",
			HttpMethod.GET,
			networkResponseCapture.getValue().getMethod()
		);
		assertEquals(
			"network request has the correct URL",
			"https://" +
			configuration.getEndpoint() +
			"" +
			"/" +
			PlacesTestConstants.ServerKeys.PLACES_EDGE +
			"" +
			"?latitude=" +
			SAMPLE_LATITUDE +
			"&longitude=" +
			SAMPLE_LONGITUDE +
			"&limit=" +
			SAMPLE_COUNT +
			configuration.getLibrariesQueryString(),
			networkResponseCapture.getValue().getUrl()
		);
		assertNull("network request has the correct request property", networkResponseCapture.getValue().getHeaders());
		assertNull("network request has the correct connect payload", networkResponseCapture.getValue().getBody());
		assertEquals(
			"network request has the correct read timeout",
			PlacesTestConstants.DEFAULT_NETWORK_TIMEOUT,
			networkResponseCapture.getValue().getReadTimeout()
		);
		assertEquals(
			"network request has the correct connect timeout",
			PlacesTestConstants.DEFAULT_NETWORK_TIMEOUT,
			networkResponseCapture.getValue().getConnectTimeout()
		);
	}

	@Test
	public void getNearByPlaces_when_nullConnection() {
		// setup
		doAnswer(invocation -> {
				((NetworkCallback) invocation.getArguments()[1]).call(null);
				return null;
			})
			.when(networking)
			.connectAsync(any(), any());

		// test and verify
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.CONNECTIVITY_ERROR,
					response.resultStatus
				);
				assertEquals(
					"has the correct error message",
					"Unable to get nearby places, connection is null",
					response.errorMessage
				);
			}
		);
		verify(networking).connectAsync(any(), any());
	}

	@Test
	public void getNearByPlaces_when_HttpNot200OK() {
		// setup
		when(connecting.getResponseCode()).thenReturn(400);
		when(connecting.getResponseMessage()).thenReturn("<message text>");
		doAnswer(invocation -> {
				((NetworkCallback) invocation.getArguments()[1]).call(connecting);
				return null;
			})
			.when(networking)
			.connectAsync(any(), any());

		// test and verify
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.CONNECTIVITY_ERROR,
					response.resultStatus
				);
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"has the correct error message",
					"Unable to get nearby places, connection failed with status 400, message <message text>",
					response.errorMessage
				);
			}
		);
	}

	@Test
	public void getNearByPlaces_when_NullResponse() {
		// setup
		when(connecting.getResponseCode()).thenReturn(200);
		when(connecting.getInputStream()).thenReturn(null);
		doAnswer(invocation -> {
				((NetworkCallback) invocation.getArguments()[1]).call(connecting);
				return null;
			})
			.when(networking)
			.connectAsync(any(), any());

		// test and verify
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.SERVER_RESPONSE_ERROR,
					response.resultStatus
				);
				assertEquals(
					"has the correct error message",
					"Unable to get nearby places, server response is empty",
					response.errorMessage
				);
			}
		);
	}

	@Test
	public void getNearByPlaces_when_EmptyResponse() {
		// setup
		mockNetworkResponse(200, "");

		// test and verify
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.SERVER_RESPONSE_ERROR,
					response.resultStatus
				);
				assertEquals(
					"has the correct error message",
					"Unable to get nearby places, server response is empty",
					response.errorMessage
				);
			}
		);
	}

	@Test
	public void getNearByPlaces_when_invalidJsonResponse() {
		// setup
		mockNetworkResponse(200, "invalidJSON");

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.SERVER_RESPONSE_ERROR,
					response.resultStatus
				);
				assertTrue(
					"has correct error message",
					response.errorMessage.contains(
						"Unable to get nearby places, Failed with exception: org.json.JSONException:"
					)
				);
			}
		);
	}

	@Test
	public void getNearByPlaces_when_invalidJsonKey() {
		// setup
		when(connecting.getResponseCode()).thenReturn(200);
		when(connecting.getInputStream())
			.thenReturn(new ByteArrayInputStream(responseWithInvalidKey().getBytes(StandardCharsets.UTF_8)));
		doAnswer(invocation -> {
				((NetworkCallback) invocation.getArguments()[1]).call(connecting);
				return null;
			})
			.when(networking)
			.connectAsync(any(), any());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertFalse("the query service should not succeed", response.isSuccess);
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.SERVER_RESPONSE_ERROR,
					response.resultStatus
				);
				assertTrue(
					"has correct error message",
					response.errorMessage.contains(
						"Unable to get nearby places, Failed with exception: org.json.JSONException:"
					)
				);
			}
		);
	}

	@Test
	public void getNearByPlaces_when_EmptyPOIsResponse() {
		// setup
		mockNetworkResponse(200, emptyPOIResponse());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertTrue("the query service should succeed", response.isSuccess);
				assertEquals(
					"the query service should have the correct result status",
					PlacesRequestError.OK,
					response.resultStatus
				);

				// verify nearbyPois
				assertNotNull(response.nearByPOIs);
				assertEquals(0, response.nearByPOIs.size());

				// verify containsUserpois
				assertNotNull(response.containsUserPOIs);
				assertEquals(0, response.containsUserPOIs.size());
			}
		);
	}

	@Test
	public void getNearByPlaces_when_InvalidLatitudeInResponse() {
		// setup
		mockNetworkResponse(200, poiWithInValidLatitude());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertTrue("the query service should succeed", response.isSuccess);

				// verify nearbyPois
				assertNotNull(response.nearByPOIs);
				assertEquals(0, response.nearByPOIs.size());

				// verify containsUserpois
				assertNotNull(response.containsUserPOIs);
				assertEquals(0, response.containsUserPOIs.size());
			}
		);
	}

	@Test
	public void getNearByPlaces_when_InvalidLongitudeInResponse() {
		// setup
		mockNetworkResponse(200, poiWithInvalidLongitude());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertTrue("the query service should succeed", response.isSuccess);

				// verify nearbyPois
				assertNotNull(response.nearByPOIs);
				assertEquals(0, response.nearByPOIs.size());

				// verify containsUserpois
				assertNotNull(response.containsUserPOIs);
				assertEquals(0, response.containsUserPOIs.size());
			}
		);
	}

	@Test
	public void getNearByPlaces_when_invalidPOIStructureInResponse() {
		// setup
		mockNetworkResponse(200, poiWithInvalidStructure());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertTrue("the query service should succeed", response.isSuccess);

				// verify nearbyPois
				assertNotNull(response.nearByPOIs);
				assertEquals(0, response.nearByPOIs.size());

				// verify containsUserpois
				assertNotNull(response.containsUserPOIs);
				assertEquals(0, response.containsUserPOIs.size());
			}
		);
	}

	@Test
	public void getNearByPlaces_when_ValidResponse() {
		// setup
		mockNetworkResponse(200, validQueryResponse());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
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
		);
	}

	@Test
	public void getNearByPlaces_when_queryResponseWithInvalidRadius() {
		// setup
		mockNetworkResponse(200, poiWithInValidRadius());

		// test
		queryService.getNearbyPlaces(
			validEventData(),
			validConfiguration(),
			response -> {
				// verify
				assertTrue("the query service should succeed", response.isSuccess);

				// verify nearbyPois
				assertNotNull(response.nearByPOIs);
				assertEquals(0, response.nearByPOIs.size());

				// verify containsUserpois
				assertNotNull(response.containsUserPOIs);
				assertEquals(0, response.containsUserPOIs.size());
			}
		);
	}

	private PlacesConfiguration validConfiguration() {
		when(configuration.getLibrariesQueryString()).thenReturn("&library=lib1&library=lib2");
		when(configuration.getEndpoint()).thenReturn("endPoint");
		when(configuration.isValid()).thenReturn(true);
		return configuration;
	}

	private PlacesConfiguration invalidConfiguration() {
		when(configuration.isValid()).thenReturn(false);
		return configuration;
	}

	private HashMap<String, Object> validEventData() {
		HashMap<String, Object> eventData = new HashMap<>();
		eventData.put(PlacesTestConstants.EventDataKeys.Places.LATITUDE, SAMPLE_LATITUDE);
		eventData.put(PlacesTestConstants.EventDataKeys.Places.LONGITUDE, SAMPLE_LONGITUDE);
		eventData.put(PlacesTestConstants.EventDataKeys.Places.PLACES_COUNT, SAMPLE_COUNT);
		return eventData;
	}

	private void mockNetworkResponse(final int responseCode, final String response) {
		when(connecting.getResponseCode()).thenReturn(responseCode);
		when(connecting.getInputStream())
			.thenReturn(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
		doAnswer(invocation -> {
				((NetworkCallback) invocation.getArguments()[1]).call(connecting);
				return null;
			})
			.when(networking)
			.connectAsync(any(), any());
	}

	private String validQueryResponse() {
		return (
			"{\n" +
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
			"}"
		);
	}

	private String poiWithInValidRadius() {
		return (
			"{\n" +
			"  \"places\": {\n" +
			"    \"userWithin\": [],\n" +
			"    \"pois\": [\n" +
			"      {\n" +
			"        \"p\": [\n" +
			"          \"f051420f-44cb-410b-ae23-9bf35ca70e76\",\n" +
			"          \"Marriott Marquis - Street Level\",\n" +
			"          \"144.7579918\",\n" +
			"          \"-73.9856339\",\n" +
			"          \"invalidRadius\",\n" +
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
			"}"
		);
	}

	private String emptyPOIResponse() {
		return (
			"{\n" +
			"  \"places\": {\n" +
			"    \"userWithin\": [],\n" +
			"    \"pois\": []\n" +
			"  },\n" +
			"  \"input\": null\n" +
			"}"
		);
	}

	private String poiWithInValidLatitude() {
		return (
			"{\n" +
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
			"}"
		);
	}

	private String poiWithInvalidLongitude() {
		return (
			"{\n" +
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
			"}"
		);
	}

	private String poiWithInvalidStructure() {
		return (
			"{\n" +
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
			"}"
		);
	}

	private String responseWithInvalidKey() {
		return (
			"{\n" +
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
			"}"
		);
	}
}
