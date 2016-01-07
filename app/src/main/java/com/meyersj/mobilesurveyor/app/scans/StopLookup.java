package com.meyersj.mobilesurveyor.app.scans;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.meyersj.mobilesurveyor.app.util.Args;
import com.meyersj.mobilesurveyor.app.util.Cons;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class StopLookup {

    private final String TAG = "StopLookup";
    private final String EOL_MSG = "SWITCH DIRECTIONS AT END OF LINE";
    private Context context;
    private String url;
    private String line;
    private String dir;
    private TextView stopText;
    private TextView eol;
    private HttpClient client;

    public StopLookup(Context context, TextView stopText, TextView eol, String url, String line, String dir) {
        this.context = context;
        this.stopText = stopText;
        this.eol = eol;
        this.url = url;
        this.line = line;
        this.dir = dir;


        this.client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
        httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    }

    public void findStop(String lat, String lon) {
        StopLookupTask task = new StopLookupTask();
        task.execute(buildParams(lat, lon));
    }

    private Response query(String[] params) {
        Integer code = 0;
        String responseString = null;

        HttpPost post = new HttpPost(params[0]);
        Log.d(TAG, params[0]);
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Args.StopLookup.RTE, params[1]));
        postParam.add(new BasicNameValuePair(Args.StopLookup.DIR, params[2]));
        postParam.add(new BasicNameValuePair(Args.StopLookup.LAT, params[3]));
        postParam.add(new BasicNameValuePair(Args.StopLookup.LON, params[4]));

        try {
            //Log.d(TAG, "execute");
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);

            HttpEntity entityR = response.getEntity();
            responseString = EntityUtils.toString(entityR);
            Log.d(TAG, responseString);
            //Log.d(TAG, "End");

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException: " + e.toString());
            code = 1;
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
            code = 2;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            code = 3;
        }
        return new Response(code, responseString);
    }

    private String buildMessage(String message) {
        return Cons.NEAR_STOP + message;
    }

    private void updateDisplay(Response response) {
        String message;
        switch(response.code) {
            case 0:
                message = buildMessage(response.stopName);
                break;
            case 1:
                message = buildMessage("UnsupportedEncodingException");
                break;
            case 2:
                message = buildMessage("ClientProtocolException");
                break;
            case 3:
                message = buildMessage("IOException");
                break;
            default:
                message = buildMessage("Error");
        }
        stopText.setText(message);
        this.eol.setText(response.eol);
    }

    private String[] buildParams(String lat, String lon) {
        String[] params = new String[5];
        params[0] = this.url;
        params[1] = line;
        params[2] = dir;
        params[3] = lat;
        params[4] = lon;
        return params;
    }

    public class Response {
        public Integer code;
        public String response;
        public String stopName;
        public String eol = "";

        public Response(Integer code, String response) {
            this.code = code;
            this.response = response;
            this.parseResponse(response);
        }

        private void parseResponse(String response) {
            if (response == null) return;
            JSONParser parser = new JSONParser();

            try{
                Object obj = parser.parse(response);
                JSONObject results = (JSONObject) obj;
                String error = results.get("error").toString();
                this.stopName = !error.equals("true") ? results.get("stop_name").toString(): "error finding near stop";
                if(!error.equals("true") && Integer.valueOf(results.get("stop_seq_rem").toString()) <= 3) {
                    this.eol = EOL_MSG;
                }
            } catch(ParseException pe){
                Log.e(TAG, pe.toString());
            }
        }
    }

    class StopLookupTask extends AsyncTask<String[], Void, Response> {

        @Override
        protected Response doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, params[1]);
            return query(params);
        }
        @Override
        protected void onPostExecute(Response response) {
            if(response != null) {
                updateDisplay(response);
            }
        }


    }
}
