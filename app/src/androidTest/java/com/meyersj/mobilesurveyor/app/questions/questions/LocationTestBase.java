package com.meyersj.mobilesurveyor.app.questions.questions;

import android.os.Bundle;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.mobilesurveyor.app.util.Args;

import junit.framework.Assert;


public class LocationTestBase extends QuestionActivityTestBase {

    public void validateLatLng(String desc, LatLng latLng) {
        Assert.assertNotNull(desc + ": No location selected", latLng);
        Assert.assertTrue(desc + ": Latitude should not equal 0", latLng.getLatitude() != 0);
        Assert.assertTrue(desc + ": Longitude should not equal 0", latLng.getLongitude() != 0);
    }

    public void validateExitCoordinates(LatLng loc, Bundle extras) {
        String lat = String.valueOf(loc.getLatitude());
        String lon = String.valueOf(loc.getLongitude());
        Assert.assertNotNull("Exit Bundle with coordinates is null", extras);
        Assert.assertEquals("Exit Latitude does not match", lat, extras.getString(Args.LAT));
        Assert.assertEquals("Exit Longitude does not match", lon, extras.getString(Args.LON));
    }
}
