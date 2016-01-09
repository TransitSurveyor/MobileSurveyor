package com.meyersj.mobilesurveyor.app.questions.odk;

import com.meyersj.mobilesurveyor.app.odk.OnOffFragment;
import com.meyersj.mobilesurveyor.app.odk.QuestionActivity;

import org.junit.Before;
import org.junit.Test;


public class OnOffMapTests extends OnOffTestBase {

    protected String ROUTE = "9";
    protected String DIRECTION = "0";

    @Before
    public void setUp() {
        action = QuestionActivity.ODK_ONOFF_ACTION;
    }

    @Test
    public void stopsListTest() {
        OnOffFragment fragment = (OnOffFragment) initializeActivity();
    }
}
