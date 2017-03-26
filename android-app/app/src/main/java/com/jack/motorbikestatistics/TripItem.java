package com.jack.motorbikestatistics;

/**
 * Created by Jack on 26-Mar-17.
 */

public class TripItem {

    private String tripName = null;
    private int fileSize = 0;

    public TripItem(String name, int size) {
        this.tripName = name;
        this.fileSize = size;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
