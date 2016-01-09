package com.meyersj.mobilesurveyor.app.odk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.locations.LocationResult;
import com.meyersj.mobilesurveyor.app.locations.SolrAdapter;
import com.meyersj.mobilesurveyor.app.survey.Location.PickLocationMapViewListener;
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.util.Args;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;
import java.util.Properties;


public class LocationFragment extends MapFragment {

    private ImageButton clear;
    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    protected Properties prop;
    private AutoCompleteTextView solrSearch;
    private SolrAdapter adapter;
    protected Drawable icon;
    protected Bundle extras;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_odk_location, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        icon = context.getResources().getDrawable(R.drawable.start);
        clear = (ImageButton) view.findViewById(R.id.clear_text);
        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);
        locOverlay = newItemizedOverlay(locList);
        mv.addOverlay(locOverlay);
        mv.setMapViewListener(new PickLocationMapViewListener(this, locOverlay, icon));
        prop = Utils.getProperties(context, Cons.PROPERTIES);

        if (!Utils.isNetworkAvailable(context)) {
            Utils.shortToastCenter(context, "No network connection, pick location from map");
        }

        solrSearch = (AutoCompleteTextView) view.findViewById(R.id.solr_input);
        adapter = new SolrAdapter(context, android.R.layout.simple_list_item_1, Utils.getUrlSolr(context));
        solrSearch.setAdapter(adapter);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // set up listeners for view objects
        solrSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String address = parent.getItemAtPosition(position).toString();
                LocationResult locationResult = adapter.getLocationResultItem(address);

                if (locationResult != null) {
                    addMarker(locationResult.getLatLng());
                }
                Utils.closeKeypad(activity);
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solrSearch.clearListSelection();
                solrSearch.setText("");
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private ItemizedIconOverlay newItemizedOverlay(ArrayList<Marker> markers) {
        ItemizedIconOverlay overlay = new ItemizedIconOverlay(mv.getContext(), markers,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
        return overlay;
    }

    private void addMarker(LatLng latLng) {
        if(latLng != null) {
            clearMarkers();
            Marker m = new Marker(null, null, latLng);
            m.setMarker(icon);
            m.addTo(mv);
            locOverlay.addItem(m);
            mv.invalidate();
            mv.setCenter(latLng);
            mv.setZoom(17);
        }
    }

    private void clearMarkers() {
        locOverlay.setFocus(null);
        locOverlay.removeAllItems();
        mv.invalidate();
    }

    public Intent buildExitIntent() {
        LatLng loc = getSelectedLatLng();
        if (loc == null) {
            Utils.shortToastCenter(context, "No location selected.");
            return null;
        }
        Intent intent = new Intent();
        intent.putExtra(Args.LAT, String.valueOf(loc.getLatitude()));
        intent.putExtra(Args.LON, String.valueOf(loc.getLongitude()));
        return intent;
    }

    public void exit(final Activity activity) {
        Intent intent = buildExitIntent();
        if (intent == null) {
            activity.setResult(Activity.RESULT_CANCELED, new Intent());
        }
        else {
            activity.setResult(Activity.RESULT_OK, intent);
        }
        activity.finish();
    }

    public LatLng getSelectedLatLng() {
        if (locList.size() == 0) {
            return null;
        }
        Marker m = locList.get(locList.size() - 1);
        return m.getPoint();
    }
}