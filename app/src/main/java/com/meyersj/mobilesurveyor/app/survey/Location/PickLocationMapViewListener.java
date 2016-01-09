package com.meyersj.mobilesurveyor.app.survey.Location;

import android.graphics.drawable.Drawable;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;


public class PickLocationMapViewListener implements MapViewListener {
    private final String TAG = "MyMapViewListener";
    private ItemizedIconOverlay locOverlay;
    private SurveyManager manager;
    private String mode;
    private Drawable icon;
    private Drawable circle;
    private Drawable square;
    private MapFragment fragment;


    public PickLocationMapViewListener(MapFragment fragment, ItemizedIconOverlay locOverlay, Drawable icon) {
        this.locOverlay = locOverlay;
        this.icon = icon;
        this.fragment = fragment;
    }

    public PickLocationMapViewListener(PickLocationFragment fragment, ItemizedIconOverlay locOverlay, SurveyManager manager, String mode,
                                       Drawable circle, Drawable square) {
        this.locOverlay = locOverlay;
        this.manager = manager;
        this.mode = mode;
        this.circle = circle;
        this.square = square;
        this.fragment = fragment;
    }

    @Override
    public void onShowMarker(final MapView mapView, final Marker marker) {}

    @Override
    public void onHidemarker(MapView mapView, Marker marker) {}

    @Override
    public void onTapMarker(final MapView mapView, final Marker marker) {}

    @Override
    public void onLongPressMarker(final MapView mapView, final Marker marker) {

    }

    @Override
    public void onTapMap(MapView mapView, ILatLng iLatLng) {}

    @Override
    public void onLongPressMap(MapView mapView, ILatLng iLatLng) {
        LatLng latLng = new LatLng(iLatLng.getLatitude(), iLatLng.getLongitude());
        Marker m = new Marker(mapView, "", null, latLng);
        if(manager != null) {
            if(mode.equals("origin")) {
                if (manager.getOrig() != null) fragment.removeLocation(manager.getOrig());
                m.setMarker(circle);
            }
            else if(mode.equals("destination")) {
                if (manager.getDest() != null) fragment.removeLocation(manager.getDest());
                m.setMarker(square);
            }
            manager.setLocation(m, mode);
        }
        else {
            m.setMarker(icon);
        }
        locOverlay.removeAllItems();
        locOverlay.addItem(m);
        mapView.invalidate();
        m.addTo(mapView);
    }
}




