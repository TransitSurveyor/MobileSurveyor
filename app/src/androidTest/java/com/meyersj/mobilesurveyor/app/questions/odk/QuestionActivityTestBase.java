package com.meyersj.mobilesurveyor.app.questions.odk;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.app.Fragment;

import com.meyersj.mobilesurveyor.app.odk.QuestionActivity;

import junit.framework.Assert;

import org.junit.Rule;


public abstract class QuestionActivityTestBase {

    public final String TAG = getClass().getCanonicalName();
    public String action = QuestionActivity.ODK_LOCATION_ACTION;
    public QuestionActivity activity;

    @Rule
    public ActivityTestRule<QuestionActivity> activityTestRule = new ActivityTestRule<QuestionActivity>(QuestionActivity.class);

    public Fragment initializeActivity() {
        Intent intent = new Intent(action);
        activity = activityTestRule.launchActivity(intent);
        Assert.assertEquals("Activity action not set", activity.getAction(), action);
        activity = activityTestRule.launchActivity(new Intent(action));
        Fragment fragment = activity.getFragment();
        Assert.assertNotNull("Fragment is null", fragment);
        return fragment;
    }
}