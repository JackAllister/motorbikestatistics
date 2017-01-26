package com.jack.motorbikestatistics;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.ListView;

import java.io.IOException;
import java.util.Set;
import java.util.ArrayList;

import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by Jack on 25-Jan-17.
 */

public class PairDeviceFragment extends Fragment {
    private final static int REQUEST_BLUETOOTH = 1;
    private final static String CONNECTED_STATUS = "connected";
    private final static int BT_DISABLED_ICON = R.drawable.ic_bluetooth_disabled_black_24px;

    private boolean firstRun = true;

    private View myView;
    private ToggleButton btnScan;
    private ListView lvDevices;

    /* Bluetooth variables */
    private BluetoothAdapter btAdapter = null;

    private ArrayList<BTDeviceItem> btDeviceList;
    private ArrayList<BTDeviceItem> btPairedList;
    private ArrayAdapter<BTDeviceItem> lvAdapter;

    private BTDeviceItem btConnectedDevice = null;


    public PairDeviceFragment() {
        /* Get bluetooth adapter for device & create device arrays */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDeviceList = new ArrayList<BTDeviceItem>();
        btPairedList = new ArrayList<BTDeviceItem>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.pairdevice_layout, container, false);

        /* Request needed privileges for bluetooth to work */
        getNeededPrivileges();

        /* Set our variables for UI buttons */
        btnScan = (ToggleButton)myView.findViewById(R.id.btnScan);
        btnScan.setOnCheckedChangeListener(toggleScanListener);

        lvDevices = (ListView)myView.findViewById(R.id.deviceList);
        lvDevices.setOnItemClickListener(listItemListener);

        lvAdapter = new BTDeviceListAdapter(getActivity(), R.layout.device_list_item, btDeviceList, btAdapter);
        lvDevices.setAdapter(lvAdapter);

        /* Check and set up bluetooth adapter */
        if (btAdapter == null)
        {
            Toast.makeText(getActivity().getApplicationContext(),
                    "This device has no bluetooth adapter", Toast.LENGTH_LONG).show();
        }
        else
        {
            /* firstRun check to list from being re-populated */
            if (firstRun == true)
            {
                firstRun = false;

                /* Enable bluetooth adapter if disabled */
                if (btAdapter.isEnabled() == false)
                {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_BLUETOOTH);
                }

                while (btAdapter.isEnabled() == false)
                {
                    /* Wait for BT to be enabled */
                }

                /* Add all paired devices to list */
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if (pairedDevices.size() > 0)
                {
                    for (BluetoothDevice device : pairedDevices)
                    {
                        BTDeviceItem newDevice =
                            new BTDeviceItem(device, "paired", BT_DISABLED_ICON);
                        btPairedList.add(newDevice);
                    }
                }
                btDeviceList.addAll(btPairedList);
            }

        }

        return myView;
    }

    private void getNeededPrivileges()
    {
        final int REQUEST_CODE = 5;

        boolean result = true;
        boolean permsGranted = true;
        String[] permsToRequest =
                {
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };

        for (String permission: permsToRequest)
        {
            permsGranted &= (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED);
        }

        if (permsGranted == false)
        {
            ActivityCompat.requestPermissions(getActivity(), permsToRequest, REQUEST_CODE);
        }
    }

    public final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            /* Check to see if found device */
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                /* Create new device item and add to list */
                BTDeviceItem newDevice = new BTDeviceItem(device, "unpaired", BT_DISABLED_ICON);
                lvAdapter.add(newDevice);
                lvAdapter.notifyDataSetChanged();
            }
        }
    };

    public final ToggleButton.OnCheckedChangeListener toggleScanListener = new ToggleButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            if (isChecked == true)
            {
                /* Clear listview, add previous paired items, start discovery */
                lvAdapter.clear();
                lvAdapter.addAll(btPairedList);

                if (btConnectedDevice != null)
                    lvAdapter.add(btConnectedDevice);

                getActivity().registerReceiver(btReceiver, filter);
                btAdapter.startDiscovery();
            }
            else
            {
                /* Stop searching for new devices */
                getActivity().unregisterReceiver(btReceiver);
                btAdapter.cancelDiscovery();
            }
        }
    };

    public final ListView.OnItemClickListener listItemListener  = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            BTDeviceItem deviceItem = (BTDeviceItem)parent.getItemAtPosition(position);

            /* Check if there is already a connection between devices */
            if ((deviceItem.getConnection() == null) ||
                    (deviceItem.getConnection().isConnected() == false))
            {
                if (btAdapter.isDiscovering())
                {
                    /* Cancel discovery is still enabled */
                    btnScan.setChecked(false);
                    btAdapter.cancelDiscovery();
                }

                try
                {
                    Toast.makeText(parent.getContext(), "Connecting to: " +
                            deviceItem.getDevice().getName(), Toast.LENGTH_SHORT).show();

                    BTConnection newConn = new BTConnection(deviceItem.getDevice());

                    /* Check to see if connected */
                    Toast.makeText(parent.getContext(), "Connected: " +
                            Boolean.toString(newConn.isConnected()), Toast.LENGTH_SHORT).show();

                    /* Check to see if running */
                    Toast.makeText(parent.getContext(), "Running: " +
                            Boolean.toString(newConn.isRunning()), Toast.LENGTH_SHORT).show();

                    /* Add set connection and add item to listview */
                    deviceItem.setConnection(newConn);

                    /* Update status and icon in list view */
                    deviceItem.setIconID(R.drawable.ic_bluetooth_connected_black_24px);
                    deviceItem.setStatus(CONNECTED_STATUS);
                    lvAdapter.notifyDataSetChanged();
                }
                catch (IOException e)
                {
                    Toast.makeText(parent.getContext(), "Unable to connect: " +
                            e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

}
