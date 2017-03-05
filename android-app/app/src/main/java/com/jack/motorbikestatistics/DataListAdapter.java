package com.jack.motorbikestatistics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jack on 05-Mar-17.
 */

public class DataListAdapter extends ArrayAdapter<DataItem> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<DataItem> data;

    public DataListAdapter(Context cnt, int layoutResourceId, ArrayList<DataItem> data) {
        super(cnt, layoutResourceId, data);

        this.context = cnt;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    private class ViewHolder {
        TextView name;
        TextView value;
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
            holder.name = (TextView)convertView.findViewById(R.id.datalist_name);
            holder.value = (TextView)convertView.findViewById(R.id.datalist_value);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        DataItem dataItem = getItem(position);

        /* Set our holder with current data of item */
        holder.name.setText(dataItem.getName());
        holder.value.setText(dataItem.getValue());

        return convertView;
    }
}
