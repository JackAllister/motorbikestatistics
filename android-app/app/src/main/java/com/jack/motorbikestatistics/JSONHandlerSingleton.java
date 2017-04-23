/**
 * @file JSONHandlerSingleton.java
 * @brief Singleton class that holds an array of the app's JSON data.
 *
 * Implementation of Singlton design pattern to allow cross activity/fragment
 * data sharing of JSON trip data.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @brief Singleton class for holding all JSON trip data.
 */
public class JSONHandlerSingleton {

    /* Singleton instance of this class. */
    private static JSONHandlerSingleton ourInstance = new JSONHandlerSingleton();
    /* ArrayList of trip statistics. */
    public ArrayList<JSONObject> tripData;

    /**
     * @brief Returns the instance of this singleton class.
     * @return JSONHandleSingleton - Instance of this class.
     */
    public static JSONHandlerSingleton getInstance() {
        return ourInstance;
    }

    /**
     * @brief Constructor for instance. Ensures ArrayList is initialised.
     */
    private JSONHandlerSingleton() {
        /*
         * On creation of our singleton object, initialise ArrayList that will
         * hold our global trip data.
         */
        tripData = new ArrayList<JSONObject>();
    }
}
