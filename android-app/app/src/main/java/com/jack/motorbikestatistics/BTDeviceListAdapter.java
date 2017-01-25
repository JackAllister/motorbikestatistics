package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import java.util.List;
import android.bluetooth.BluetoothAdapter;



/**
 * Created by Jack on 25-Jan-17.
 */

public class BTDeviceListAdapter extends ArrayAdapter<BTDeviceItem> {

    private Context context;
    private BluetoothAdapter btAdapter;

    public BTDeviceListAdapter(Context cnt, List items, BluetoothAdapter adapter) {
        super(cnt, android.R.layout.simple_list_item_1, items);
        this.context = cnt;
        this.btAdapter = adapter;
    }

    private class ViewHolder {
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
            convertView = inflater.inflate(R.layout.device_list_item, null);

            holder = new ViewHolder();
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
        holder.name.setText(btItem.getName());
        holder.address.setText(btItem.getAddress());
        holder.status.setText(Boolean.toString(btItem.isConnected()));

        return convertView;
    }
}
