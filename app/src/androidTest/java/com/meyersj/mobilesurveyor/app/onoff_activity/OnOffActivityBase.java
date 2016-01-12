package com.meyersj.mobilesurveyor.app.onoff_activity;


import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import com.meyersj.mobilesurveyor.app.Fields;
import com.meyersj.mobilesurveyor.app.onoff.OnOffActivity;

import org.junit.Before;
import org.junit.Rule;

public abstract class OnOffActivityBase {

    public final String TAG = getClass().getCanonicalName();
    public final String ACTION = OnOffActivity.ODK_ONOFF_ACTION;
    public final String ROUTE = "9";
    public final String DIRECTION = "0";

    public OnOffActivity activity;

    @Rule
    public ActivityTestRule<OnOffActivity> activityTestRule = new ActivityTestRule<>(OnOffActivity.class);

    @Before
    public void setUp() {
        Intent intent = new Intent(ACTION);
        intent.putExtra(Fields.ROUTE, ROUTE);
        intent.putExtra(Fields.DIRECTION, DIRECTION);
        activity = activityTestRule.launchActivity(intent);
    }
}
