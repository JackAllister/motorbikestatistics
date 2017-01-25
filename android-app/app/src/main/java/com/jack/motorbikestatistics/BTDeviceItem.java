package com.jack.motorbikestatistics;

/**
 * Created by Jack on 25-Jan-17.
 */

public class BTDeviceItem {

    private String name_;
    private String address_;
    private boolean connected_;

    public String getName() {
        return name_;
    }

    public String getAddress() {
        return address_;
    }

    public boolean isConnected() {
        return connected_;
    }

    public BTDeviceItem(String name, String address, boolean connected)
    {
        this.name_ = name;
        this.address_ = address;
        this.connected_ = connected;
    }
}
