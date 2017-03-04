package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by Jack on 26-Jan-17.
 */

public class BTConnection implements Runnable {

    private static final String TAG = "BTConnection";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private BluetoothDevice btDevice;
    private Handler RXHandler;

    private BluetoothSocket btSocket = null;

    private volatile boolean running = false;


    public BTConnection(BluetoothDevice btDevice, Handler rxHandler)
            throws IOException
    {
        this.btDevice = btDevice;
        this.RXHandler = rxHandler;

        /* Connect the device so ready to use run */
        connect();
    }

    @Override
    public void run() {
        InputStream RXStream;

        /* Indicate that we are now running main thread */
        running = true;

        if (isConnected())
        {
            /* Get our input stream for receiving bytes */
            try
            {
                RXStream = btSocket.getInputStream();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Unable to get RXStream", e);
                running = false;
                return;
            }

            /*
             * While still connected and not signalled to stop we receive data
             * and then send it to the handler
             */
            while (isRunning() && isConnected())
            {
                try
                {
                    int bytesAvailable = RXStream.available();

                    if (bytesAvailable > 0)
                    {
                        byte[] packetBytes = new byte[bytesAvailable];
                        int bytesRead = RXStream.read(packetBytes);

                        /*
                         * Having to send data to main thread using messages
                         * as we are multithreading.
                         * If we try and use a standard call to function
                         * will cause a crash.
                         */
                        Message message = new Message();
                        message.obj = new String(packetBytes);
                        message.setTarget(RXHandler);
                        message.sendToTarget();
                    }

                }
                catch (IOException e)
                {
                    Log.e(TAG, "Unable to read data", e);
                    running = false;
                    return;
                }

            }
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

    private void connect() throws IOException {

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

    private void close() throws IOException {
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
