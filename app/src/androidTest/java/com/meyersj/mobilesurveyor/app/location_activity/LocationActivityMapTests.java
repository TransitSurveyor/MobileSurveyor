package com.meyersj.mobilesurveyor.app.location_activity;

import android.os.Bundle;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.mobilesurveyor.app.R;

import junit.framework.Assert;

import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


public class LocationActivityMapTests extends LocationActivityBase {

    @Test
    public void actionSetTest() {
        Assert.assertEquals("Invalid action", ACTION, activity.getIntent().getAction());
    }

    @Test
    public void basicLongPressTest() {
        LatLng selectedLocation = selectLocation(0);
        Bundle exitBundle = activity.buildBundle(activity.getSelectedLatLng());
        validateExitBundle(selectedLocation, exitBundle);
    }

    @Test
    public void iterativeLongPressesTest() {
        LatLng loc1 = selectLocation(0);
        LatLng loc2 = selectLocation(50);
        LatLng loc3 = selectLocation(100);
        Assert.assertTrue("Latitude error 1", loc1.getLatitude() > loc2.getLatitude());
        Assert.assertTrue("Latitude error 2", loc2.getLatitude() > loc3.getLatitude());
        Assert.assertTrue("Longitude error 1", loc1.getLongitude() < loc2.getLongitude());
        Assert.assertTrue("Longitude error 2", loc2.getLongitude() < loc3.getLongitude());
        Bundle exitBundle = activity.buildBundle(activity.getSelectedLatLng());
        validateExitBundle(loc3, exitBundle);
    }

    public ViewAction locationClick(final float offsetX, final float offsetY){
        return new GeneralClickAction(
                Tap.LONG,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        final float screenX = screenPos[0] + (view.getWidth() / 2) + offsetX;
                        final float screenY = screenPos[1] + (view.getHeight() / 2) + offsetY;
                        float[] coordinates = {screenX, screenY};
                        return coordinates;
                    }
                },
                Press.FINGER);
    }

    public LatLng selectLocation(float offset) {
        onView(withId(R.id.mapview)).perform(locationClick(offset, offset));
        LatLng selectedLocation = activity.getSelectedLatLng();
        validateLatLng(selectedLocation);
        return selectedLocation;
    }
}