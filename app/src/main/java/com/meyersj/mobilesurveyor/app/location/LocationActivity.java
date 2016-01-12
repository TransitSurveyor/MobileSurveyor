package com.meyersj.mobilesurveyor.app.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.Fields;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.Utils;
import com.meyersj.mobilesurveyor.app.geocode.LocationResult;
import com.meyersj.mobilesurveyor.app.geocode.SolrAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;


public class LocationActivity extends Activity {

    public static final String ODK_LOCATION_ACTION = "com.meyersj.mobilesurveyor.app.ODK_LOCATION";

    @Bind(R.id.mapview) MapView mapView;
    @Bind(R.id.textview_solr_search) AutoCompleteTextView solrSearch;
    @Bind(R.id.button_save) Button saveButton;

    private SolrAdapter adapter;
    private Marker selectedLocation;
    private Integer startEpoch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
        startEpoch = (int) (System.currentTimeMillis() / 1000L);
        setupMapView(savedInstanceState, Utils.getMapboxToken(this));
        setupSolr(this);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    @Override
    protected void onStart() {
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
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void setupMapView(Bundle savedInstanceState, String accessToken) {
        LatLng PORTLAND = new LatLng(45.49186, -122.679005);
        mapView.setAccessToken(accessToken);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.setCenterCoordinate(PORTLAND);
        mapView.setZoomLevel(11);
        mapView.onCreate(savedInstanceState);
        mapView.setOnMapLongClickListener(new MapView.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng point) {
                selectLocation(point);
            }
        });
    }

    public void setupSolr(final Activity activity) {
        String url = Utils.getUrlSolr(this);
        int layout = android.R.layout.simple_list_item_1;
        adapter = new SolrAdapter(this, layout, url);
        solrSearch.setAdapter(adapter);
        solrSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String address = parent.getItemAtPosition(position).toString();
                LocationResult locationResult = adapter.getLocationResultItem(address);
                if (locationResult != null) {
                    mapView.setCenterCoordinate(locationResult.latLng);
                    selectLocation(locationResult.latLng);
                }
                Utils.closeKeypad(activity);
            }
        });
    }

    public void selectLocation(LatLng latLng) {
        if (selectedLocation != null) {
            mapView.removeMarker(selectedLocation);
        }
        MarkerOptions options = new MarkerOptions().position(latLng);
        mapView.addMarker(options);
        selectedLocation = options.getMarker();
    }

    public Bundle buildBundle(LatLng location) {
        if (location == null) {
            return null;
        }
        Integer endEpoch = (int) (System.currentTimeMillis() / 1000L);
        Bundle bundle = new Bundle();
        bundle.putString(Fields.LAT, String.valueOf(location.getLatitude()));
        bundle.putString(Fields.LON, String.valueOf(location.getLongitude()));
        bundle.putInt(Fields.SECONDS, endEpoch - startEpoch);
        return bundle;
    }

    public void save() {
        setResult(Activity.RESULT_CANCELED);
        if (selectedLocation != null) {
            Intent intent = new Intent();
            intent.putExtras(buildBundle(selectedLocation.getPosition()));
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    public LatLng getSelectedLatLng() {
        if (selectedLocation != null) {
            return selectedLocation.getPosition();
        }
        return null;
    }
}