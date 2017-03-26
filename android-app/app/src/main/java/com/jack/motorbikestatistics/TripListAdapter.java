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
 * Created by Jack on 26-Mar-17.
 */

public class TripListAdapter extends ArrayAdapter<TripItem> {

    private Context context;
    private int layoutResourceId;
    private ArrayList<TripItem> data;


    public TripListAdapter(Context cnt, int layoutResourceId, ArrayList<TripItem> data) {
        super(cnt, layoutResourceId, data);

        this.context = cnt;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    private class ViewHolder {
        TextView name;
        TextView fileSize;
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
            holder.name = (TextView)convertView.findViewById(R.id.triplist_name);
            holder.fileSize = (TextView)convertView.findViewById(R.id.triplist_size);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        TripItem tripItem = getItem(position);

        /* Set our holder with current data of item */
        holder.name.setText(tripItem.getTripName());
        holder.fileSize.setText(Integer.toString(tripItem.getFileSize()));

        return convertView;
    }
}
