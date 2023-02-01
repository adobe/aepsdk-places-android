package com.adobe.marketing.mobile.places;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

public class PlacesUtilTests {

    @Test
    public void test_convertToStringMap_when_JSONValuesAreAllString() throws JSONException {
        // setup
        JSONObject jsonObject = new JSONObject(metaDataAllString);

        // test
        Map<String, String> metaDataMap = PlacesUtil.convertPOIMetadataToStringMap(jsonObject);

        // verify
        assertEquals("Android Avenue" ,metaDataMap.get("address"));
        assertEquals("22333" ,metaDataMap.get("zipcode"));
        assertEquals("CA" ,metaDataMap.get("state"));
        assertEquals("false" ,metaDataMap.get("isHeadquarters"));
        assertEquals("US" ,metaDataMap.get("country"));
    }

    @Test
    public void test_convertToStringMap_when_emptyJSONObject() {
        // test
        Map<String, String> metaDataMap = PlacesUtil.convertPOIMetadataToStringMap(new JSONObject());

        // verify
        assertEquals(0 ,metaDataMap.size());
    }

    @Test
    public void test_convertToStringMap_when_JSONValuesDifferentTypes() throws JSONException {
        // setup
        JSONObject jsonObject = new JSONObject(metadataAllTypes);

        // test
        Map<String, String> metaDataMap = PlacesUtil.convertPOIMetadataToStringMap(jsonObject);

        // verify
        assertEquals("Android Avenue" ,metaDataMap.get("address"));
        assertEquals("22333" ,metaDataMap.get("zipcode"));
        assertEquals("CA" ,metaDataMap.get("state"));
        assertEquals("false" ,metaDataMap.get("isHeadquarters"));
        assertEquals("US" ,metaDataMap.get("country"));
        assertEquals("777888999000" ,metaDataMap.get("area"));
        assertFalse(metaDataMap.containsKey("map"));
        assertFalse(metaDataMap.containsKey("array"));
        assertEquals("" ,metaDataMap.get("emptyKey"));
        assertEquals("null" ,metaDataMap.get("nullKey"));
    }

    private static String metaDataAllString = "{\n" +
            "  \"address\": \"Android Avenue\",\n" +
            "  \"zipcode\": \"22333\",\n" +
            "  \"state\": \"CA\",\n" +
            "  \"isHeadquarters\": \"false\",\n" +
            "  \"country\": \"US\"\n" +
            "}";

    private static String metadataAllTypes = "{\n" +
            "  \"address\": \"Android Avenue\",\n" +
            "  \"zipcode\": 22333,\n" +
            "  \"distanceFromStarbucks\": 22.333,\n" +
            "  \"state\": \"CA\",\n" +
            "  \"isHeadquarters\": false,\n" +
            "  \"country\": \"US\",\n" +
            "  \"map\": {\"keylevel1\": \"value\", \"keyLevel2\" : {\"key\" : \"value\"}},\n" +
            "  \"array\" : [\"one\", \"two\", [\"one\",\"two\"], {\"key\" : \"value\"}],\n" +
            "  \"area\": 777888999000,\n" +
            "  \"emptyKey\": \"\",\n" +
            "  \"nullKey\": null\n" +
            "}";
}
