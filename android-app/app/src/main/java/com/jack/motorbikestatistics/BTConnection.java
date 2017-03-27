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
 * Created by Jack on 26-Jan-17.
 */

public class BTConnection implements Runnable {

    private static final String TAG = "BTConnection";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String NEW_LINE = "\r\n";

    private BluetoothDevice btDevice;
    private Handler RXHandler = null;

    private BluetoothSocket btSocket = null;

    private volatile boolean running = false;

    public BTConnection(BluetoothDevice btDevice)
            throws IOException {
        this.btDevice = btDevice;

        /* Connect the device so ready to use run */
        connect();
    }

    public final Handler txHandler = new Handler(Looper.getMainLooper()) {

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

    public void setRXHandler(Handler newHandler) {
        RXHandler = newHandler;
    }

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

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isConnected() {
        boolean result = false;

        if (btSocket != null) {
            if (btSocket.isConnected())
                result = true;
        }
        return result;
    }

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
