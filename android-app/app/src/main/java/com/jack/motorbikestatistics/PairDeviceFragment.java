/**
 * @file PairDeviceFragment.java
 * @brief Fragment/Tab for connecting to the logging device.
 *
 * Implements Android's bluetooth API to discover, pair and connecting
 * to the logging device.
 *
 * Communication to the logging device is done via using Serial
 * data mode.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
 * @brief UI Class for discovering, pairing and connecting to
 * the logging device.
 */
public class PairDeviceFragment extends Fragment {
    /** @brief Request code for activating bluetooth. */
    private final static int REQUEST_BLUETOOTH = 1;
    /** @brief Status to change BTDeviceItem to when connected. */
    private final static String CONNECTED_STATUS = "connected";
    /** @brief Icon ID to use when device is not connected. */
    private final static int BT_DISABLED_ICON = R.drawable.ic_bluetooth_disabled_black_24px;

    /** @brief Check variable used to stop ListView from being re-populated. */
    private boolean firstRun = true;
    /** @brief Scan button, used for toggling discovery. */
    private ToggleButton btnScan;

    /* Bluetooth variables */
    /** @brief Mobile's bluetooth adapter. */
    private BluetoothAdapter btAdapter = null;

    /** @brief List of all devices, unpaired, paired & connected. */
    private ArrayList<BTDeviceItem> btDeviceList;
    /** @brief List of only paired devices. */
    private ArrayList<BTDeviceItem> btPairedList;
    /** @brief UI adapter for ListView that displays bluetooth devices. */
    private ArrayAdapter<BTDeviceItem> lvAdapter;
    /** @brief Applications connected logging device. */
    private BTDeviceItem btConnectedDevice = null;
    /** @brief Receiver class for when new device discovered. */
    private DiscoverReceiver btReceiver = null;


    /**
     * @brief Constructor for UI fragment.
     *
     * Get's the mobile's bluetooth adapter and sets
     * up our lists of used for holding devices.
     */
    public PairDeviceFragment()
    {
        /* Get bluetooth adapter for device & create device arrays */
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btDeviceList = new ArrayList<BTDeviceItem>();
        btPairedList = new ArrayList<BTDeviceItem>();
        btReceiver = new DiscoverReceiver();
    }

    /**
     * @brief Function called when fragment is shown on UI.
     *
     * Sets up the UI ListView and Buttons.
     * Add all paired devices for the bluetooth adapter to the ListView.
     *
     * @param inflater - Inflater used for displaying view.
     * @param container - Container that the view will be displayed on.
     * @param savedInstanceState - Last known state of this fragment.
     * @return View - The UI view of this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myView = inflater.inflate(R.layout.pairdevice_layout, container, false);

        /* Request needed privileges for bluetooth to work */
        getNeededPrivileges();

        /* Set our variables for UI buttons */
        btnScan = (ToggleButton)myView.findViewById(R.id.pairdevice_search);
        btnScan.setOnCheckedChangeListener(new DiscoverButtonListener());

        ListView lvDevices = (ListView)myView.findViewById(R.id.pairdevice_deviceList);
        lvDevices.setOnItemClickListener(new DeviceItemListener());

        lvAdapter = new BTDeviceListAdapter(getActivity(), R.layout.device_list_item, btDeviceList);
        lvDevices.setAdapter(lvAdapter);

        /* Check and set up bluetooth adapter */
        if (btAdapter == null)
        {
            Toast.makeText(getActivity().getApplicationContext(),
                    "This device has no bluetooth adapter", Toast.LENGTH_LONG).show();
        }
        else
        {
            /* Check to see if connected device still is connected */
            if (btConnectedDevice != null)
            {
                if (!btConnectedDevice.getConnection().isConnected() ||
                        !btConnectedDevice.getConnection().isRunning())
                {
                    btConnectedDevice = null;
                }
            }

            /* firstRun check to list from being re-populated */
            if (firstRun)
            {
                firstRun = false;

                /* Enable bluetooth adapter if disabled */
                if (!btAdapter.isEnabled())
                {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBT, REQUEST_BLUETOOTH);
                }

                while (!btAdapter.isEnabled())
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

    /**
     * @brief Getter for getting current connected device.
     * @return BTConnection - Bluetooth device (logging device).
     */
    public BTConnection getBTConnection()
    {
        if (btConnectedDevice != null) {
            return btConnectedDevice.getConnection();
        } else {
            return null;
        }
    }

    /**
     * @brief Prompts user for needed permissions of this application.
     *
     * Due to android using a permissions/access method this method
     * parses through each permission needed and prompts the user to accept.
     */
    private void getNeededPrivileges()
    {
        final int REQUEST_CODE = 5;

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

        if (!permsGranted)
        {
            ActivityCompat.requestPermissions(getActivity(), permsToRequest, REQUEST_CODE);
        }
    }

    /**
     * @brief Receiver for when a new device is discovered.
     */
    private class DiscoverReceiver extends BroadcastReceiver {
        /**
         * @brief When a BT device is found, adds the device to the ListView.
         *
         * @param context - Context that the application is running in.
         * @param intent - Intent holding the device object.
         */
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
    }

    /**
     * @brief Listener for when discovery button is pressed.
     */
    private class DiscoverButtonListener implements ToggleButton.OnCheckedChangeListener {

        /**
         * @brief Function for handling when discover toggle button pressed.
         *
         * If toggled on it bluetooth adapter is turned to discover mode.
         * If toggled off bluetooth adapter is turn off of disover mode.
         *
         * @param buttonView - Current view of the toggle button.
         * @param isChecked - The new state of the toggle button.
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            if (isChecked)
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
    }

    /**
     * @brief Listener for when a ListView item is pressed (to connect).
     */
    private class DeviceItemListener implements ListView.OnItemClickListener {

        /**
         * @brief Function called when user wants to connect to a device.
         *
         * Discovery is turned off to stop power wastage.
         * A new connection thread is then created which is responsible
         * for parsing receive, and transmission requests from other
         * fragments.
         *
         * @param parent - The parent ListView.
         * @param view - Current view of the ListItem.
         * @param position - Index of item pressed in ListView.
         * @param id - ID of the ListItem.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            BTDeviceItem deviceItem = (BTDeviceItem)parent.getItemAtPosition(position);

            /* Check if there is already a connection between devices */
            if ((deviceItem.getConnection() == null) ||
                    (!deviceItem.getConnection().isConnected()))
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

                    /* Create a new BTConnection item with no RX handler */
                    BTConnection newConn = new BTConnection(deviceItem.getDevice());

                    /* Execute the 'run' procedure in object in new thread */
                    Thread tmpThread = new Thread(newConn);
                    tmpThread.start();

                    /* Add set connection and add item to listview */
                    deviceItem.setConnection(newConn);
                    btConnectedDevice = deviceItem;

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
    }

}
