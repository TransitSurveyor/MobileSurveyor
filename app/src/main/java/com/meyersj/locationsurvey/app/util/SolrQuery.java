package com.meyersj.locationsurvey.app.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by meyersj on 7/11/2014.
 */
public class SolrQuery {

    private final String TAG = "SolrServer";
    private final String baseParams = "/select?start=0&wt=json&qt=dismax&rows=5&q=";
    private String url = null;

    private HashMap<String, LocationResult> solrResults = new HashMap<String, LocationResult>();

    public SolrQuery(String url) {
        this.url = url + baseParams;
    }

    public HashMap<String, LocationResult> getSolrResults() {
        return solrResults;
    }


    protected void solrLookup(String input) {

        //String url = "http://maps10.trimet.org/solr/select?start=0&wt=json&qt=dismax&rows=5&q=" + input

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String url;

        try {
            url = this.url + URLEncoder.encode(input, "UTF-8");

            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                parseResponse(out.toString());
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        }
        catch (UnsupportedEncodingException e) {
                Log.e(TAG, "failed to encode params: " + input);

        } catch (ClientProtocolException e) {
            Log.d(TAG, "ClientProtocolException");
            Log.d(TAG, e.toString());

        } catch (IOException e) {
            Log.d(TAG, "IOException");
            Log.d(TAG, e.toString());

        }

    }

    protected void parseResponse(String responseString) {

        JSONParser parser = new JSONParser();
        try {
            JSONObject responseJSON = (JSONObject) parser.parse(responseString);

            //String responseHeader = responseJSON.get("responseHeader").toString();
            //Log.d(TAG, responseHeader);
            //String responseData = responseJSON.get("response").toString();
            //Log.d(TAG, responseData);

            JSONObject responseData = (JSONObject) responseJSON.get("response");
            JSONArray responseDocs = (JSONArray) responseData.get("docs");
            //Log.d(TAG, responseDocs.toString());

            //clear current results and populate with new results
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
            //String score = record.get("score").toString();
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
