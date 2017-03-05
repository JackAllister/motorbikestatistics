package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

    private static String YAW_NAME = "Yaw";
    private static String PITCH_NAME = "Pitch";
    private static String ROLL_NAME = "Roll";
    private static String GPS_VALID_NAME = "GPS Valid";
    private static String SATELLITES_NAME = "Satellites";
    private static String LATITUDE_NAME = "Latitude";
    private static String LONGITUDE_NAME = "Longitude";
    private static String VELOCITY_NAME = "Velocity (MPH)";
    private static String ALTITUDE_NAME = "Altitude (FT)";
    private static String DATE_VALID_NAME = "Date Valid";
    private static String DATE_NAME = "Date";

    private View myView;
    private TextView textStatus;

    private ArrayList<DataItem> dataList;
    private ArrayAdapter<DataItem> lvAdapter;

    public RealtimeFragment()
    {
        dataList = new ArrayList<DataItem>();

//        /* Create our data objects that we are going to use for storage */
//
//        /* Add orientation based data */
//        dataList.add(new DataItem<Float>(YAW_NAME));
//        dataList.add(new DataItem<Float>(PITCH_NAME));
//        dataList.add(new DataItem<Float>(ROLL_NAME));
//
//        /* Add GPS based data */
//        dataList.add(new DataItem<Boolean>(GPS_VALID_NAME));
//        dataList.add(new DataItem<Integer>(SATELLITES_NAME));
//        dataList.add(new DataItem<Integer>(LATITUDE_NAME));
//        dataList.add(new DataItem<Integer>(LONGITUDE_NAME));
//        dataList.add(new DataItem<Integer>(VELOCITY_NAME));
//        dataList.add(new DataItem<Integer>(ALTITUDE_NAME));
//
//        /* DateTime based data */
//        dataList.add(new DataItem<Boolean>(DATE_VALID_NAME));
//        dataList.add(new DataItem<Integer>(DATE_NAME));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.realtime_layout, container, false);

        /* Get the ListView via ID */
        ListView lvDataItems = (ListView)myView.findViewById(R.id.realtime_data_list);

        /* Inflate the header view for ListView */
        ViewGroup headerView = (ViewGroup)inflater.inflate(R.layout.data_list_header, lvDataItems, false);
        lvDataItems.addHeaderView(headerView);

        /* Create our new list adapter for our data list view */
        lvAdapter = new DataListAdapter(getActivity(), R.layout.data_list_item, dataList);
        lvDataItems.setAdapter(lvAdapter);

        textStatus = (TextView)myView.findViewById(R.id.realtime_status);
        return myView;
    }

    private boolean updateItem(String name, Object value)
    {
        boolean result = false;

        DataItem found = findItemByName(name);
        if (found != null)
        {
            found.setValue(value);
            result = true;
        }

        return result;
    }

    @Nullable
    private DataItem findItemByName(String name)
    {
        for (int i = 0; i < dataList.size(); i++)
        {
            if (dataList.get(i).getName().equals(name))
                return dataList.get(i);
        }

        return null;
    }

    private boolean iterateJSONObject(JSONObject jsonData)
    {
        boolean result = true;

        Iterator<String> iter = jsonData.keys();
        while (iter.hasNext())
        {
            String key = iter.next();

            try
            {
                Object value = jsonData.get(key);

                /* If the received object itself is a JSON object we iterate that that too */
                if (value.getClass() == JSONObject.class)
                {
                    JSONObject nestedObject = jsonData.getJSONObject(key);
                    iterateJSONObject(nestedObject);
                }
                else
                {
                    /* Add or update our value into a dataItem here */
                    DataItem foundItem = findItemByName(key);
                    if (foundItem == null)
                    {
                        /* Add our new DataItem here */
                        DataItem newItem = new DataItem(key, value);
                        dataList.add(newItem);
                    }
                    else
                    {
                        /* Update an existing object here */
                        foundItem.setValue(value);
                    }
                }

            } catch (JSONException e)
            {
                result = false;
            }
        }

        return result;
    }

    public final void newData(JSONObject jsonData) {
        /*
         * If successfully iterates through the JSON object
         * while updating data update list view
         */
        if (iterateJSONObject(jsonData))
            lvAdapter.notifyDataSetChanged();
    }

}
