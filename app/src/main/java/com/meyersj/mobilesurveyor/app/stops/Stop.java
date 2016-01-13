package com.meyersj.mobilesurveyor.app.stops;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Comparator;


public class Stop {

    public String stopName;
    public String stopId;
    public Integer stopSequence;
    public LatLng latLng;

    public Stop() {}

    public Stop(Marker marker) {
        parseMarker(marker);
    }

    public static class StopComparator implements Comparator<Stop> {
        @Override
        public int compare(Stop stop1, Stop stop2) {
            return stop1.stopSequence.compareTo(stop2.stopSequence);
        }
    }

    public MarkerOptions getMarkerOptions() {
        return new MarkerOptions()
                .position(latLng)
                .title(stopName)
                .snippet(String.valueOf(stopSequence) + "|" + stopId);
    }

    public void parseMarker(Marker marker) {
        stopName = marker.getTitle();
        String[] fields = marker.getSnippet().split("|");
        if (fields.length == 2) {
            stopSequence = Integer.parseInt(fields[0]);
            stopId = fields[1];
        }
        latLng = marker.getPosition();
    }
}
