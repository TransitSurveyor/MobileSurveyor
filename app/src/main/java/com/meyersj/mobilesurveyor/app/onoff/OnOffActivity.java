package com.meyersj.mobilesurveyor.app.onoff;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.Fields;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.Utils;
import com.meyersj.mobilesurveyor.app.stops.GeoJSON;
import com.meyersj.mobilesurveyor.app.stops.Stop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;


public class OnOffActivity extends Activity {

    public static final String ODK_ONOFF_ACTION = "com.meyersj.mobilesurveyor.app.ODK_ONOFF";
    public final String TAG = getClass().getCanonicalName();

    /* Bind XML resources to View objects */
    @Bind(R.id.mapview) MapView mapView;
    @Bind(R.id.textview_solr_search) AutoCompleteTextView solrSearch;
    @Bind(R.id.listview_on_stops) ListView onStopsListView;
    @Bind(R.id.listview_off_stops) ListView offStopsListView;
    @Bind(R.id.button_stops_lists) Button stopsButton;
    @Bind(R.id.button_save) Button saveButton;
    @Bind(R.id.layout_stops_list) LinearLayout stopsListLayout;

    private Activity context;
    private Integer startEpoch;
    private String route;
    private String direction;
    private ArrayList<Marker> stopMarkers;
    private StopListAdapter onStopsListAdapter;
    private StopListAdapter offStopsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onoff);
        ButterKnife.bind(this);
        context = this;

        /* start timer to see how long surveyor takes to capture locations */
        startEpoch = (int) (System.currentTimeMillis() / 1000L);

        /*
            If the Activity is launched using ODK Collect action then
            grab `route` and `direction` arguments set by ODK Collect,
            otherwise use defaults for testing.
        */
        Bundle extras = getIntent().getExtras();
        if (extras != null && getIntent().getAction().equals(ODK_ONOFF_ACTION)) {
            route = extras.getString(Fields.ROUTE);
            direction = extras.getString(Fields.DIRECTION);
        }
        else {
            route = "9";
            direction = "0";
        }

        /* Setup vector tiles using MapBox access token */
        setupMapView(savedInstanceState, Utils.getMapboxToken(this));

        /*
            RetrieveStopResources performs the following operations:

            1. Open stops geojson file in assets for current route/direction:
                - assets/geojson/{route}_{direction}_stops.geojson
            2. Parse json into ArrayList of Stop objects:
            3. Add stop markers to MapView
            4. Build ON and OFF dropdown lists of stops served
        */
        new RetrieveStopResources().execute();

        /* Register callback to exit with ON/OFF stop locations when user taps SAVE */
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause()  {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void setupMapView(Bundle savedInstanceState, String accessToken) {
        LatLng PORTLAND = new LatLng(45.49186, -122.679005);
        mapView.setAccessToken(accessToken);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(PORTLAND);
        mapView.setZoomLevel(11);
        mapView.onCreate(savedInstanceState);
        mapView.setOnMarkerClickListener(new MapView.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Stop stop = new Stop(marker);
                Log.d(TAG, "Stop Clicked: " + stop.stopName);
                return false;
            }
        });
    }

    private void setupStopLists(ArrayList<Stop> stops) {
        onStopsListAdapter = new StopListAdapter(context, stops);
        offStopsListAdapter = new StopListAdapter(context, stops);
        setupStopListAdapter(onStopsListView, onStopsListAdapter);
        setupStopListAdapter(offStopsListView, offStopsListAdapter);
        stopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleStopListVisibility(stopsListLayout.getVisibility());
            }
        });
    }

    private void setupStopListAdapter(final ListView listView, final StopListAdapter adapter) {
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                adapter.setSelectedIndex(position);
                Stop stop = (Stop) adapterView.getAdapter().getItem(position);
                if (listView == onStopsListView) {
                    Log.d(TAG, "selected ON stop " + stop.stopName);
                }
                else {
                    Log.d(TAG, "selected OFF stop " + stop.stopName);
                }
            }
        });
    }

    private void toggleStopListVisibility(int currentVisibility) {
        switch (currentVisibility) {
            case View.INVISIBLE:
                stopsButton.setText("Hide Stops");
                stopsListLayout.setVisibility(View.VISIBLE);
                break;
            case View.VISIBLE:
                stopsButton.setText("List Stops");
                stopsListLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void save() {
        Intent intent = new Intent();
        intent.putExtras(buildBundle());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private class RetrieveStopResources extends AsyncTask<Void, Void, ArrayList<Stop>> {

        @Override
        protected ArrayList<Stop> doInBackground(Void... voids) {
            String json = "";
            String filename = "geojson/" + route + "_" + direction + "_stops.geojson";
            try {
                json = GeoJSON.readFile(context, filename);
            } catch (IOException e) {
                Log.e(TAG, "IOException opening < " + filename + " >");
                Log.e(TAG, e.toString());
            }
            return json.isEmpty() ? null : GeoJSON.parseStops(json);
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops) {
            super.onPostExecute(stops);
            if (stops.size() > 0) {
                stopMarkers = new ArrayList<>();
                // sort stops by stop sequence
                Collections.sort(stops, new Stop.StopComparator());
                for (Stop stop: stops) {
                    Marker marker = mapView.addMarker(stop.getMarkerOptions());
                    stopMarkers.add(marker);
                }
                setupStopLists(stops);
            }
        }
    }

    public Bundle buildBundle() {
        Integer endEpoch = (int) (System.currentTimeMillis() / 1000L);
        Bundle bundle = new Bundle();
        bundle.putInt(Fields.SECONDS, endEpoch - startEpoch);
        return bundle;
    }

    public String getRoute() {
        return route;
    }

    public String getDirection() {
        return direction;
    }

    public ArrayList<Marker> getStopMarkers() {
        return stopMarkers;
    }
}