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
 * Created by Jack on 25-Jan-17.
 */

public class BTDeviceListAdapter extends ArrayAdapter<BTDeviceItem> {

    private int layoutResourceId;
    private Context context;
    private BluetoothAdapter btAdapter;
    private ArrayList<BTDeviceItem> data;

    public BTDeviceListAdapter(Context cnt, int layoutResourceId, ArrayList<BTDeviceItem> data, BluetoothAdapter adapter) {
        super(cnt, layoutResourceId, data);
        this.context = cnt;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
        this.btAdapter = adapter;
    }

    private class ViewHolder {
        ImageView imageStatus;
        TextView name;
        TextView address;
        TextView status;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.imageStatus = (ImageView)convertView.findViewById(R.id.imageListStatus);
            holder.name = (TextView)convertView.findViewById(R.id.textListName);
            holder.address = (TextView)convertView.findViewById(R.id.textListAddress);
            holder.status = (TextView)convertView.findViewById(R.id.textListStatus);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        BTDeviceItem btItem = getItem(position);
        holder.imageStatus.setImageResource(btItem.getIconID());
        holder.name.setText(btItem.getDevice().getName());
        holder.address.setText(btItem.getDevice().getAddress());
        holder.status.setText(btItem.getStatus());

        return convertView;
    }
}
