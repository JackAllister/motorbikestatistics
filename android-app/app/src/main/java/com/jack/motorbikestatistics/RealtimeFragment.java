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

/**
 * Created by Jack on 23-Jan-17.
 */

public class RealtimeFragment extends Fragment {

    private View myView;
    private TextView textStatus;

    private ArrayList<DataItem> dataList;
    private ArrayAdapter<DataItem> lvAdapter;

    public RealtimeFragment()
    {
        dataList = new ArrayList<DataItem>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.realtime_layout, container, false);

        /* Get the listview via ID */
        ListView lvDataItems = (ListView)myView.findViewById(R.id.realtime_data_list);

        /* Create our new list adapter for our data list view */
        lvAdapter = new DataListAdapter(getActivity(), R.layout.data_list_item, dataList);
        lvDataItems.setAdapter(lvAdapter);

        textStatus = (TextView)myView.findViewById(R.id.realtime_status);
        return myView;
    }

    public final void newData(JSONObject jsonData) {

        try
        {
            JSONObject orientObject = jsonData.getJSONObject("orientation");
            JSONObject gpsObject = jsonData.getJSONObject("gps");
            JSONObject timeObject = jsonData.getJSONObject("time");

            dataList.clear();

            /* Add orientation based data */
            dataList.add(new DataItem("Yaw", orientObject.getString("yaw")));
            dataList.add(new DataItem("Pitch", orientObject.getString("pitch")));
            dataList.add(new DataItem("Roll", orientObject.getString("roll")));

            /* Add GPS based data */
            dataList.add(new DataItem("GPS Valid", Boolean.toString(gpsObject.getBoolean("valid"))));
            dataList.add(new DataItem("Satellites", gpsObject.getString("available")));
            dataList.add(new DataItem("Latitude", gpsObject.getString("lat")));
            dataList.add(new DataItem("Longitude", gpsObject.getString("lng")));
            dataList.add(new DataItem("Velocity (MPH)", gpsObject.getString("vel_mph")));
            dataList.add(new DataItem("Altitude (FT)", gpsObject.getString("alt_ft")));

            /* DateTime based data */
            dataList.add(new DataItem("Date Valid", Boolean.toString(timeObject.getBoolean("valid"))));

            Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Calendar.YEAR, timeObject.getInt("year"));
            cal.set(Calendar.MONTH, timeObject.getInt("month"));
            cal.set(Calendar.DATE, timeObject.getInt("day"));

            cal.set(Calendar.HOUR, timeObject.getInt("hour"));
            cal.set(Calendar.MINUTE, timeObject.getInt("minute"));
            cal.set(Calendar.SECOND, timeObject.getInt("second"));
            cal.set(Calendar.MILLISECOND, timeObject.getInt("centiseconds") * 10);

            /* Create format for date and add to list */
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SS");
            dataList.add(new DataItem("Date", dateFormat.format(cal.getTime())));
            lvAdapter.notifyDataSetChanged();

        }
        catch (JSONException e)
        {
            /* Do nothing */
        }
    }

}
