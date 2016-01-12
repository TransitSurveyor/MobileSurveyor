package com.meyersj.mobilesurveyor.app.onoff_activity;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.Stop;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


public class OnOffActivityStopTests extends OnOffActivityBase {

    private final String FIRST_STOP_NAME = "NW 5th & Davis";
    private final String FIRST_STOP_ID = "9301";
    private final Integer FIRST_STOP_SEQUENCE = 100;

    @Test
    public void loadStopsTest() {
        int POSITION = 0;

        //.onChildView(withText(FIRST_STOP_NAME)).atPosition(POSITION)
        //onData(withId(R.id.listview_on_stops))
        //        .onChildView(ViewMatchers.withClassName(Matchers.is()))
        //        .atPosition(POSITION)
        //        .check(ViewAssertions.matches(withText(FIRST_STOP_NAME)));

        //onView(withId(R.id.listview_on_stops))
        //        .check(ViewAssertions.matches())


                //, ViewMatchers.hasSibling(withText(FIRST_STOP_NAME)));

       // )
         //       .check(matches(withText(FIRST_STOP_NAME)));

                //.withText(FIRST_STOP_NAME))
                //.check(matches(ViewMatchers.isDisplayed()))

        ArrayList<Marker> stops = activity.getStopMarkers();
        Assert.assertNotNull("Stop markers is null", stops);
        Assert.assertTrue("Stop markers is empty", stops.size() > 0);
        //Stop stop = new Stop(stops.get(POSITION));
        //Assert.assertEquals("Incorrect stop name", FIRST_STOP_NAME, stop.stopName);
        //Assert.assertEquals("Incorrect stop id", FIRST_STOP_ID, stop.stopId);
        //Assert.assertTrue("Incorrect stop sequence", stop.stopSequence.equals(FIRST_STOP_SEQUENCE));
    }
}