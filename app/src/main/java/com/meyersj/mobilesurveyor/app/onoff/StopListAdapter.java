package com.meyersj.mobilesurveyor.app.onoff;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.Stop;

import java.util.ArrayList;
import java.util.HashMap;


public class StopListAdapter extends ArrayAdapter<Stop> {

    private static final int LIST_ITEM_LAYOUT = R.layout.list_item_stop_sequence;
    private final String TAG = getClass().getCanonicalName();
    private final int SELECTED_COLOR, DEFAULT_COLOR;

    private Context context;
    private ArrayList<Stop> stops;
    private int selectedIndex = -1;
    private HashMap<String, Integer> stopsIndexHash;

    public StopListAdapter(Context context, ArrayList<Stop> stops) {
        super(context, LIST_ITEM_LAYOUT, stops);
        this.context = context;
        SELECTED_COLOR = ContextCompat.getColor(context, android.R.color.background_dark);
        DEFAULT_COLOR = ContextCompat.getColor(context, android.R.color.background_light);
        this.stops = stops;
        stopsIndexHash = new HashMap<>();
        for(Integer i = 0; i < stops.size(); i++) {
            stopsIndexHash.put(stops.get(i).stopId, i);
        }
    }

    @Override
    public Stop getItem(int position) {
        return stops.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(LIST_ITEM_LAYOUT, null);
        }

        Stop stop = stops.get(position);
        if (stop != null) {
            TextView label = (TextView) view.findViewById(R.id.text_stop_name);
            if (label != null) {
                label.setText(stop.stopName);
                if(position == selectedIndex) {
                    label.setBackgroundColor(SELECTED_COLOR);
                }
                else {
                    label.setBackgroundColor(DEFAULT_COLOR);
                }
            }
        }
        return view;
    }

    public Integer getItemIndex(String label) {
        return stopsIndexHash.get(label);
    }

    public void setSelectedIndex(int i) {
        selectedIndex = i;
        notifyDataSetChanged();
    }

    public void clearSelected() {
        selectedIndex = -1;
        notifyDataSetChanged();
    }
}
