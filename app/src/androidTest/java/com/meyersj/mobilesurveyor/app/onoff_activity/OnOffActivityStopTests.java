package com.meyersj.mobilesurveyor.app.onoff_activity;

import android.support.test.espresso.action.ViewActions;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.Stop;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;


public class OnOffActivityStopTests extends OnOffActivityBase {

    private final String FIRST_STOP_NAME = "NW 5th & Davis";

    @Test
    public void loadStopsTest() {
        int POSITION = 0;

        onView(withId(R.id.button_stops_lists)).perform(ViewActions.click());

        onData(allOf(is(instanceOf(Stop.class))))
                .inAdapterView(withId(R.id.listview_on_stops))
                .atPosition(POSITION)
                .check(matches(withChild(withText(FIRST_STOP_NAME))));
    }
}