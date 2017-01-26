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

/**
 * Created by Jack on 23-Jan-17.
 */

public class RealtimeFragment extends Fragment {

    private View myView;
    private TextView textStatus;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.realtime_layout, container, false);

        textStatus = (TextView)myView.findViewById(R.id.txtRTStatus);
        return myView;
    }

    public Handler getRecvHandler()
    {
        return btRecvHandler;
    }

    private final Handler btRecvHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            String data = (String)msg.obj;

            textStatus.setText(data);
        }
    };

}
