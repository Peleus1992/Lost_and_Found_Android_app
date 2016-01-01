package edu.gatech.wguo64.lostandfoundandroidapp.googlemaps;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class PlaceDetailsJSONParser {

    /**
     * Receives a JSONObject and returns a list
     */
    public static HashMap<String, String> parse(String data) {
        JSONObject jObject = null;
        JSONArray jArray = null;
        String formattedAddress = "";
        String lat = "";
        String lng = "";

        HashMap<String, String> hm = new HashMap<String, String>();

        try {
            jObject = new JSONObject(data);
            jArray = jObject.getJSONArray("results");

            if (jArray.length() > 0) {
                JSONObject jTemp = (JSONObject) jArray.get(0);
                formattedAddress = jTemp.getString("formatted_address");
                lat = jTemp.getJSONObject("geometry").getJSONObject("location").getString("lat");
                lng = jTemp.getJSONObject("geometry").getJSONObject("location").getString("lng");
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        hm.put("lat", lat);
        hm.put("lng", lng);
        hm.put("formatted_address", formattedAddress);

        return hm;
    }
}
