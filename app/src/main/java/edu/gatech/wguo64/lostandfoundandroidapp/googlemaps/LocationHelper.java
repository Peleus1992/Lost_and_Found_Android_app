package edu.gatech.wguo64.lostandfoundandroidapp.googlemaps;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by guoweidong on 11/3/15.
 */
public class LocationHelper {
    private static final String URL_GOOGLE_MAP_PLACE_AUTOCOMPLETE = "https://maps.googleapis.com/maps/api/place/autocomplete/";
    private static final String URL_GOOGLE_MAP_GEOCODE = "https://maps.googleapis.com/maps/api/geocode/";
    private static final String URL_OUTPUT_FORMAT = "json";
    // This key should be different when application changes
    private static final String GOOGLE_MAP_BROWSER_KEY = "AIzaSyDC3r_jmFqSdDTRlCD9c5k96kQ_d35TwDE";

    /**
     * This method must not be run in UI thread
     *
     * @param query
     * @return
     */
    public static List<HashMap<String, String>> getAutoCompletePlaces(String query) {

        String url = getAutoCompleteURL(query);
        String data = downloadFromURL(url);
        List<HashMap<String, String>> res = AutoCompleteJSONParser.parse(data);
        return res;
    }

    /**
     * This method must not be run in UI thread
     *
     * @param query
     * @return
     */
    public static HashMap<String, String> getPlaceDetails(String query) {
        String url = getPlaceDetailsURL(query);
        String data = downloadFromURL(url);
        // Columns can be id, reference, description
        HashMap<String, String> res = PlaceDetailsJSONParser.parse(data);
        return res;
    }

    public static String getAddress(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.US);
        String address = "";
        try {
            List<Address> list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(list.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for(int i = 0; i < list.get(0).getMaxAddressLineIndex(); i++) {
                    sb.append(list.get(0).getAddressLine(i));
                }
                address = sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    private static String getAutoCompleteURL(String query) {
        try {
            query = "input=" + URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // Sensor enabled
        String sensor = "sensor=false";


        // place type to be searched
        String types = "types=geocode";

        String key = "key=" + GOOGLE_MAP_BROWSER_KEY;

        // Building the parameters to the web service
        String parameters = query + "&" + types + "&" + sensor + "&" + key;


        // Building the url to the web service   "http://maps.google.com/maps/api/geocode/"
        String url = URL_GOOGLE_MAP_PLACE_AUTOCOMPLETE + URL_OUTPUT_FORMAT + "?" + parameters;
//        Log.i("myinfo", url);
        return url;
    }

    private static String getPlaceDetailsURL(String query) {
        try {
            query = "address=" + URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = query + "&" + sensor;


        // Building the url to the web service   "http://maps.google.com/maps/api/geocode/"
        String url = URL_GOOGLE_MAP_GEOCODE + URL_OUTPUT_FORMAT + "?" + parameters;
//        Log.i("myinfo", url);
        return url;
    }

    /**
     * A method to download json data from url
     */
    private static String downloadFromURL(String strUrl) {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                iStream.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }


}
