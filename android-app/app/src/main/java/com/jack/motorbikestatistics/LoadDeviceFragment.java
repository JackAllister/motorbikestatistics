/**
 * @file LoadDeviceFragment.java
 * @brief Fragment/Tab for providing UI for loading from device.
 *
 * UI to allow the user to load saved trips stored on the uSD of
 * the logging device.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
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
 * @brief UI Class for loading saved trips from device.
 */
public class LoadDeviceFragment extends Fragment {

    /** @brief New line string. */
    private final static String NEW_LINE = "\r\n";
    /** @brief Command string to be sent to device to load a specific trip. */
    private final static String LOAD_TRIP_CHAR = "3";

    /** @brief Current connectected logging device (via bluetooth). */
    private BTConnection btConnection = null;
    /** @brief List of all trips saved on the logging device */
    private ArrayList<TripItem> tripList;
    /** @brief Array adapter for displaying trips in ListView */
    private ArrayAdapter<TripItem> lvAdapter;

    /**
     * @brief Constructor for UI fragment.
     *
     * Creates a new arraylist of trips that is empty and
     * ready to be filled from the logging device.
     */
    public LoadDeviceFragment() {
        tripList = new ArrayList<TripItem>();
    }

    /**
     * @brief Function called when fragment is shown on UI.
     *
     * Sets up the ListView on the screen using our custom ArrayAdapter
     * specificed.
     *
     * @param inflater - Inflater used to load fragment on UI.
     * @param container - Container where fragment will be shown.
     * @param savedInstance - Information holding past state.
     * @return View - Modified view to display on the UI.
     */
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

    /**
     * @brief Setter for current BT connection.
     *
     * Set from main UI activity, allows cross tab communication with
     * the logging device.
     *
     * @param btConnection - Logging device bluetooth connection.
     */
    public void setBTConnection(BTConnection btConnection) {
        this.btConnection = btConnection;
    }

    /**
     * @brief Adds a trip to the ListView specifying name and filesize.
     *
     * @param jsonData - JSON object holding trip name and size.
     */
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

    /**
     * @brief Handler used for receiving trip names.
     *
     * Receives trip information from the bluetooth connection thread.
     * Handler has to be used as system is multithreaded.
     */
    public final Handler RXHandler = new Handler(Looper.getMainLooper()) {

        /**
         * @brief Handler for receiving messages from bluetooth thread.
         *
         * Receives data in a bundle, the string is then extracted and
         * an attempt to parse the line into a JSONObject takes places. \n
         * If successful it is then sent to the addTrip function to display
         * on the ListView.
         *
         * @param msg - Message holding the JSON string.
         */
        @Override
        public void handleMessage(Message msg) {

            Bundle msgData = msg.getData();
            String jsonString = msgData.getString("JSON");

            if (jsonString != null) {

                /* Try parse the string to JSON object, then pass to new trip function */
                try {
                    JSONObject tmpJSON = new JSONObject(jsonString);
                    addTrip(tmpJSON);

                } catch (JSONException e) {
                    /* Ignore line if exception */
                }
            }
        }
    };

    /**
     * @brief Listener used to identify when a trip has been pressed.
     */
    public final ListView.OnItemClickListener tripClickListener = new ListView.OnItemClickListener() {

        /**
         * @brief Loads a trip the user has specified.
         *
         * User has selected a trip via the ListView, method switches
         * to the statistic fragment and sends a message to logging device
         * to load the specified trip (via name).
         */
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
