package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Jack on 26-Jan-17.
 */

public class BTConnection implements Runnable {

    private static final String TAG = "BTConnection";
    private BluetoothDevice btDevice;
    private UUID uuid;

    private BluetoothSocket btSocket = null;
    private volatile boolean running = false;

    public BTConnection(Context cnt, BluetoothDevice btDevice, UUID uuid)
            throws IOException
    {
        this.btDevice = btDevice;
        this.uuid = uuid;
    }

    @Override
    public void run() {

        running = true;

        while ((running == true) && this.isConnected())
        {

        }

        /* Close bluetooth socket */
        try
        {
            this.close();
        }
        catch (IOException e)
        {
            /* Do nothing */
        }

        /* Null BT socket to show needs to reconnect */
        btSocket = null;
        running = false;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isConnected() {
        boolean result = false;

        if (btSocket != null)
        {
            if (btSocket.isConnected())
                result = true;
        }
        return result;
    }

    public void connect() throws IOException {

        /* Attempt to make connection to remote device, throw exception if not */
        try
        {
            btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Unable to create RFCOMM", e);
            throw e;
        }

        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Unable to connect", e);

            /* Close our socket as unable to connect */
            try
            {
                this.close();
            }
            catch (IOException e2)
            {
                throw e2;
            }
            throw e;
        }
    }

    public void close() throws IOException {
        if (btSocket != null)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Unable to close socket", e);
                throw e;
            }
            btSocket = null;
        }
    }

}
