package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
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

    private ArrayList<DataItem> dataList;
    private ArrayAdapter<DataItem> lvAdapter;

    public RealtimeFragment()
    {
        jsonList = new ArrayList<String>();
        dataList = new ArrayList<DataItem>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.realtime_layout, container, false);

        /* Get the ListView via ID */
        ListView lvDataItems = (ListView)myView.findViewById(R.id.realtime_data_list);

        /* Inflate the header view for ListView */
        ViewGroup headerView = (ViewGroup)inflater.inflate(R.layout.data_list_header, lvDataItems, false);
        lvDataItems.addHeaderView(headerView);

        /* Create our new list adapter for our data list view */
        lvAdapter = new DataListAdapter(getActivity(), R.layout.data_list_item, dataList);
        lvDataItems.setAdapter(lvAdapter);

        /* Set our listeners for buttons */
        Button mapButton = (Button)myView.findViewById(R.id.realtime_show_map);
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

    public final void newData(JSONObject jsonData) {

        try
        {
            dataList.clear();

            JSONObject orientObject = jsonData.getJSONObject("orientation");
            JSONObject gpsObject = jsonData.getJSONObject("gps");
            JSONObject timeObject = jsonData.getJSONObject("time");

            /* Add orientation based data */
            dataList.add(new DataItem<Double>("Yaw", orientObject.getDouble("yaw")));
            dataList.add(new DataItem<Double>("Pitch", orientObject.getDouble("pitch")));
            dataList.add(new DataItem<Double>("Roll", orientObject.getDouble("roll")));

            /* Add GPS based data to */
            dataList.add(new DataItem<Boolean>("GPS Valid", gpsObject.getBoolean("gps_valid")));
            dataList.add(new DataItem<Integer>("Satellites", gpsObject.getInt("available")));
            dataList.add(new DataItem<Double>("Latitude", gpsObject.getDouble("lat")));
            dataList.add(new DataItem<Double>("Longitude", gpsObject.getDouble("lng")));
            dataList.add(new DataItem<Double>("Velocity (MPH)", gpsObject.getDouble("vel_mph")));
            dataList.add(new DataItem<Double>("Altitude (FT)", gpsObject.getDouble("alt_ft")));

            /* DateTime based data */
            dataList.add(new DataItem<Boolean>("Date Valid", timeObject.getBoolean("time_valid")));

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
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SS");
            dataList.add(new DataItem<Date>("Date", cal.getTime()));

            lvAdapter.notifyDataSetChanged();

            /*
             * Add json object to our list
             * so we can send it to other activities/fragments later
             */
            jsonList.add(jsonData.toString());
        }
        catch (JSONException e)
        {
            /* Do nothing */
        }
    }

}
