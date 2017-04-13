/**
 * @file BTConnection.java
 * @brief Class for holding containing bluetooth connection on app.
 *
 * Class runs in a seperate thread to main UI allowing for concurrent
 * transmission and receiving of data to/from the logging device.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @brief Thread class for a new bluetooth connection to a device.
 */
public class BTConnection implements Runnable {

    /** @brief Tag using for debugging */
    private static final String TAG = "BTConnection";
    /** @brief UUID to allow Serial connection via BT */
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    /** @brief New line string */
    private static final String NEW_LINE = "\r\n";

    /** @brief Bluetooth Device object, holds information for chosen slave */
    private BluetoothDevice btDevice;
    /** @brief Handler function where received data is sent to */
    private Handler RXHandler = null;
    /** @brief Socket created for bluetooth connection, used for TX/RX */
    private BluetoothSocket btSocket = null;
    /** @brief Indicates whether main run thread is in progress */
    private volatile boolean running = false;

    /**
     * @brief Constructor for BTConnection class.
     *
     * Sets the BT device interface used for this class
     * and attempts a connection.
     *
     * @param btDevice - Device used for creating connection.
     */
    public BTConnection(BluetoothDevice btDevice)
            throws IOException {
        this.btDevice = btDevice;

        /* Connect the device so ready to use run */
        connect();
    }

    /**
     * @brief Handler class for transmission of data.
     *
     * Messages containing data to be transmitted are sent from
     * main UI thread.
     */
    public final Handler txHandler = new Handler(Looper.getMainLooper()) {

        /**
         * @brief Handler function for transmission of data.
         *
         * Receives data from main UI thread that is then transmitted
         * via BT serial to the logging device.
         *
         * @param msg - Message object containing data to be transmitted.
         */
        @Override
        public void handleMessage(Message msg) {

            /* If connected and running, transmit character over bluetooth */
            if (isConnected() && isRunning()) {
                OutputStream TXStream;

                /* Get our output stream for sending bytes then send data */
                try {
                    TXStream = btSocket.getOutputStream();

                    String txString = (String)msg.obj;
                    TXStream.write(txString.getBytes());
                } catch (IOException e) {
                    Log.e(TAG, "Unable to use TXStream", e);
                    return;
                }
            }
        }
    };

    /**
     * @brief Setter function for RXHandler.
     *
     * @param newHandler - The new Handler where RX'd data will be sent to.
     */
    public void setRXHandler(Handler newHandler) {
        RXHandler = newHandler;
    }

    /**
     * @brief Main run procedure for new Runnable thread created.
     *
     * If connected procedure waits for data to be received.
     * Parsing this received into lines and then splitting each line
     * into a JSONObject. If a valid JSONObject is found it is then sends
     * to the receive handler in a seperate thread (using messages).
     */
    @Override
    public void run() {
        InputStream RXStream;

        /* Indicate that we are now running main thread */
        running = true;

        if (isConnected()) {
            /* Get our input stream for receiving bytes */
            try {
                RXStream = btSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Unable to get RXStream", e);
                running = false;
                return;
            }

            /*
             * While still connected and not signalled to stop we receive data
             * and then send it to the handler
             */
            String recvBuff = "";
            while (isRunning() && isConnected()) {
                try {
                    int bytesAvailable = RXStream.available();

                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        int bytesRead = RXStream.read(packetBytes, 0, bytesAvailable);

                        recvBuff += new String(packetBytes);
                    }

                    if (RXHandler != null) {

                        if (recvBuff.indexOf(NEW_LINE) > 0) {

                            String jsonLine = recvBuff.substring(0, recvBuff.indexOf(NEW_LINE));

                            /*
                             * Having to send data to main thread using messages
                             * as we are multithreading.
                             * If we try and use a standard call to function
                             * will cause a crash.
                             */
                            Bundle dataBundle =  new Bundle();
                            dataBundle.putString("JSON", jsonLine);

                            Message message = RXHandler.obtainMessage();
                            message.setData(dataBundle);
                            message.sendToTarget();

                            recvBuff = recvBuff.replace(jsonLine + NEW_LINE, "");
                        }
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Unable to read data", e);
                    running = false;
                    return;
                }
            }

        }

        /* Close bluetooth socket */
        try {
            this.close();
        } catch (IOException e) {
            /* Do nothing */
        }

        /* Null BT socket to show needs to reconnect */
        btSocket = null;
        running = false;
    }

    /**
     * @brief Procedure to stop the bluetooth connection thread from running.
     */
    public void stop() {
        running = false;
    }

    /**
     * @brief Function to check whether main connection thread is running.
     * @return boolean - Whether thread is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @brief Function to check whether BT connection is still valid.
     * @return boolean - Whether connection is still available.
     */
    public boolean isConnected() {
        boolean result = false;

        if (btSocket != null) {
            if (btSocket.isConnected())
                result = true;
        }
        return result;
    }

    /**
     * @brief Procedure to create a connection to logging device.
     *
     * Creates a raw Serial socket via UUID and then attempts
     * to connect. Exceptions thrown on failure.
     */
    private void connect() throws IOException {

        /* Attempt to make connection to remote device, throw exception if not */
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e(TAG, "Unable to create RFCOMM", e);
            throw e;
        }

        try {
            btSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, "Unable to connect", e);

            /* Close our socket as unable to connect */
            try {
                this.close();
            } catch (IOException e2) {
                throw e2;
            }
            throw e;
        }
    }

    /**
     * @brief Closes the BT connection socket, exceptions thrown on failure.
     */
    private void close() throws IOException {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close socket", e);
                throw e;
            }
            btSocket = null;
        }
    }

}
