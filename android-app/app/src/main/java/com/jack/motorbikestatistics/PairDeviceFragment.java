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
    private static int REQUEST_BLUETOOTH = 1;

    private View myView;
    private ToggleButton btnScan;
    private ListView lvDevices;

    /* Bluetooth variables */
    private BluetoothAdapter btAdapter = null;

    private ArrayList<BTDeviceItem> btPairedItemList;
    private ArrayList<BTDeviceItem> btDeviceItemList;
    private ArrayAdapter<BTDeviceItem> lvAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.pairdevice_layout, container, false);

        getNeededPrivileges();

        /* Get bluetooth adapter for device & create device array */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btPairedItemList = new ArrayList<BTDeviceItem>();
        btDeviceItemList = new ArrayList<BTDeviceItem>();

        /* Set our variables for UI buttons */
        btnScan = (ToggleButton)myView.findViewById(R.id.btnScan);
        btnScan.setOnCheckedChangeListener(toggleScanListener);

        lvDevices = (ListView)myView.findViewById(R.id.deviceList);
        lvDevices.setOnItemClickListener(listItemListener);

        lvAdapter = new BTDeviceListAdapter(getActivity(), R.layout.device_list_item, btDeviceItemList, btAdapter);
        lvDevices.setAdapter(lvAdapter);

        /* Check and set up bluetooth adapter */
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
                            new BTDeviceItem(device.getName(), device.getAddress(), "paired", false);
                    btPairedItemList.add(newDevice);
                }
            }
            else if (pairedDevices.size() == 0)
            {
                /* If device has no paired devices already */
                btPairedItemList.add(new BTDeviceItem("No Devices", "", "", false));
            }
            btDeviceItemList.addAll(btPairedItemList);

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
                BTDeviceItem newDevice = new BTDeviceItem(device.getName(),
                                                device.getAddress(), "unpaired", false);
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
                lvAdapter.addAll(btPairedItemList);
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

            BTDeviceItem device = (BTDeviceItem)parent.getItemAtPosition(position);
            Toast.makeText(parent.getContext(), device.getName(), Toast.LENGTH_SHORT).show();
        }
    };

}
