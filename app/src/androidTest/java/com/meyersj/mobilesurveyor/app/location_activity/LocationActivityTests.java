package com.meyersj.mobilesurveyor.app.location_activity;

import junit.framework.Assert;

import org.junit.Test;


public class LocationActivityTests extends LocationActivityBase {

    @Test
    public void actionSetTest() {
        Assert.assertEquals("Invalid action", ACTION, activity.getIntent().getAction());
    }
}