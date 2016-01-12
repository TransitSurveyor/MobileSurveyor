package com.meyersj.mobilesurveyor.app.onoff_activity;

import junit.framework.Assert;

import org.junit.Test;


public class OnOffActivityTests extends OnOffActivityBase {

    @Test
    public void intentArgumentsTest() {
        Assert.assertEquals("Invalid action", ACTION, activity.getIntent().getAction());
        Assert.assertEquals("Invalid route", ROUTE, activity.getRoute());
        Assert.assertEquals("Invalid route", DIRECTION, activity.getDirection());
    }
}