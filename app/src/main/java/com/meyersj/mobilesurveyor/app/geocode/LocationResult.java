package com.meyersj.mobilesurveyor.app.geocode;

import com.mapbox.mapboxsdk.geometry.LatLng;


public class LocationResult {

    public String address;
    public LatLng latLng;
    public Double score;

    public boolean isValid() {
        return (address != null && latLng != null && score != null);
    }

    public void setLatLng(Double latitude, Double longitude) {
        this.latLng = new LatLng(latitude, longitude);
    }
}
