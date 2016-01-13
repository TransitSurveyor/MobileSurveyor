package com.meyersj.mobilesurveyor.app.stops;

import android.content.Context;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class GeoJSON {

    public static String readFile(Context context, String filename) throws IOException {
        InputStream inputStream = context.getAssets().open(filename);
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        inputStream.close();
        return sb.toString();
    }

    public static ArrayList<Stop> parseStops(String stopsGeoJSON) {
        if (stopsGeoJSON == null || stopsGeoJSON.isEmpty()) {
            return null;
        }

        JSONObject json = (JSONObject) JSONValue.parse(stopsGeoJSON);
        JSONArray features = (JSONArray) json.get("features");
        ArrayList<Stop> stops = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = (JSONObject) features.get(i);

            // properties
            JSONObject properties = (JSONObject) feature.get("properties");
            JSONObject geometry = (JSONObject) feature.get("geometry");
            stops.add(parseStop(properties, geometry));
        }
        return stops.size() != 0 ? stops : null;
    }

    private static Stop parseStop(JSONObject properties, JSONObject geometry) {
        String type = (String) geometry.get("type");
        if (!type.equals("Point")) {
            return null;
        }
        String stopName = (String) properties.get("stop_name");
        Long stopSequence = (Long) properties.get("stop_seq");
        Long stopId = (Long) properties.get("stop_id");
        JSONArray coordinates = (JSONArray) geometry.get("coordinates");
        Double longitude = (Double) coordinates.get(0);
        Double latitude = (Double) coordinates.get(1);
        LatLng latLng = new LatLng(latitude, longitude);
        Stop stop = new Stop();
        stop.stopName = stopName;
        stop.stopId = String.valueOf(stopId);
        stop.stopSequence = stopSequence.intValue();
        stop.latLng = latLng;
        return stop;
    }
}
