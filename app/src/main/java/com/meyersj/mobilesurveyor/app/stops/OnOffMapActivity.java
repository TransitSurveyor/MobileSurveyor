package com.meyersj.mobilesurveyor.app.stops;

import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.meyersj.mobilesurveyor.app.util.Args;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.PathUtils;
import com.meyersj.mobilesurveyor.app.util.Utils;
import com.meyersj.mobilesurveyor.app.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;


public class OnOffMapActivity extends ActionBarActivity {
    private final String TAG = "OnOffMapActivity";
    private final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_ONOFFMAP";
    private final String ONOFF_ACTION = "com.meyersj.mobilesurveyor.app.ONOFFMAP";
    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    private final String TILES = "OSMTriMet.mbtiles";
    private final Integer COUNT_MAX = 5;

    private Context context;

    // parameters for HTTP POST
    private String line;
    private String dir;
    private String url;
    private String user_id;

    // Views
    private MapView mv;
    private AutoCompleteTextView stopName;
    private ImageButton clear;
    private View seqView;
    private ListView onSeqListView;
    private ListView offSeqListView;
    private Button stopSeqBtn;
    private Button toggleOnBtn;
    private Button toggleOffBtn;
    private Button submit;
    private TextView osmText;
    private Spinner countSpinner;

    private SelectedStops selectedStops;
    private ArrayAdapter<Integer> countAdapter;
    private Integer submitCount;
    private StopSequenceAdapter onSeqListAdapter;
    private StopSequenceAdapter offSeqListAdapter;
    private HttpClient client;

    //list of markers generated from geojson for current stop and direction
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    //overlay on map that displays each stop
    private ItemizedIconOverlay locOverlay;
    //used to lookup marker when the stop name is selected from the search bar
    private HashMap<String, Marker> stopsMap;

    Boolean isOnReversed = false;
    Boolean isOffReversed = false;
    private ArrayList<Marker> locListOpposite = new ArrayList<Marker>();

    private ItemizedIconOverlay selOverlay;
    private ArrayList<Marker> selList = new ArrayList<Marker>();
    private BoundingBox bbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_off_map);
        context = getApplicationContext();
        submit = (Button) findViewById(R.id.submit);
        mv = (MapView) findViewById(R.id.mapview);
        clear = (ImageButton) findViewById(R.id.clear_input_stop);
        stopName = (AutoCompleteTextView) findViewById(R.id.input_stop);

        setupCounter(COUNT_MAX);
        getExtras();
        setTiles(mv);

        //TODO move to seperate class for all other posts
        client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);


        if (line != null && dir != null) {
            locList = getStops(line, dir, true);
            selList = new ArrayList<Marker>();

            // set listener for when marker tooltip is selected
            for (Marker marker: locList) {
                setToolTipListener(marker);
            }

            if(bbox != null) {
                mv.zoomToBoundingBox(bbox, true, false, true, true);
            }

            setItemizedOverlay(mv, locList, selList);
            mv.addListener(new OnOffMapListener(mv, locList, locOverlay));

            addRoute(line, dir);
            setupStopSequenceList();
            setupStopSearch();
            selectedStops = new SelectedStops(
                    context, onSeqListAdapter, offSeqListAdapter, selOverlay);

            //if line is a streetcar
            //enable on or off to be reversed because streetcar runs in a loop
            for (String streetcar: Cons.STREETCARS) {
                if (streetcar.equals(line)) {
                    Log.d(TAG, "we have a streetcar");
                    setupReverseStops();
                    break;
                }
            }

        }

        Intent i = this.getIntent();
        String action = i.getAction();

        if (action.equals(ONOFF_ACTION)) {
            setupOnOffSubmitAction();
        }
        else if (action.equals(ODK_ACTION)) {
            setupODKSubmitAction();
            countSpinner.setVisibility(View.GONE);

            // change layout params of submit button so it fills parent after count spinner is removed
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 10.0f);
            submit.setLayoutParams(params);

        }

        if (!Utils.isNetworkAvailable(context)) {
            Utils.longToastCenter(context,
                    "Please enable network connections.");
        }
    }

    protected void setupReverseStops() {
        String dirOpposite = dir.equals("0") ? "1" : "0";
        locListOpposite = getStops(line, dirOpposite, false);

        toggleOnBtn = (Button) findViewById(R.id.on_btn);
        toggleOffBtn = (Button) findViewById(R.id.off_btn);

        Drawable onDrawable = context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_none);
        Drawable offDrawable = context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_none);

        toggleOnBtn.setBackground(onDrawable);
        toggleOffBtn.setBackground(offDrawable);

        toggleOnBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isOffReversed) {
                    Utils.shortToastCenter(context, "Both on and off cannot have direction reversed");
                }
                else {
                    reverseDirection(Cons.ON, isOnReversed);
                }
                return true;
            }
        });
        toggleOffBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isOnReversed) {
                    Utils.shortToastCenter(context, "Both on and off cannot have direction reversed");
                }
                else {
                    reverseDirection(Cons.OFF, isOffReversed);
                }
                return true;
            }
        });
    }

    protected void reverseDirection(String mode, Boolean isReversed) {
        if(mode.equals(Cons.ON)) {
            if(isReversed == false) {
                changeAdapter(onSeqListView, onSeqListAdapter, locListOpposite);
                isOnReversed = true;
                toggleOnBtn.setText("On Stop (Opposite Direction)");
            }
            else {
                changeAdapter(onSeqListView, onSeqListAdapter, locList);
                isOnReversed = false;
                toggleOnBtn.setText("On Stop");
            }
        }
        else {
            if(isReversed == false) {
                changeAdapter(offSeqListView, offSeqListAdapter, locListOpposite);
                isOffReversed = true;
                toggleOffBtn.setText("Off Stop (Opposite Direction)");
            }
            else {
                changeAdapter(offSeqListView, offSeqListAdapter, locList);
                isOffReversed = false;
                toggleOffBtn.setText("Off Stop");
            }
        }
    }

    private void changeAdapter(ListView listView, StopSequenceAdapter adapter, ArrayList<Marker> locList)  {
        ArrayList<Stop> stops = stopsSequenceSort(locList);
        if (adapter == onSeqListAdapter) {
            onSeqListAdapter = new StopSequenceAdapter(this, stops);
            selectedStops.setOnAdapter(onSeqListAdapter);
            selectedStops.clearSequenceMarker(Cons.BOARD);
            stopSequenceAdapterSetup(listView, onSeqListAdapter);
        }
        else {
            offSeqListAdapter = new StopSequenceAdapter(this, stops);
            selectedStops.setOffAdapter(offSeqListAdapter);
            selectedStops.clearSequenceMarker(Cons.ALIGHT);
            stopSequenceAdapterSetup(listView, offSeqListAdapter);
        }
    }

    protected void stopSequenceAdapterSetup(final ListView listView, final StopSequenceAdapter adapter) {
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                adapter.setSelectedIndex(position);

                Stop stop = (Stop) adapterView.getAdapter().getItem(position);

                if (listView == onSeqListView) {
                    selectedStops.saveSequenceMarker(Cons.BOARD, stop);
                } else {
                    selectedStops.saveSequenceMarker(Cons.ALIGHT, stop);
                }
            }
        });
    }

    private void setupStopSearch() {
        String[] stopNames = buildStopsArray(locList);
        //String[] stopNames =
        //      {"N Lombard TC MAX Station", "SW 6th & Madison St MAX Station","13123", "11512", ... };
        final ArrayList<String> stopsList = new ArrayList<String>();
        Collections.addAll(stopsList, stopNames);

        StopSearchAdapter adapter = new StopSearchAdapter
                (this,android.R.layout.simple_list_item_1,stopsList);
        stopName.setAdapter(adapter);

        stopName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                stopName.setText("");
                closeKeypad();
                selectLocType(stopsMap.get(stopsList.get(position)));
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopName.clearListSelection();
                stopName.setText("");
            }
        });
    }

    public void closeKeypad() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void setupCounter(int size) {
        countSpinner = (Spinner) findViewById(R.id.count_spinner);

        ArrayList<Integer> countList = new ArrayList<Integer>();
        for (int i = 1; i < size + 1; i++) {
            countList.add(i);
        }

        countAdapter = new ArrayAdapter<Integer>(context, R.layout.spinner_item_center, countList);
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countSpinner.setAdapter(countAdapter);

        countSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                submitCount = (Integer) adapterView.getAdapter().getItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}

        });
    }

    private void setupStopSequenceList() {
        seqView = (View) findViewById(R.id.seq_list);
        stopSeqBtn = (Button) findViewById(R.id.stop_seq_btn);
        onSeqListView = (ListView) findViewById(R.id.on_stops_seq);
        offSeqListView = (ListView) findViewById(R.id.off_stops_seq);
        osmText = (TextView) findViewById(R.id.osm_text);

        /* if streetcar we need opposite direction stops in case
        user toggles that on or off was before start of line */

        ArrayList<Stop> stops = stopsSequenceSort(locList);

        onSeqListAdapter = new StopSequenceAdapter(this, stops);
        offSeqListAdapter = new StopSequenceAdapter(this, stops);

        stopSequenceAdapterSetup(onSeqListView, onSeqListAdapter);
        stopSequenceAdapterSetup(offSeqListView, offSeqListAdapter);

        stopSeqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSeqListVisibility(seqView.getVisibility());
            }
        });
    }

    //toggle visibility of sequence list depending on current visibility
    private void changeSeqListVisibility(int currentVisibility) {
        osmText.setVisibility(currentVisibility);

        if (currentVisibility == View.INVISIBLE) {
            stopSeqBtn.setText("Hide stop sequences");
            seqView.setVisibility(View.VISIBLE);
            stopSeqBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_top));
        }
        else {
            seqView.setVisibility(View.INVISIBLE);
            stopSeqBtn.setText("Show stop sequences");
            stopSeqBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_all));
        }
    }

    private void setupOnOffSubmitAction() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedStops.validateSelection()) {
                    //verify correct locations
                    if (selectedStops.validateStopSequence(line, isOnReversed || isOffReversed)) {
                        verifyAndSubmitLocationsPOST(submitCount);
                    }
                    else {
                        Utils.longToastCenter(context,
                                "Invalid stop sequence based on route direction");
                    }
                }
                else {
                    Utils.longToastCenter(context,
                            "Both boarding and alighting locations must be set");
                }
            }
        });
    }

    private void setupODKSubmitAction() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedStops.validateSelection()) {
                    //verifyAndSubmitLocationsODK();
                    String onStop = selectedStops.getBoard().getDescription();
                    String offStop = selectedStops.getAlight().getDescription();
                    if (selectedStops.validateStopSequence(line, isOnReversed || isOffReversed)) {
                        exitWithStopIDs(onStop, offStop);
                    }
                    else {
                        Utils.longToastCenter(context,
                                "Invalid stop sequence based on route direction");
                    }
                }
                else {
                    Utils.longToastCenter(context,
                            "Both boarding and alighting locations must be set");
                }
            }
        });
    }

    protected ArrayList<Stop> stopsSequenceSort(final ArrayList<Marker> locList) {
        ArrayList<Stop> stops = new ArrayList<Stop>();

        for(Marker marker: locList) {
            stops.add((Stop) marker);
        }
        Collections.sort(stops);

        return stops;
    }

    protected String[] buildStopsArray(ArrayList<Marker> locList) {

        stopsMap = new HashMap<String, Marker>();

        for(Marker m: locList) {
            stopsMap.put(m.getTitle(), m);
            stopsMap.put(m.getDescription(), m);
        }

        String[] stopNames = new String[stopsMap.size()];

        Integer i = 0;
        for (String key : stopsMap.keySet()) {
            stopNames[i] = key;
            i += 1;
        }

        return stopNames;
    }

    protected void postResults(String onStop, String offStop, Boolean isOnReversed, Boolean isOffReversed) {
        Log.d(TAG, "posting results");
        Date date = new Date();
        Bundle extras = new Bundle();
        extras.putString(Cons.URL, url);
        extras.putString(Cons.TYPE, Cons.PAIR);
        extras.putString(Args.Stops.DATE, Utils.dateFormat.format(date));
        extras.putString(Args.Stops.USER_ID, user_id);
        extras.putString(Args.Stops.RTE, line);
        extras.putString(Args.Stops.DIR, dir);
        extras.putString(Args.Stops.ON_STOP, onStop);
        extras.putString(Args.Stops.OFF_STOP, offStop);
        extras.putString(Args.Stops.ON_REVERSED, String.valueOf(isOnReversed));
        extras.putString(Args.Stops.OFF_REVERSED, String.valueOf(isOffReversed));

        Utils.appendCSV("stops", buildPairRow(extras));
        String[] params = getPairParams(extras);
        PostTask task = new PostTask();
        task.execute(params);
    }

    protected String buildPairRow(Bundle bundle) {
        String row = "";
        row += bundle.getString(Cons.DATE) + ",";
        row += bundle.getString(Cons.USER_ID) + ",";
        row += bundle.getString(Cons.LINE) + ",";
        row += bundle.getString(Cons.DIR) + ",";
        row += bundle.getString(Cons.ON_STOP) + ",";
        row += bundle.getString(Cons.OFF_STOP) + ",";
        row += bundle.getString(Cons.ON_REVERSED) + ",";
        row += bundle.getString(Cons.OFF_REVERSED);
        return row;
    }

    protected String[] getPairParams(Bundle bundle) {
        Log.d(TAG, Utils.getUrlApi(context));
        String[] params = new String[9];
        params[0] = Utils.getUrlApi(context) + "/insertPair";
        params[1] = bundle.getString(Args.Stops.DATE);
        params[2] = bundle.getString(Args.Stops.USER_ID);
        params[3] = bundle.getString(Args.Stops.RTE);
        params[4] = bundle.getString(Args.Stops.DIR);
        params[5] = bundle.getString(Args.Stops.ON_STOP);
        params[6] = bundle.getString(Args.Stops.OFF_STOP);
        params[7] = bundle.getString(Args.Stops.ON_REVERSED);
        params[8] = bundle.getString(Args.Stops.OFF_REVERSED);
        return params;
    }

    class PostTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, "url:" + params[0]);
            Log.d(TAG, "data:" + params[1]);
            return post(params);
        }
        @Override
        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute(): " + response);
        }
    }

    protected String post(String[] params) {

        String retVal = null;
        HttpPost post = new HttpPost(params[0]);
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Args.Stops.DATE, params[1]));
        postParam.add(new BasicNameValuePair(Args.Stops.USER_ID, params[2]));
        postParam.add(new BasicNameValuePair(Args.Stops.RTE, params[3]));
        postParam.add(new BasicNameValuePair(Args.Stops.DIR, params[4]));
        postParam.add(new BasicNameValuePair(Args.Stops.ON_STOP, params[5]));
        postParam.add(new BasicNameValuePair(Args.Stops.OFF_STOP, params[6]));
        postParam.add(new BasicNameValuePair(Args.Stops.ON_REVERSED, params[7]));
        postParam.add(new BasicNameValuePair(Args.Stops.OFF_REVERSED, params[8]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            Log.d(TAG, EntityUtils.toString(entityR));
            retVal = response.toString();

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException" + e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        }
        return retVal;
    }


    protected void resetMap() {
        selectedStops.clearCurrentMarker();
        mv.zoomToBoundingBox(bbox, true, false, true, true);
    }


    protected void setTiles(MapView mv) {
        ILatLng startingPoint = new LatLng(45.52186, -122.679005);
        ITileLayer source;
        BoundingBox box = new BoundingBox(46.0 ,-122 ,45.0, -123.5);

        try {
            File mbTiles = new File(TILESPATH, TILES);
            source = new MBTilesLayer(mbTiles);
            mv.setTileSource(source);
            box = source.getBoundingBox();
        }
        catch(Exception e) {
            Log.d(TAG, "unable to open local mbtiles");
            source = new WebSourceTileLayer("openstreetmap",
                    "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(11)
                    .setMaximumZoomLevel(17);
            mv.setTileSource(source);
        }

        mv.setScrollableAreaLimit(box);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(startingPoint);
        //mv.setZoom(14);
    }


    protected void setItemizedOverlay(
            final MapView mapView, ArrayList<Marker> locList, ArrayList<Marker> selList) {

        locOverlay = new ItemizedIconOverlay(mv.getContext(), locList,

                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectLocType(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );

        selOverlay = new ItemizedIconOverlay(mv.getContext(), selList, new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
            @Override
            public boolean onItemSingleTapUp(int i, Marker marker) {
                Log.d(TAG, "selOverlay single tap");
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, Marker marker) {
                Log.d(TAG, "selOverlay long press");
                return false;
            }
        });

        mv.addItemizedOverlay(locOverlay);
        mv.addItemizedOverlay(selOverlay);
    }

    protected void selectLocType(final Marker selectedMarker) {
        String message = selectedMarker.getTitle();

        final CharSequence[] items = {Cons.BOARD, Cons.ALIGHT};

        AlertDialog.Builder builder = new AlertDialog.Builder(OnOffMapActivity.this);
        builder.setTitle(message)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String choice = items[i].toString();
                        Log.d(TAG, "Choice: " + choice);
                        selectedStops.setCurrentMarker(selectedMarker, choice);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Clicked OK");
                        //check if choice (on-off) is being displayed in reverse
                        //if so switch to regular direction
                        //and highlight selected
                        String choice = selectedStops.getCurrentType();
                        Log.d(TAG, choice);

                        if(choice.equals(Cons.BOARD) && isOnReversed) {
                            reverseDirection(Cons.ON, isOnReversed);
                        }
                        if(choice.equals(Cons.ALIGHT) && isOffReversed) {
                            reverseDirection(Cons.OFF, isOffReversed);
                        }

                        selectedStops.saveCurrentMarker(selectedMarker);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Clicked Cancel");
                        selectedStops.clearCurrentMarker();
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }


    // set spinner to first item
    private void resetCount() {
        submitCount = 1;
        countSpinner.setSelection(0);

    }
    protected void verifyAndSubmitLocationsPOST(final int count) {

        final Stop board = (Stop) selectedStops.getBoard();
        final Stop alight = (Stop) selectedStops.getAlight();

        final Boolean isOnReversed = !board.getDir().equals(dir);
        final Boolean isOffReversed = !alight.getDir().equals(dir);

        if (Utils.isNetworkAvailable(context)) {
            String boardLoc = board.getTitle();
            String alightLoc = alight.getTitle();
            String message = Cons.BOARD + ": " + boardLoc + "\n\n" + Cons.ALIGHT + ": " + alightLoc;
            AlertDialog.Builder builder = new AlertDialog.Builder(OnOffMapActivity.this);
            builder.setTitle("Are you sure you want to submit these locations?")
                    .setMessage(message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //get stop ids
                            String onStop = board.getDescription();
                            String offStop = alight.getDescription();
                            //call function to post on off pair

                            for(int i_count = 0; i_count < count; i_count++ ) {
                                postResults(onStop, offStop, isOnReversed, isOffReversed);
                            }
                            resetCount();
                            resetMap();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

            AlertDialog select = builder.create();
            select.show();
        }
        else {
            Log.d(TAG, "No network connection");
            Utils.shortToastCenter(context, "Please enable network connections.");
        }
    }

    private boolean exitWithStopIDs(String onStop, String offStop) {
        Intent intent = new Intent();
        intent.putExtra(Cons.ODK_BOARD, onStop);
        intent.putExtra(Cons.ODK_ALIGHT, offStop);
        setResult(RESULT_OK, intent);
        finish();
        return true;
    }

    protected void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if(extras.containsKey(Cons.LINE)) {
                Log.d(TAG, extras.getString(Cons.LINE));
                line = extras.getString(Cons.LINE);
            }
            if(extras.containsKey(Cons.DIR)) {
                Log.d(TAG, extras.getString(Cons.DIR));
                dir = extras.getString(Cons.DIR);
            }
            if(extras.containsKey(Cons.URL)) {
                Log.d(TAG, extras.getString(Cons.URL));
                url = extras.getString(Cons.URL);
            }
            if(extras.containsKey(Cons.USER_ID)) {
                Log.d(TAG, extras.getString(Cons.USER_ID));
                user_id = extras.getString(Cons.USER_ID);
            }
        }
    }

    // open stops geojson for current route
    // parse into ArrayList of markers
    // each marker contains stop description, stop id, stop sequence and LatLng
    protected ArrayList<Marker> getStops(String line, String dir, Boolean zoom) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName, dir);

        if(zoom) {
            bbox = stops.getBoundingBox();
            mv.zoomToBoundingBox(bbox, true, false, true, true);
        }

        return stops.getStops();
    }


    //modify mView for each toolTip in each marker to prevent closing it when touched
    protected void setToolTipListener(final Marker marker) {

        View mView = marker.getToolTip(mv).getView();

        //from InfoWindow Constructor but commented out close
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    //do nothing (don't close)
                }
                return false;
            }
        });
        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "locOverlay toolTip LongPress");
                Log.d(TAG, marker.getTitle());
                selectLocType(marker);
                return true;
            }
        });
    }

    protected void addRoute(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_routes.geojson";

        Paint pathPaint = new Paint();
        pathPaint.setColor(getResources().getColor(R.color.black_light_light));
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(6.0f);
        pathPaint.setStyle(Paint.Style.STROKE);

        ArrayList<PathOverlay> paths = PathUtils.getPathFromAssets(this, "geojson/" + geoJSONName);

        if (paths != null) {
            for(PathOverlay mPath: paths) {
                mPath.setPaint(pathPaint);
                mv.addOverlay(mPath);
            }
        }
    }
}
