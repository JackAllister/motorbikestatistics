/**
 * @file RealtimeFragment.java
 * @brief Fragment/Tab for viewing streamed statistics.
 *
 * Implements RXHandler from bluetooth device to receive statistics.
 * Data is then displayed in a ListView as well as option to view via
 * Google Maps.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @brief UI Class for viewing data sent from the logging device.
 */
public class RealtimeFragment extends Fragment {
    /** @brief String for new line parsing. */
    private final static String NEW_LINE = "\r\n";

    /** @brief TextView to show amount of logs received. */
    private TextView textStatus;
    /** @brief Array holding each statistic that device is measuring. */
    private SetOfDataItems dataList;
    /** @brief Adapter used for displaying statistics in the ListView. */
    private ArrayAdapter<DataItem> lvAdapter;

    /**
     * @brief Constructor for UI fragment.
     *
     * Creates our initial data items that we are going to log.
     * Setting whether extended functionality is needed for each data
     * item.
     */
    public RealtimeFragment() {

        dataList = new SetOfDataItems();

        /* Set up our data items that we will want to log & show on ListView */
        dataList.add(new DataItem<Double>("Yaw", true));
        dataList.add(new DataItem<Double>("Pitch", true));
        dataList.add(new DataItem<Double>("Roll", true));
        dataList.add(new DataItem<Boolean>("GPS Valid", false));
        dataList.add(new DataItem<Integer>("Satellites", false));
        dataList.add(new DataItem<Double>("Latitude", false));
        dataList.add(new DataItem<Double>("Longitude", false));
        dataList.add(new DataItem<Double>("Velocity (MPH)", true));
        dataList.add(new DataItem<Double>("Altitude (FT)", true));
        dataList.add(new DataItem<Boolean>("Date Valid", false));
        dataList.add(new DataItem<String>("Date", false));
        dataList.add(new DataItem<String>("Time", false));

        /* Clear our tripData handler in singleton class */
        ArrayList<JSONObject> tripData = JSONHandlerSingleton.getInstance().tripData;
        tripData.clear();
    }

    /**
     * @brief Function called when fragment is shown on UI.
     *
     * Sets up the UI ListView and Buttons.
     *
     * @param inflater - Inflater used for displaying view.
     * @param container - Container that the view will be displayed on.
     * @param savedInstanceState - Last known state of this fragment.
     * @return View - The UI view of this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.realtime_layout, container, false);

        textStatus = (TextView)myView.findViewById(R.id.realtime_status);

        /* Get the ListView via ID */
        ListView lvDataItems = (ListView) myView.findViewById(R.id.realtime_data_list);

        /* Inflate the header view for ListView */
        ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.data_list_header, lvDataItems, false);
        lvDataItems.addHeaderView(headerView);

        /* Create our new list adapter for our data list view */
        lvAdapter = new DataListAdapter(getActivity(), R.layout.data_list_item, dataList);
        lvDataItems.setAdapter(lvAdapter);

        /* Set our listeners for buttons */
        FloatingActionButton mapButton = (FloatingActionButton) myView.findViewById(R.id.realtime_show_map);
        mapButton.setOnClickListener(new MapButtonListener());

        return myView;
    }

    /**
     * @brief Function for adding new statistics when received via bluetooth.
     *
     * Attempts to break the initial JSON object into it's child objects
     * and then retreive the data from these child nodes.
     *
     * @param jsonData - Received JSONObject from receive handler.
     */
    private final void newData(JSONObject jsonData) {

        try {
            /*
             * Add parent object to our singleton trip data list.
             * This is so that other activities/fragments can access it.
             */
            ArrayList<JSONObject> tripData = JSONHandlerSingleton.getInstance().tripData;
            tripData.add(jsonData);

            /* Get the child JSON objects from parents. */
            JSONObject orientObject = jsonData.getJSONObject("orientation");
            JSONObject gpsObject = jsonData.getJSONObject("gps");
            JSONObject timeObject = jsonData.getJSONObject("time");

            /* Update our data items */
            dataList.getItemByName("Yaw").setCurrent(orientObject.getDouble("yaw"));
            dataList.getItemByName("Pitch").setCurrent(orientObject.getDouble("pitch"));
            dataList.getItemByName("Roll").setCurrent(orientObject.getDouble("roll"));

            /* Add GPS based data to */
            dataList.getItemByName("GPS Valid").setCurrent(gpsObject.getBoolean("gps_valid"));
            dataList.getItemByName("Satellites").setCurrent(gpsObject.getInt("available"));
            dataList.getItemByName("Latitude").setCurrent(gpsObject.getDouble("lat"));
            dataList.getItemByName("Longitude").setCurrent(gpsObject.getDouble("lng"));
            dataList.getItemByName("Velocity (MPH)").setCurrent(gpsObject.getDouble("vel_mph"));
            dataList.getItemByName("Altitude (FT)").setCurrent(gpsObject.getDouble("alt_ft"));

            /* DateTime based data */
            dataList.getItemByName("Date Valid").setCurrent(timeObject.getBoolean("time_valid"));

            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Calendar.YEAR, timeObject.getInt("year"));
            cal.set(Calendar.MONTH, timeObject.getInt("month"));
            cal.set(Calendar.DATE, timeObject.getInt("day"));

            cal.set(Calendar.HOUR, timeObject.getInt("hour"));
            cal.set(Calendar.MINUTE, timeObject.getInt("minute"));
            cal.set(Calendar.SECOND, timeObject.getInt("second"));
            cal.set(Calendar.MILLISECOND, timeObject.getInt("centiseconds") * 10);

            /* Create format for date and times then add to list */
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
            dataList.getItemByName("Date").setCurrent(dateFormat.format(cal.getTime()));

            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SS");
            dataList.getItemByName("Time").setCurrent(timeFormat.format(cal.getTime()));

            lvAdapter.notifyDataSetChanged();

            textStatus.setText("Reading count: " + Integer.toString(tripData.size()));
        } catch (JSONException e) {
            /* Do nothing */
        }
    }

    /**
     * @brief Listener for starting a map activity when button pressed.
     */
    private class MapButtonListener implements Button.OnClickListener {

      /**
       * @brief Function for handling when map button pressed.
       *
       * Created a new intent to start our map activity.
       * Serialised statistics are then added as a bundle in the intent.
       *
       * @param v - Current view of the button.
       */
      @Override
      public void onClick(View v) {
          Intent intent = new Intent(getActivity(), MapsActivity.class);
          startActivity(intent);
      }
    }

    /**
     * @brief Handler used for receiving statistics via bluetooth.
     *
     * Receives data in a bundle passed from the bluetooth connection thread.
     * This is due to multithreading as safe data exchange between threads
     * has to be done via messages.
     * Attempts to parse the data into a JSON object, if successful
     * this data is then passed to our JSON adding procedure.
     */
    public final Handler RXHandler = new Handler(Looper.getMainLooper()) {

        /**
         * @brief Method that receives message containing JSON string.
         *
         * Attempts to turn into JSONObject and then passes to
         * procedure that adds to UI.
         *
         * @param msg - Message holding JSON string.
         */
        @Override
        public void handleMessage(Message msg) {

            Bundle msgData = msg.getData();
            String jsonString = msgData.getString("JSON");

            if (jsonString != null) {

                /* Try parse the string into a JSON object & send to new data function */
                try {
                    JSONObject tmpJSON = new JSONObject(jsonString);
                    newData(tmpJSON);

                } catch (JSONException e) {
                    /* Ignore line if exception */
                }
            }
        }
    };
}
