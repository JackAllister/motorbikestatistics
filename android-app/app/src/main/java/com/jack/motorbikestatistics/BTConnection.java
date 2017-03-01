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
            String jsonString = "";
            while (isRunning() && isConnected())
            {
                try
                {
                    int bytesAvailable = RXStream.available();

                    if (bytesAvailable > 0)
                    {
                        byte[] packetBytes = new byte[bytesAvailable];
                        int bytesRead = RXStream.read(packetBytes);

                        //jsonString += packetBytes;

                        Message message = new Message();
                        message.obj = "Test";
                        message.setTarget(RXHandler);
                        message.sendToTarget();

                        /*
                         * If jsonString is valid we add to our JSON receiver.
                         * Once that is completed we send to our interface.
                         *
                         * If not we wait till next run of while loop and then try re-add.
                         */
//                        try {
//                            //JSONObject newJSON = new JSONObject(jsonString);
//                            JSONObject newJSON = new JSONObject();
//                            newJSON.put("Test", "DEBUG");
//
//                            jsonInterface.JSONReceived(newJSON);
//                            jsonString = "";
//                        } catch (JSONException e) {
//                            /* Not valid JSON object so cannot send */
//                        }
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
