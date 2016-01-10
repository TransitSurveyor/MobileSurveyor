package com.meyersj.mobilesurveyor.app.solr;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SolrAdapter extends ArrayAdapter<String> implements Filterable {

    private final String TAG = getClass().getCanonicalName();;
    private List<String> mData = new ArrayList<>();
    private SolrQuery mSolrQuery;
    private HashMap<String, LocationResult> mResults;

    public SolrAdapter(Context context, int resource, String url) {
        super(context, resource);
        mSolrQuery = new SolrQuery(url);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int index) {
        return mData.get(index);
    }

    //used to retrieve item picked after user selection
    public LocationResult getLocationResultItem(String name) {
        LocationResult locationResult = null;
        if (mResults.containsKey(name)) {
            locationResult = mResults.get(name);
        }
        return locationResult;
    }

    public List<String> getNames(Map mp) {
        List<String> names = new ArrayList<String>();

        //add score to beginning of string and add to list
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            //build string with score and name
            //0.992|NW 5th and Davis
            String name = mResults.get(pairs.getKey()).getScore().toString() + "|" + pairs.getKey();
            names.add(name);
        }

        //sort names in descending order
        //higher scores will be first
        //0.992|NW 6th & Davis
        //0.123|NW 5th and Davis
        Collections.sort(names);
        Collections.reverse(names);

        //remove score from beginning of string
        //NW 6th & Davis
        //NW 5th and Davis
        for(int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            names.set(i, n.substring(n.indexOf("|") + 1));
        }
        return names;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<String> results;
                // This method is called in a worker thread
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    try {
                        //fetch data from Solr Server
                        mSolrQuery.solrSearch(constraint.toString());
                        mResults = mSolrQuery.getSolrResults();
                        //extract names to be displayed in UI
                        results = getNames(mResults);
                        filterResults.values = results;
                        filterResults.count = results.size();
                    } catch (Exception e) {
                        Log.e(TAG, "Filter failed");
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    mData = (List<String>) results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}