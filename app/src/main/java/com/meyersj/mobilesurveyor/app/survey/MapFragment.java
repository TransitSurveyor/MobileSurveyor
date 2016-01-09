package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.BuildStops;
import com.meyersj.mobilesurveyor.app.survey.Location.PickLocationFragment;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.TransitRoute;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public abstract class MapFragment extends Fragment {

    protected final String TAG = "MapFragment";
    protected final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    protected final String TILESNAME = "OSMTriMet.mbtiles";
    private final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_SURVEY";

    protected Activity activity;
    protected Context context;
    protected View view;
    protected MapView mv;
    protected ItemizedIconOverlay surveyOverlay;
    protected ArrayList<Marker> surveyList = new ArrayList<Marker>();
    protected TransitRoute defaultRoute;

    protected HashMap<String, TransitRoute> transferRoutes = new HashMap<String, TransitRoute>();
    protected HashMap<String, TransitRoute> cachedRoutes = new HashMap<String, TransitRoute>();
    protected String line;
    protected String dir;

    //public MapFragment() {
    //   //TODO move ODK extras fetching into this parent class instead of indiv. frags
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_default_map, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        surveyOverlay = new ItemizedIconOverlay(context, surveyList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void setTiles(MapView mv) {
        ILatLng startingPoint = new LatLng(45.49186, -122.679005);
        ITileLayer mbTilesSource;
        String url = "http://a.tile2.opencyclemap.org/transport/{z}/{x}/{y}.png";
        url = "http://tilea.trimet.org/tilecache/tilecache.py/1.0.0/currentOSM/{z}/{x}/{y}";
        ITileLayer osmSource = new WebSourceTileLayer("openstreetmap", url)
                .setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");
        try {
            File tiles = new File(TILESPATH, TILESNAME);
            mbTilesSource = new MBTilesLayer(tiles);
            mv.setTileSource(mbTilesSource);
        }
        catch(Exception e) {
            Log.e(TAG, "unable to open local mbtiles");
            mv.setTileSource(osmSource);
        }
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(startingPoint);
        mv.setZoom(12);
    }

    protected void addDefaultRoute(Context context, String line, String dir) {
        Paint paint = Utils.defaultRoutePaint(context);
        defaultRoute = new TransitRoute(context, line, dir, paint);
        defaultRoute.addRoute(mv, false);
    }

    protected void zoomToRoute(MapView mapView) {
        if(defaultRoute != null) {
            defaultRoute.zoomTo(mapView);
        }
    }

    public void addTransferRoute(Context context, String line, String dir) {
        String key = line + "_" + dir;
        Paint paint = Utils.transferRoutePaint(context);
        TransitRoute route;
        if(transferRoutes.containsKey(key)) {
            // route is already displayed
            return;
        }
        else if(cachedRoutes.containsKey(key)) {
            route = cachedRoutes.get(key);
            if(route.isValid()) {
                route.addRoute(mv, false);
                transferRoutes.put(key, route);
            }
            return;
        }

        route = new TransitRoute(context, line, dir, paint);
        transferRoutes.put(key, route);
        cachedRoutes.put(key, route);
        route.addRoute(mv, false);
    }

    public void clearRoute(String line, String dir) {
        String key = line + "_" + dir;
        if(transferRoutes.containsKey(key)) {
            TransitRoute route = transferRoutes.get(key);
            route.clearRoute(mv);
            transferRoutes.remove(key);
        }
    }

    public void removeLocation(Marker loc) {
        surveyOverlay.removeItem(loc);
    }

    public void updateView(SurveyManager manager) {
        mv.removeOverlay(surveyOverlay);
        surveyOverlay.removeAllItems();
        Marker orig = manager.getOrig();
        Marker dest = manager.getDest();
        Marker onStop = manager.getOnStop();
        Marker offStop = manager.getOffStop();
        if(orig != null) {
            surveyOverlay.addItem(orig);
        }
        if(dest != null) {
            surveyOverlay.addItem(dest);
        }
        if(onStop != null) {
            surveyOverlay.addItem(onStop);
        }
        if(offStop != null) {
            surveyOverlay.addItem(offStop);
        }
        mv.addItemizedOverlay(surveyOverlay);
    }


    public class Bounds {
        double north = 0;
        double east = 0;
        double south = 0;
        double west = 0;
        double BUFFER = 0.01;

        public Bounds() {}

        protected void update(Marker marker) {
            if(marker != null) {
                Double lat = marker.getPoint().getLatitude();
                Double lng = marker.getPoint().getLongitude();
                if(north == 0 || lat > north) north = lng;
                if(south == 0 || lat < south) south = lng;
                if(west == 0 || lat < west) west = lng;
                if(east == 0 || lat > east) east = lng;
            }
        }

        public BoundingBox getBounds() {
            return new BoundingBox(north + BUFFER, east + BUFFER, south - BUFFER, west - BUFFER);
        }

    }


    protected BoundingBox getBoundingBox(Bounds bounds, ArrayList<Marker> markers, int count) {
        if (count <= 0) {
            return bounds.getBounds();
        }
        if (bounds == null) {
            bounds = new Bounds();
        }
        bounds.update(markers.get(count - 1));
        return this.getBoundingBox(bounds, markers, count - 1);
    }

}
