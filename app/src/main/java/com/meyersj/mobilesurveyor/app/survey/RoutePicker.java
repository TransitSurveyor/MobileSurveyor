package com.meyersj.mobilesurveyor.app.survey;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.meyersj.mobilesurveyor.app.R;

import java.util.ArrayList;
import java.util.HashMap;


public class RoutePicker {

    private final String TAG = "RoutePicker";
    protected Context context;
    protected LinearLayout routeLayout;
    protected LinearLayout view;
    protected Spinner spinner;
    protected ImageButton remove;
    protected ArrayList<String> routes;
    protected HashMap<String, String> routeLookup;

    public RoutePicker(Context context, LayoutInflater inflater,
                       ViewGroup parent, LinearLayout routeLayout, ArrayList<String> routes,
                       String line) {
        this.context = context;
        this.routeLayout = routeLayout;
        this.view = (LinearLayout) inflater.inflate(R.layout.route_spinner_layout, parent, false);
        this.routeLayout.addView(view);
        //if(line != null) {
        //    Log.d(TAG, "make layout visible");
        //    this.routeLayout.setVisibility(View.VISIBLE);
        //}
        this.spinner = (Spinner) this.view.findViewById(R.id.route_spinner);
        this.remove = (ImageButton) this.view.findViewById(R.id.remove_route);
        this.remove.setVisibility(View.VISIBLE);
        this.routes = routes;
        attachAdapter();
        buildRouteLookup();
        setSpinner(line);
        setRemoveListener();
    }

    public void setSpinner(String line) {
        if (line != null && !line.isEmpty()){
            String lineDesc = routeLookup.get(line);
            for (int i = 0; i < routes.size(); i++) {
                Log.d(TAG, routes.get(i));
                Log.d(TAG, lineDesc);
                if (routes.get(i).equals(lineDesc)) {
                    this.spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    protected void setRemoveListener() {
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayout.removeView(view);
            }
        });
    }


    protected void attachAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item, routes);
        spinner.setAdapter(adapter);
    }

    //TODO replace with reading input file
    protected void buildRouteLookup() {
        routeLookup = new HashMap<String, String>();
        routeLookup.put("4-Division/Fessenden", "4");
        routeLookup.put("9-Powell Blvd", "9");
        routeLookup.put("17-Holgate/Broadway", "17");
        routeLookup.put("19-Woodstock/Glisan", "19");
        routeLookup.put("28-Linwood", "28");
        routeLookup.put("29-Lake/Webster Rd", "29");
        routeLookup.put("30-Estacada", "30");
        routeLookup.put("31-King Rd", "31");
        routeLookup.put("32-Oatfield", "32");
        routeLookup.put("33-McLoughlin", "33");
        routeLookup.put("34-River Rd", "34");
        routeLookup.put("35-Macadam/Greeley", "35");
        routeLookup.put("70-12th/NE 33rd Ave", "70");
        routeLookup.put("75-Cesar Chavez/Lombard", "75");
        routeLookup.put("99-McLoughlin Express", "99");
        routeLookup.put("152-Milwaukie", "152");
        routeLookup.put("MAX Yellow Line", "190");
        routeLookup.put("MAX Green Line", "200");
        routeLookup.put("Portland Streetcar - NS Line", "193");
        routeLookup.put("Portland Streetcar - CL Line", "194");
        routeLookup.put("4", "4-Division/Fessenden");
        routeLookup.put("9", "9-Powell Blvd");
        routeLookup.put("17", "17-Holgate/Broadway");
        routeLookup.put("19", "19-Woodstock/Glisan");
        routeLookup.put("28", "28-Linwood");
        routeLookup.put("29", "29-Lake/Webster Rd");
        routeLookup.put("30", "30-Estacada");
        routeLookup.put("31", "31-King Rd");
        routeLookup.put("32", "32-Oatfield");
        routeLookup.put("33", "33-McLoughlin");
        routeLookup.put("34", "34-River Rd");
        routeLookup.put("35", "35-Macadam/Greeley");
        routeLookup.put("70", "70-12th/NE 33rd Ave");
        routeLookup.put("75", "75-Cesar Chavez/Lombard");
        routeLookup.put("99", "99-McLoughlin Express");
        routeLookup.put("152", "152-Milwaukie");
        routeLookup.put("190", "MAX Yellow Line");
        routeLookup.put("200", "MAX Green Line");
        routeLookup.put("193", "Portland Streetcar - NS Line");
        routeLookup.put("194", "Portland Streetcar - CL Line");
    }









}
