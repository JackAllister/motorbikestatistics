package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import java.util.Set;
import java.util.ArrayList;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;


/**
 * Created by Jack on 25-Jan-17.
 */

public class PairDeviceFragment extends Fragment
        implements View.OnClickListener {

    private View myView;
    private Button btnPair;
    private ListView deviceList;

    /* Bluetooth variables */
    private BluetoothAdapter btAdapter = null;
    private Set pairedDevices;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.pairdevice_layout, container, false);

        /* Set our variables for UI buttons */
        btnPair = (Button)myView.findViewById(R.id.btnPair);

        deviceList = (ListView)myView.findViewById(R.id.deviceList);

        /* Check and set up bluetooth adapter */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            Toast.makeText(getActivity().getApplicationContext(),
                    "This device has no bluetooth adapter", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (btAdapter.isEnabled() == false)
            {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, 1);
            }
        }

        return myView;
    }

    @Override
    public void onClick(View v) {
        
    }
}
