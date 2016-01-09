package com.meyersj.mobilesurveyor.app.locations;


import android.util.Log;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;


public class SolrQuery {

    private final String TAG = "SolrServer";
    private final String baseParams = "/select?start=0&wt=json&qt=dismax&rows=5&q=";
    private String url = null;
    private HttpClient httpclient;

    private HashMap<String, LocationResult> solrResults = new HashMap<String, LocationResult>();

    public SolrQuery(String url) {
        this.url = url + baseParams;
        this.httpclient = new DefaultHttpClient();
        HttpParams httpParams = this.httpclient.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
    }

    public HashMap<String, LocationResult> getSolrResults() {
        return solrResults;
    }

    protected void solrLookup(String input) {
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
            JSONObject responseData = (JSONObject) responseJSON.get("response");
            JSONArray responseDocs = (JSONArray) responseData.get("docs");

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
