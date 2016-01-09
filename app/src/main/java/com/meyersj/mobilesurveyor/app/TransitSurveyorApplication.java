package com.meyersj.mobilesurveyor.app;

import android.app.Application;
import android.util.Log;

import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.Properties;

public class TransitSurveyorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Properties prop = Utils.getProperties(this, "config.properties");
        String token = prop.getProperty(Cons.NEWRELIC, "");
        Log.d("SurveyApplication", "token: " + token);
        //if (!token.isEmpty()) {
        //    NewRelic.withApplicationToken(token).start(this);
        //}
    }
}
