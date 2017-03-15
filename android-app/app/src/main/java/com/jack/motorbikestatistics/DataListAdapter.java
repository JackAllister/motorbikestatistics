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
        TextView current;
        TextView average;
        TextView minimum;
        TextView maximum;
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
            holder.current = (TextView)convertView.findViewById(R.id.datalist_current);
            holder.average = (TextView)convertView.findViewById(R.id.datalist_average);
            holder.minimum = (TextView)convertView.findViewById(R.id.datalist_minimum);
            holder.maximum = (TextView)convertView.findViewById(R.id.datalist_maximum);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        DataItem dataItem = getItem(position);

        /* Set our holder with current data of item */
        holder.name.setText(dataItem.getName());

        Object current = dataItem.getCurrent();
        if (current != null) {
            DecimalFormat df = new DecimalFormat("#.####");
            df.setRoundingMode(RoundingMode.CEILING);

            if (current instanceof Double) {
                holder.current.setText(df.format(current));
            } else {
                holder.current.setText(current.toString());
            }

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
