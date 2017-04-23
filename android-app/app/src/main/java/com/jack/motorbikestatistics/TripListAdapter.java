/**
 * @file TripListAdapter.java
 * @brief UI ListView adapter to display all saved trips.
 *
 * Implemented so that the trip list ListView can display relevant information
 * relating to the statistic such as name and file size.
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
 * @brief Adapter class used for displaying all trips.
 */
public class TripListAdapter extends ArrayAdapter<TripItem> {

    /** @brief Context that the ListView is operating in. */
    private Context context;
    /** @brief Resource ID for current layout. */
    private int layoutResourceId;
    /** @brief ArrayList of all trip items to display. */
    private ArrayList<TripItem> data;

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
    public TripListAdapter(Context cnt, int layoutResourceId, ArrayList<TripItem> data) {
        super(cnt, layoutResourceId, data);

        this.context = cnt;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    /**
     * @brief Class that holds all UI data to be displayed for each ListItem.
     */
    private class ViewHolder {
        TextView name;
        TextView fileSize;
    }

    /**
     * @brief Function for returning the view of each list item (TripItem).
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
            holder.name = (TextView)convertView.findViewById(R.id.triplist_name);
            holder.fileSize = (TextView)convertView.findViewById(R.id.triplist_size);
            convertView.setTag(holder);
        }
        else
        {
            /* If view already exists. */
            holder = (ViewHolder)convertView.getTag();
        }

        TripItem tripItem = getItem(position);

        /* Set our holder with current data of item */
        holder.name.setText(tripItem.getTripName());
        holder.fileSize.setText(Integer.toString(tripItem.getFileSize()));

        return convertView;
    }
}
