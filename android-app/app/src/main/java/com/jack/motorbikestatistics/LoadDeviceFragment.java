package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jack on 23-Jan-17.
 */

public class LoadDeviceFragment extends Fragment {


    private ArrayList<TripItem> tripList;
    private ArrayAdapter<TripItem> lvAdapter;

    String receiveString = "";

    public LoadDeviceFragment() {

        tripList = new ArrayList<TripItem>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.loaddevice_layout, container, false);

        /* Get our ListView via ID, set headers and create our ArrayAdapter for it */
        ListView lvTripList = (ListView)myView.findViewById(R.id.loaddevice_triplist);

        ViewGroup headerView = (ViewGroup)inflater.inflate(R.layout.trip_list_header, lvTripList, false);
        lvTripList.addHeaderView(headerView);

        lvAdapter = new TripListAdapter(getActivity(), R.layout.trip_list_item, tripList);
        lvTripList.setAdapter(lvAdapter);

        return myView;
    }

    private final void addTrip(JSONObject jsonData) {
        try {

            /* Get name and size from json object */
            String tripName = jsonData.getString("name");
            int fileSize = jsonData.getInt("size");

            /* Add new trip to our list & notify list view */
            TripItem newTrip = new TripItem(tripName, fileSize);
            tripList.add(newTrip);
            lvAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            /* Do nothing */
        }
    }

    public final Handler RXHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            receiveString += (String) msg.obj;

            /* Check that new line exists, otherwise wait till next time called */
            if (receiveString.indexOf("\r\n") >= 0) {
                String[] line = receiveString.split("\r\n");

                /*
                 * We want to check every line for JSON data as it could be possible
                 * that multiple JSON objects arrive at once.
                 */
                for (int i = 0; i < line.length; i++) {
                    /* Try parse each line for JSON data */
                    try {
                        JSONObject tmpJSON = new JSONObject(line[i]);
                        addTrip(tmpJSON);

                    } catch (JSONException e) {
                        /* Ignore line if exception */
                    }
                }

                receiveString = "";
            }
        }
    };
}
