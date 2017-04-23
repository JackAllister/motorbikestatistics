/**
 * @file BTDeviceListAdapter.java
 * @brief UI ListView adapter to display bluetooth devices.
 *
 * Implemented so that the device ListView can display relevant information
 * relating to the BluetoothDevice's that are available to pair, connect.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import android.bluetooth.BluetoothAdapter;

/**
 * @brief Adapter class used for displaying bluetooth devices.
 */
public class BTDeviceListAdapter extends ArrayAdapter<BTDeviceItem> {

    /** @brief Resource ID for current layout. */
    private int layoutResourceId;
    /** @brief Context that the ListView is operating in. */
    private Context context;
    /** @brief ArrayList of all bluetooth device items to display. */
    private ArrayList<BTDeviceItem> data;

    /**
     * @brief Constructor for the ListView adapter.
     *
     * Calls the constructor of the superclass as well as setting
     * other relevant information needed.
     *
     * @param cnt - Context of the adapter to be operating in.
     * @param layoutResourceId - Resource ID for current layout.
     * @param data - ArrayList of devices to display in ListView.
     */
    public BTDeviceListAdapter(Context cnt, int layoutResourceId, ArrayList<BTDeviceItem> data) {
        super(cnt, layoutResourceId, data);
        this.context = cnt;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    /**
     * @brief Class that holds all data displayed for each ListItem.
     */
    private class ViewHolder {
        ImageView imageStatus;
        TextView name;
        TextView address;
        TextView status;
    }

    /**
     * @brief Function for returning the view of each list item (BTDeviceItem).
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
            /* Create new view via inflater as it does not exist. */
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, parent, false);

            /* Create holder that will contain information to display. */
            holder = new ViewHolder();
            holder.imageStatus = (ImageView)convertView.findViewById(R.id.imageListStatus);
            holder.name = (TextView)convertView.findViewById(R.id.textListName);
            holder.address = (TextView)convertView.findViewById(R.id.textListAddress);
            holder.status = (TextView)convertView.findViewById(R.id.textListStatus);
            convertView.setTag(holder);
        }
        else
        {
            /* Get current holder to use instead of creating new one. */
            holder = (ViewHolder)convertView.getTag();
        }

        /* Get BTDeviceItem for specified item and update holder info. */
        BTDeviceItem btItem = getItem(position);
        holder.imageStatus.setImageResource(btItem.getIconID());
        holder.name.setText(btItem.getDevice().getName());
        holder.address.setText(btItem.getDevice().getAddress());
        holder.status.setText(btItem.getStatus());

        return convertView;
    }
}
