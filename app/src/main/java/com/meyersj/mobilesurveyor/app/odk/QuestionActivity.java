package com.meyersj.mobilesurveyor.app.odk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.meyersj.mobilesurveyor.app.R;

public class QuestionActivity extends FragmentActivity {

    private final String TAG = getClass().getCanonicalName();
    public static final String ODK_LOCATION_ACTION = "com.meyersj.mobilesurveyor.app.GEO_LOCATION";
    public static final String ODK_ONOFF_ACTION = "com.meyersj.mobilesurveyor.app.ONOFF_LOCATIONS";

    private String action;
    private LocationFragment locationFragment;
    private OnOffFragment onOffFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        // set action and get extras from ODK
        Bundle extras = getIntentExtras();

        if (findViewById(R.id.fragment_main) == null || savedInstanceState != null) {
            return;
        }

        if (action.equals(ODK_LOCATION_ACTION)) {
            locationFragment = new LocationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_main, locationFragment).commit();
        }
        else if (action.equals(ODK_ONOFF_ACTION)) {
            onOffFragment = new OnOffFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_main, onOffFragment).commit();

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected Bundle getIntentExtras() {
        Intent intent = this.getIntent();
        action = intent.getAction();

        if (action.equals(ODK_LOCATION_ACTION)) {
            Log.d(TAG, "Geo Extras");
            return intent.getExtras();
        }
        else if (action.equals(ODK_ONOFF_ACTION)) {
            Log.d(TAG, "OnOff Extras");
            return intent.getExtras();
        }
        return null;
    }

    public String getAction() {
        return action;
    }

    public Fragment getFragment() {
        if (action.equals(ODK_LOCATION_ACTION)) {
            return locationFragment;
        }
        else if (action.equals(ODK_ONOFF_ACTION)) {
            return onOffFragment;
        }
        return null;
    }

    public void exit() {
        if (action.equals(ODK_LOCATION_ACTION)) {
            if (locationFragment != null) {
                locationFragment.exit(this);
            }
        }
        else if (action.equals(ODK_ONOFF_ACTION)) {
            //
            //
        }
        return;
    }
}