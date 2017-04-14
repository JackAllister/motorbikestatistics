/**
 * @file MapsActivity.java
 * @brief Maps activity class reponsible for showing data on Google Maps.
 *
 * Responsible for showing trip data on google maps.
 * Places clickable points 5m away from each other showing stats at that point.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
package com.jack.motorbikestatistics;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @brief Maps activity class for displaying map data.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /** @brief Google maps object for plotting. */
    private GoogleMap mMap;
    /** @brief ArrayList holding all trip data. */
    private ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();

    /**
     * @brief Fills our maps array with points to plot on the map.
     *
     * Called when maps activity is first started.
     * Responsible for making sure we have points to plot.
     *
     * @param savedInstanceState - Information holding last previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        getJSONObjects();

        mapFragment.getMapAsync(this);
    }

    /**
     * @brief Gets point data and convert to array of JSON objects.
     *
     * Gets an arraylist of strings passed via a bundle to this activity.
     * These strings are there converted back to JSON objects which
     * will be used for plotting.
     * The reason for not passing straight JSON objects is because
     * they are not serializable and passable between activities.
     *
     * @return boolean - Whether all objects were able to be created.
     */
    private boolean getJSONObjects()
    {
        boolean result = true;

        /*
         * Get our serialized arrayList of jsonStrings
         * then convert them back to jsonObjects
         */
        ArrayList<String> jsonStrings = (ArrayList<String>)getIntent().getSerializableExtra("JSONList");
        for (int i = 0; i < jsonStrings.size(); i++)
        {
            try
            {
                JSONObject jsonObject = new JSONObject(jsonStrings.get(i));
                jsonList.add(jsonObject);
            }
            catch (JSONException e)
            {
                result = false;
            }
        }

        return result;
    }

    /**
     * @brief Finds JSONObject from ArrayList via LAT/LNG coordinates.
     *
     * @param position - Latitude and Longitude position.
     * @return JSONObject - The found JSON object.
     */
    private JSONObject findJSONByLatLng(LatLng position) {
        JSONObject result = null;

        for (int i = 0; i < jsonList.size(); i++) {
            JSONObject tmpJSON = jsonList.get(i);

            try {
                JSONObject gpsJSON = tmpJSON.getJSONObject("gps");

                Double latitude = gpsJSON.getDouble("lat");
                Double longitude = gpsJSON.getDouble("lng");

                /* Check to see if latitude and logitudes match */
                if ((latitude == position.latitude) && (longitude == position.longitude)) {
                    result = tmpJSON;
                    break;
                }

            } catch (JSONException e) {
                /* Do nothing */
            }
        }

        return result;
    }

    /**
     * @brief Calculates the absolute distance between two points.
     *
     * Distance is as the crow flys and not via streets etc.
     * @param start - Start position.
     * @param end - End position.
     * @return flaot - Distance between points in metres.
     */
    private float calcDistance(LatLng start, LatLng end)
    {
        float[] results = new float[1];

        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

    /**
     * @brief Manipulates the map once available.
     *
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines.
     *
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * @param googleMap - Our map object ready to manipulate.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        /* Set our info window adapter class that is shown when marker clicked */
        mMap.setInfoWindowAdapter(new StatisticWindowAdapter());

        /* If we have no data don't bother plotting points */
        if (jsonList.size() != 0)
        {
            /* lineOpts will store our route */
            PolylineOptions lineOpts = new PolylineOptions();
            lineOpts.color(Color.parseColor( "#CC0000FF"));
            lineOpts.width(5);
            lineOpts.visible(true);

            try
            {
                LatLng lastMarker = null;

                /* Plot every point in the our JSONObject array */
                for (int i = 0; i < jsonList.size(); i++)
                {
                    JSONObject rootJSON = jsonList.get(i);
                    JSONObject gpsJSON = rootJSON.getJSONObject("gps");

                    Double lat = gpsJSON.getDouble("lat");
                    Double lng = gpsJSON.getDouble("lng");
                    LatLng location = new LatLng(lat, lng);

                    /* Add this location to our trip line */
                    lineOpts.add(location);

                    /*
                     * Check if distance between this point and
                     * last marker is greater than 5m otherwise don't add marker.
                     * Adding markers every 5 metres prevents the map being spammed with
                     * thousands of readings.
                     */
                    if ((lastMarker == null) || (calcDistance(location, lastMarker) > 5))
                    {
                        /* Only add a marker if the gps data is valid */
                        if (gpsJSON.getBoolean("gps_valid") == true) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(location);
                            markerOptions.title("Reading: " + i);

                            mMap.addMarker(markerOptions);

                            lastMarker = location;

                            /* Changes camera to point to newest marker */
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
                        }
                    }

                }
                mMap.addPolyline(lineOpts);
            }
            catch (JSONException e)
            {
                /* Do nothing */
            }
        }
    }

    /**
     * @brief Adapter used for displaying statistics at a certain marker
     * that user has clicked on.
     */
    private class StatisticWindowAdapter implements GoogleMap.InfoWindowAdapter {

        /**
         * @brief We don't want to use default information window.
         */
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        /**
         * @brief Displays statistics at a marker that
         * the user has clicked on.
         *
         * @param marker - The marker the user has clicked on.
         * @return View - Updated view showing information.
         */
        @Override
        public View getInfoContents(Marker marker) {

            View v =  getLayoutInflater().inflate(R.layout.map_marker_info, null);

            /* Get latitude and longitude from marker */
            LatLng latlng = marker.getPosition();

            /* Find the JSONObject relating to this location */
            JSONObject rootJSON = findJSONByLatLng(latlng);
            if (rootJSON != null) {
                try {
                    JSONObject gpsJSON = rootJSON.getJSONObject("gps");
                    JSONObject orientJSON = rootJSON.getJSONObject("orientation");
                    JSONObject timeJSON = rootJSON.getJSONObject("time");

                    /* Set latitude and longitude in info window */
                    TextView tvLatLng = (TextView)v.findViewById(R.id.map_latlng);
                    tvLatLng.setText("Lat/Lng: " + Double.toString(latlng.latitude) + "/"
                            + Double.toString(latlng.longitude));

                    /* Set time */
                    TextView tvTime = (TextView)v.findViewById(R.id.map_time);
                    Calendar cal = Calendar.getInstance();
                    cal.clear();
                    cal.set(Calendar.YEAR, timeJSON.getInt("year"));
                    cal.set(Calendar.MONTH, timeJSON.getInt("month"));
                    cal.set(Calendar.DATE, timeJSON.getInt("day"));

                    cal.set(Calendar.HOUR, timeJSON.getInt("hour"));
                    cal.set(Calendar.MINUTE, timeJSON.getInt("minute"));
                    cal.set(Calendar.SECOND, timeJSON.getInt("second"));
                    cal.set(Calendar.MILLISECOND, timeJSON.getInt("centiseconds") * 10);

                    /* Create format for date and times then add to view */
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss.SS");
                    tvTime.setText("Time: " + dateFormat.format(cal.getTime()));

                    /* Velocity & Altitude */
                    TextView tvVelocity = (TextView)v.findViewById(R.id.map_velocity);
                    tvVelocity.setText("Velocity: " + gpsJSON.getDouble("vel_mph") + "mph");

                    TextView tvAltitude = (TextView)v.findViewById(R.id.map_altitude);
                    tvAltitude.setText("Altitude: " + gpsJSON.getDouble("alt_ft") + "ft");

                    /* Orientation */
                    TextView tvPitch = (TextView)v.findViewById(R.id.map_pitch);
                    tvPitch.setText("Pitch Angle: " + orientJSON.getDouble("pitch") + "\u00b0");

                    TextView tvRoll = (TextView)v.findViewById(R.id.map_roll);
                    tvRoll.setText("Roll/Lean Angle: " + orientJSON.getDouble("roll") + "\u00b0");

                } catch (JSONException e) {
                    marker.hideInfoWindow();
                }
            } else {
                /* If unable to find relating we hide the info window */
                marker.hideInfoWindow();
            }

            return v;
        }
    }

}
