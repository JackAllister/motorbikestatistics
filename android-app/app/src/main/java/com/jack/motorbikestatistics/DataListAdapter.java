/**
 * @file DataListAdapter.java
 * @brief UI ListView adapter to display statistics.
 *
 * Implemented so that the statistics ListView can display relevant information
 * relating to the statistic such as name, value, average, min & max.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @brief Adapter class used for displaying statistics.
 */
public class DataListAdapter extends ArrayAdapter<DataItem> {

    /** @brief Context that the ListView is operating in. */
    private Context context;
    /** @brief Resource ID for current layout. */
    private int layoutResourceId;
    /** @brief ArrayList of all statistic items to display. */
    private ArrayList<DataItem> data;

    /**
     * @brief Constructor for the ListView adapter.
     *
     * Calls the constructor of the superclass as well as setting
     * other relevant information needed.
     *
     * @param cnt - Context of the adapter to be operating in.
     * @param layoutResourceId - Resource ID for current layout.
     * @param data - ArrayList of statistics to display in ListView.
     */
    public DataListAdapter(Context cnt, int layoutResourceId, ArrayList<DataItem> data) {
        super(cnt, layoutResourceId, data);

        this.context = cnt;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    /**
     * @brief Class that holds all data displayed for each ListItem.
     */
    private class ViewHolder {
        TextView name;
        TextView current;
        TextView average;
        TextView minimum;
        TextView maximum;
    }

    /**
     * @brief Function for returning the view of each list item (DataItem).
     *
     * If a view for selected item has not been created inflater initialises
     * it. A holder is then used to hold all the information that will be
     * displayed on the UI to the user.
     *
     * @param position - Index of item in array to use/reference to.
     * @param convertView - View to be used for specified item.
     * @param parent - Object where the created view will be placed on.
     * @return View - The result view of item with updated/current information.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null)
        {
            /* If view does not already exist. */
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.datalist_name);
            holder.current = (TextView)convertView.findViewById(R.id.datalist_current);
            holder.average = (TextView)convertView.findViewById(R.id.datalist_average);
            holder.minimum = (TextView)convertView.findViewById(R.id.datalist_minimum);
            holder.maximum = (TextView)convertView.findViewById(R.id.datalist_maximum);
            convertView.setTag(holder);
        }
        else
        {
            /* If view already exists. */
            holder = (ViewHolder)convertView.getTag();
        }

        DataItem dataItem = getItem(position);

        /* Set our holder with current data of item */
        holder.name.setText(dataItem.getName());

        Object current = dataItem.getCurrent();
        if (current != null) {
            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);

            /* To aid aesthetics rounding is used. */
            if (current instanceof Double) {
                holder.current.setText(df.format(current));
            } else {
                holder.current.setText(current.toString());
            }

            /*
             * Displays added functionality if available.
             * Not all statistics need it, for example averaging of LAT/LNG.
             */
            if (dataItem.getEnabledAvgMinMax()) {
                holder.average.setText(df.format(dataItem.getAverage()));
                holder.minimum.setText(df.format(dataItem.getMinimum()));
                holder.maximum.setText(df.format(dataItem.getMaximum()));
            } else {
                holder.average.setText("N/A");
                holder.minimum.setText("N/A");
                holder.maximum.setText("N/A");
            }
        }

        return convertView;
    }
}
