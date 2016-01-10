package com.meyersj.mobilesurveyor.app.location_activity;

import android.os.Bundle;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.espresso.IdlingResource;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.utils.ElapsedTimeIdlingResource;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


public class LocationActivitySearchTests extends LocationActivityBase {

    private final long SOLR_WAIT = 3;
    private final String SEARCH = "NW 5th and Davis";
    private final String RESULT = "NW 5th Ave & NW Davis St, Portland";
    private final int EDITTEXT_ID = R.id.textview_solr_search;

    @Test
    public void searchLocationTest() {
        // make sure Espresso does not time out
        IdlingPolicies.setMasterPolicyTimeout(SOLR_WAIT * 2, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(SOLR_WAIT * 2, TimeUnit.SECONDS);

        // perform solr search
        onView(withId(EDITTEXT_ID)).perform(typeText(SEARCH));

        // wait for results to download
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(SOLR_WAIT);
        Espresso.registerIdlingResources(idlingResource);

        // check result exists in autocomplete dropdown
        onView(withText(RESULT))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        // click on result in dropdown to place marker on map
        onView(withText(RESULT))
                .inRoot(withDecorView(not(is(activity.getWindow().getDecorView()))))
                .perform(click());

        // verify search text is updated
        onView(withId(EDITTEXT_ID)).check(matches(withText(RESULT)));

        // verify coordinates are captured from marker
        LatLng selected = activity.getSelectedLatLng();
        Bundle exitBundle = activity.buildBundle(activity.getSelectedLatLng());
        validateLatLng(selected);
        validateExitBundle(selected, exitBundle);

        // cleanup
        Espresso.unregisterIdlingResources(idlingResource);
    }
}