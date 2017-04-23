/**
 * @file BTDeviceItem.java
 * @brief UI class for holding information regarding a bluetooth device.
 *
 * Implemented for the ListView that shows unpaired/paired & connected
 * bluetooth devices.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.bluetooth.BluetoothDevice;

/**
 * @brief Class used for holding core UI information of a bluetooth devices.
 */
public class BTDeviceItem {

    /** @brief Variable for BTConnection if device is already connected. */
    private BTConnection connection = null;
    /** @brief ID of icon to use within the ListView. */
    private int iconID;
    /** @brief Device object that holds info such as name, HWID etc. */
    private BluetoothDevice device;
    /** @brief Status of the device, unpaired, paired, connected. */
    private String status;

    /**
     * @brief Getter for the bluetooth connection of specified device.
     * @return BTConnection - Connection between app & logging device.
     */
    public BTConnection getConnection() {
        return connection;
    }

    /**
     * @brief Setter for setting the DeviceItem object's connection.
     * @param newConn - New connection between app & logging device.
     */
    public void setConnection(BTConnection newConn) {
        connection = newConn;
    }

    /**
     * @brief Getter for BT device object (contains name, HWID etc.).
     * @return BluetoothDevice - The bluetooth device object.
     */
    public BluetoothDevice getDevice() {
        return device;
    }

    /**
     * @brief Getter for current status of BTDeviceItem.
     * @return String - Current status: unpaired, paired or connected.
     */
    public String getStatus() {
        return status;
    }

    /**
     * @brief Setter for current status of BTDeviceItem.
     * @param newStatus - New string for status.
     */
    public void setStatus(String newStatus) {
        status = newStatus;
    }

    /**
     * @brief Getter for icon ID to use in ListView.
     * @return int - Icon ID to use.
     */
    public int getIconID() {
        return iconID;
    }

    /**
     * @brief Setter for icon ID to use in ListView.
     * @param newID - New icon ID to use.
     */
    public void setIconID(int newID) {
        iconID = newID;
    }

    /**
     * @brief Constructor for BTDeviceItem class.
     *
     * Called when new BluetoothDevice is found during discovery, so
     * that it can be added to the device ListView.
     *
     * @param device - BluetoothDevice containing HWID, name, etc.
     * @param status - Current status of the discovered device.
     * @param iconID - Icon ID to display within the ListView.
     */
    public BTDeviceItem(BluetoothDevice device, String status, int iconID)
    {
        this.device = device;
        this.status = status;
        this.iconID = iconID;
    }

}
