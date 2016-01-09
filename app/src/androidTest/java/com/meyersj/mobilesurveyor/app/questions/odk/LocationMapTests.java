package com.meyersj.mobilesurveyor.app.questions.odk;

import android.os.Bundle;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.view.View;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.odk.LocationFragment;
import com.meyersj.mobilesurveyor.app.odk.QuestionActivity;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


public class LocationMapTests extends LocationTestBase {

    @Before
    public void setUp() {
        action = QuestionActivity.ODK_LOCATION_ACTION;
    }

    @Test
    public void exitCoordinatesTest() {
        LocationFragment fragment = (LocationFragment) initializeActivity();
        LatLng loc = selectLocation("Exit Location", fragment, 0);
        Bundle extras = fragment.buildExitIntent().getExtras();
        validateExitCoordinates(loc, extras);
    }

    @Test
    public void longPressLocationTest() {
        // upper left corner of view has coordinates (0, 0)
        // increasing offset will cause Latitude to decrease and Longitude to increase
        LocationFragment fragment = (LocationFragment) initializeActivity();
        LatLng loc1 = selectLocation("Location #1", fragment, 0);
        LatLng loc2 = selectLocation("Location #2", fragment, 25);
        LatLng loc3 = selectLocation("Location #3", fragment, 50);
        Assert.assertTrue("Latitude Error 1,2", loc1.getLatitude() > loc2.getLatitude());
        Assert.assertTrue("Latitude Error 2,3", loc2.getLatitude() > loc3.getLatitude());
        Assert.assertTrue("Longitude Error 1,2", loc1.getLongitude() < loc2.getLongitude());
        Assert.assertTrue("Longitude Error 2,3", loc2.getLongitude() < loc3.getLongitude());
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

    public LatLng selectLocation(String desc, LocationFragment fragment, float offset) {
        onView(withId(R.id.mapview)).perform(locationClick(offset, offset));
        LatLng latLng = fragment.getSelectedLatLng();
        validateLatLng(desc, latLng);
        return latLng;
    }
}
