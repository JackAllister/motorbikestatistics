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
    private static int REQUEST_BLUETOOTH = 1;

    private View myView;
    private Button btnScan;
    private Button btnPair;
    private ListView lvDevices;

    /* Bluetooth variables */
    private BluetoothAdapter btAdapter = null;
    private Set<BluetoothDevice> pairedDevices;

    private ArrayList<BTDeviceItem> btDeviceItemList;
    private ArrayAdapter<BTDeviceItem> lvAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.pairdevice_layout, container, false);

        /* Set our variables for UI buttons */
        btnScan = (Button)myView.findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        btnPair = (Button)myView.findViewById(R.id.btnPair);
        btnPair.setOnClickListener(this);
        lvDevices = (ListView)myView.findViewById(R.id.deviceList);

        /* Check and set up bluetooth adapter */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null)
        {
            Toast.makeText(getActivity().getApplicationContext(),
                    "This device has no bluetooth adapter", Toast.LENGTH_LONG).show();
        }
        else
        {
            /* Enable bluetooth adapter if disabled */
            if (btAdapter.isEnabled() == false)
            {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }

            btDeviceItemList = new ArrayList<BTDeviceItem>();

            pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0)
            {
                for (BluetoothDevice device : pairedDevices)
                {
                    BTDeviceItem newDevice = new BTDeviceItem(device.getName(), device.getAddress(), false);
                    btDeviceItemList.add(newDevice);
                }
            }
            else if (pairedDevices.size() == 0)
            {
                /* If device has no paired devices already */
                btDeviceItemList.add(new BTDeviceItem("No Devices", "", false));
            }

            lvAdapter = new BTDeviceListAdapter(getActivity(), btDeviceItemList, btAdapter);
        }

        return myView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnScan:
            {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Button pressed", Toast.LENGTH_LONG).show();
                break;
            }

            case R.id.btnPair:
            {
                break;
            }
        }
    }

}
