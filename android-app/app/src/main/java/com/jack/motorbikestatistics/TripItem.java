/**
 * @file TripItem.java
 * @brief Class for holding information relating to a specific trip.
 *
 * Holds the trips name and file size. This information is used when
 * loading a previous trip.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

/**
 * @brief Class used for holding name and size information relating to a trip.
 */
public class TripItem {

    /** @brief The trips name on the uSD card. */
    private String tripName = null;
    /** @brief The trips file size on the uSD card. */
    private int fileSize = 0;

    /**
     * @brief Constructor for creating of a TripItem.
     *
     * Sets the original file name and size.
     *
     * @param name - Trip name.
     * @param size - Size of the file.
     */
    public TripItem(String name, int size) {
        this.tripName = name;
        this.fileSize = size;
    }

    /**
     * @brief Getter for trip name.
     * @return String - Trip name.
     */
    public String getTripName() {
        return tripName;
    }

    /**
     * @brief Setter for trip name.
     * @param tripName - New trip name.
     */
    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    /**
     * @brief Getter for trip filesize.
     * @return int - Filesize in bytes.
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @brief Setter for trip filesize.
     * @param fileSize - New trip filesize.
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
