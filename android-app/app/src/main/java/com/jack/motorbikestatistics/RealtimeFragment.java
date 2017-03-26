package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
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
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Jack on 23-Jan-17.
 */

public class RealtimeFragment extends Fragment {

    private ArrayList<String> jsonList;

    private SetOfDataItems dataList;
    private ArrayAdapter<DataItem> lvAdapter;

    private static String receiveString = "";

    public RealtimeFragment() {
        jsonList = new ArrayList<String>();

        dataList = new SetOfDataItems();

        /* Set up our data items that we will want to log */
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.realtime_layout, container, false);

        /* Get the ListView via ID */
        ListView lvDataItems = (ListView) myView.findViewById(R.id.realtime_data_list);

        /* Inflate the header view for ListView */
        ViewGroup headerView = (ViewGroup) inflater.inflate(R.layout.data_list_header, lvDataItems, false);
        lvDataItems.addHeaderView(headerView);

        /* Create our new list adapter for our data list view */
        lvAdapter = new DataListAdapter(getActivity(), R.layout.data_list_item, dataList);
        lvDataItems.setAdapter(lvAdapter);

        /* Set our listeners for buttons */
        Button mapButton = (Button) myView.findViewById(R.id.realtime_show_map);
        mapButton.setOnClickListener(mapButtonListener);

        return myView;
    }

    private final Button.OnClickListener mapButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            intent.putExtra("JSONList", jsonList);
            startActivity(intent);
        }
    };

    private final void newData(JSONObject jsonData) {

        try {
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

            /*
             * Add json object to our list
             * so we can send it to other activities/fragments later
             */
            jsonList.add(jsonData.toString());
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
                        newData(tmpJSON);

                    } catch (JSONException e) {
                        /* Ignore line if exception */
                    }
                }

                receiveString = "";
            }
        }
    };
}
