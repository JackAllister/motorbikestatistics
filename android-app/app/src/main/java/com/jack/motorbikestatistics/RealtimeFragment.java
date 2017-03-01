package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jack on 23-Jan-17.
 */

public class RealtimeFragment extends Fragment {

    private View myView;
    private TextView textStatus;

    private int counter = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.realtime_layout, container, false);

        textStatus = (TextView)myView.findViewById(R.id.txtRTStatus);
        return myView;
    }

    public Handler getHandler()
    {
        return recvHandler;
    }

    private final Handler recvHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            textStatus.setText((String)msg.obj);
            counter++;

//            JSONObject jsonObject = (JSONObject)msg.obj;
//
//            try {
//                textStatus.setText(jsonObject.getString("Test"));
//            } catch (JSONException e)
//            {
//
//            }
        }
    };
}
