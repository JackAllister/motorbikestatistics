package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Jack on 23-Jan-17.
 */

public class LoadDeviceFragment extends Fragment {

    private final static String NEW_LINE = "\r\n";
    private final static String LOAD_TRIP_CHAR = "3";

    private BTConnection btConnection = null;
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
        lvTripList.setOnItemClickListener(tripClickListener);

        ViewGroup headerView = (ViewGroup)inflater.inflate(R.layout.trip_list_header, lvTripList, false);
        lvTripList.addHeaderView(headerView);

        lvAdapter = new TripListAdapter(getActivity(), R.layout.trip_list_item, tripList);
        lvTripList.setAdapter(lvAdapter);

        tripList.clear();
        lvAdapter.notifyDataSetChanged();

        return myView;
    }

    public void setBTConnection(BTConnection btConnection) {
        this.btConnection = btConnection;
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
            if (receiveString.indexOf(NEW_LINE) >= 0) {
                String[] line = receiveString.split(NEW_LINE);

                /*
                 * We want to check every line for JSON data as it could be possible
                 * that multiple JSON objects arrive at once.
                 */
                for (int i = 0; i < line.length; i++) {
                    /* Try parse each line for JSON data */
                    try {
                        JSONObject tmpJSON = new JSONObject(line[i]);
                        addTrip(tmpJSON);

                        /* Remove string from buffer if successfully added */
                        receiveString = receiveString.replace(line[i] + NEW_LINE, "");
                    } catch (JSONException e) {
                        /* Ignore line if exception */
                    }
                }
            }
        }
    };

    public final ListView.OnItemClickListener tripClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (btConnection != null && btConnection.isConnected()) {
                TripItem tripItem = (TripItem) parent.getItemAtPosition(position);

                /*
                 * Create a new statistics fragment.
                 * This will receive the stored data from the logging device.
                 */
                RealtimeFragment statFragment = new RealtimeFragment();
                btConnection.setRXHandler(statFragment.RXHandler);

                /* Transmit over the name of the trip we want to load */
                Message message = new Message();
                message.obj = (String) LOAD_TRIP_CHAR + tripItem.getTripName();
                message.setTarget(btConnection.txHandler);
                message.sendToTarget();

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, statFragment)
                        .commit();
            }
        }
    };
}
