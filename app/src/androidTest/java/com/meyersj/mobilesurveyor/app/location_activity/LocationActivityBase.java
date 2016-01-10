package com.meyersj.mobilesurveyor.app.location_activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.mobilesurveyor.app.Fields;
import com.meyersj.mobilesurveyor.app.LocationActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;

public abstract class LocationActivityBase {

    public final String TAG = getClass().getCanonicalName();
    public final String ACTION = LocationActivity.ODK_LOCATION_ACTION;

    public LocationActivity activity;

    @Rule
    public ActivityTestRule<LocationActivity> activityTestRule = new ActivityTestRule<>(LocationActivity.class);

    @Before
    public void setUp() {
        Intent intent = new Intent(ACTION);
        activity = activityTestRule.launchActivity(intent);
    }

    public void validateLatLng(LatLng latLng) {
        Assert.assertNotNull("No location selected", latLng);
        Assert.assertTrue("Latitude should not equal 0", latLng.getLatitude() != 0);
        Assert.assertTrue("Longitude should not equal 0", latLng.getLongitude() != 0);
    }

    public void validateExitBundle(LatLng loc, Bundle extras) {
        String lat = String.valueOf(loc.getLatitude());
        String lon = String.valueOf(loc.getLongitude());
        Assert.assertNotNull("Exit Bundle with coordinates is null", extras);
        Assert.assertEquals("Exit Latitude does not match", lat, extras.getString(Fields.LAT));
        Assert.assertEquals("Exit Longitude does not match", lon, extras.getString(Fields.LON));
    }
}
