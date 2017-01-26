package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Jack on 25-Jan-17.
 */

public class BTDeviceItem {

    private BTConnection connection = null;
    private int iconID;
    private BluetoothDevice device;
    private String status;

    public BTConnection getConnection() {
        return connection;
    }

    public void setConnection(BTConnection newConn) {
        connection = newConn;
    }

    public BluetoothDevice getDevice() {
        return device;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus) {
        status = newStatus;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int newID) {
        iconID = newID;
    }

    public BTDeviceItem(BluetoothDevice device, String status, int iconID)
    {
        this.device = device;
        this.status = status;
        this.iconID = iconID;
    }
}
