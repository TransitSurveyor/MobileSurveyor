package com.meyersj.mobilesurveyor.app.geocode;


import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class SolrQuery {

    private final String TAG = getClass().getCanonicalName();
    private String url;
    private OkHttpClient client;

    private HashMap<String, LocationResult> solrResults = new HashMap<>();

    public SolrQuery(String url) {
        this.url = url;
        client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
    }

    public HashMap<String, LocationResult> getSolrResults() {
        return solrResults;
    }

    public void solrSearch(String searchKey) {
        String url;
        try {
            url = this.url + URLEncoder.encode(searchKey, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to encode params: " + searchKey);
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            parseResponse(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, "solrGET IOException: " + e.toString());
        }
    }

    protected void parseResponse(String responseString) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject responseJSON = (JSONObject) parser.parse(responseString);
            JSONObject responseData = (JSONObject) responseJSON.get("response");
            JSONArray responseDocs = (JSONArray) responseData.get("docs");
            //clear active results and populate with updated results
            solrResults.clear();
            for(Object j: responseDocs) {
                JSONObject record = (JSONObject) j;
                LocationResult result = parseRecord(record);
                if (result != null) {
                    Log.d(TAG, result.getAddress());
                    solrResults.put(result.getAddress(), result);
                }
                else {
                    Log.e(TAG, "parseRecord not successful");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    protected LocationResult parseRecord(JSONObject record) {
        LocationResult result = new LocationResult();
        if (record.containsKey("score")) {
            result.setScore(record.get("score").toString());
        }
        if(record.containsKey("lat") && record.containsKey("lon")) {
            result.setLatLng(record.get("lat").toString(),record.get("lon").toString());
        }
        if(record.containsKey("city") && record.containsKey("name")){
            String name = record.get("name").toString();
            String city = record.get("city").toString();
            if (!city.equals("") && !name.equals("")) {
                result.setAddress(name + ", " + city);
            }
            else if (!name.equals("")) {
                result.setAddress(name);
            }
            else {
                result.setAddress(null);
            }
        }
        else {
            result.setAddress(null);
        }
        if (!result.isValid()) {
            result = null;
        }
        return result;
    }
}