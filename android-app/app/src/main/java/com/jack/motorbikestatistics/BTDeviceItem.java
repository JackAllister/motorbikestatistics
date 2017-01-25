package com.jack.motorbikestatistics;

/**
 * Created by Jack on 25-Jan-17.
 */

public class BTDeviceItem {

    private String name;
    private String address;
    private String status;
    private boolean connected;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public boolean isConnected() {
        return connected;
    }

    public BTDeviceItem(String name, String address, String status, boolean connected)
    {
        this.name = name;
        this.address = address;
        this.status = status;
        this.connected = connected;
    }
}
