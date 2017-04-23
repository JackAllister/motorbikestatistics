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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private ArrayList<JSONObject> tripData;
    private ArrayList<Marker> markerList;

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

        /* Clear our marker list for new instances. */
        markerList = new ArrayList<Marker>();

        /* Get the tripData from our JSON handler singleton. */
        tripData = JSONHandlerSingleton.getInstance().tripData;

        mapFragment.getMapAsync(this);
    }

    /**
     * @brief Finds JSONObject from ArrayList via LAT/LNG coordinates.
     *
     * @param position - Latitude and Longitude position.
     * @return JSONObject - The found JSON object.
     */
    private JSONObject findJSONByLatLng(LatLng position) {
        JSONObject result = null;

        for (int i = 0; i < tripData.size(); i++) {
            JSONObject tmpJSON = tripData.get(i);

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

        /* Set our listener class for adjusting visibility when zoomed. */
        mMap.setOnCameraMoveListener(new ZoomToogleListener());

        /* If we have no data don't bother plotting points */
        if (tripData.size() != 0)
        {
            /* lineOpts will store our route */
            PolylineOptions lineOpts = new PolylineOptions();
            lineOpts.color(Color.parseColor( "#CCF44242"));
            lineOpts.width(18);
            lineOpts.visible(true);

            try
            {
                LatLng lastMarker = null;

                /* Plot every point in the our JSONObject array */
                for (int i = 0; i < tripData.size(); i++)
                {
                    JSONObject rootJSON = tripData.get(i);
                    JSONObject gpsJSON = rootJSON.getJSONObject("gps");

                    Double lat = gpsJSON.getDouble("lat");
                    Double lng = gpsJSON.getDouble("lng");
                    LatLng location = new LatLng(lat, lng);

                    /* Don't add location with invalid lat & lng. */
                    if (location.latitude != 0.00 && location.longitude != 0.00) {

                        /* Add this location to our trip line */
                        lineOpts.add(location);

                        /*
                         * Check if distance between this point and
                         * last marker is greater than 5m otherwise don't add marker.
                         * Adding markers every 5 metres prevents the map being spammed with
                         * thousands of readings.
                         */
                        if ((lastMarker == null) || (calcDistance(location, lastMarker) > 5)) {
                            /* Only add a marker if the gps data is valid */
                            if (gpsJSON.getBoolean("gps_valid") == true) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(location);
                                markerOptions.title("Reading: " + i);
                                markerOptions.icon(getBitmapDescriptor(R.drawable.ic_expand_more_white_24px));
                                markerOptions.visible(false);

                                /* Add our marker to map and to list. */
                                markerList.add(mMap.addMarker(markerOptions));

                                lastMarker = location;

                                /* Changes camera to point to newest marker */
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
                            }
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
     * @brief Function for converting raw DP value to pixels.
     *
     * @param dp - DP value.
     * @param context - Context of application.
     * @return float - Value in pixels.
     */
    private float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * @brief Created a valid bitmap descriptor from a drawable resource ID.
     *
     * @param id - Drawable resource ID.
     * @return BitmapDescriptor - Bitmap image converted from VectorAsset.
     */
    private BitmapDescriptor getBitmapDescriptor(int id) {

        Context context = getApplicationContext();
        Drawable vectorDrawable = ContextCompat.getDrawable(context, id);
        int h = ((int) convertDpToPixel(42, context));
        int w = ((int) convertDpToPixel(25, context));
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    /**
     * @brief Listener class for making markers invisible when zoomed out.
     */
    private class ZoomToogleListener implements GoogleMap.OnCameraMoveListener {

        private boolean currentVisible = false;

        /**
         * @brief Listener function that tooggles all markers visibility.
         */
        @Override
        public void onCameraMove() {

            if (mMap != null) {
                float zoom = mMap.getCameraPosition().zoom;

                /* Check current zoom level on the map. */
                if (zoom > 18.0) {
                    /* Make markers visible. */
                    setMarkersVisible(true);
                } else {
                    /* Make markers invisible. */
                    setMarkersVisible(false);
                }
            }
        }

        /**
         * @brief Sets all markers either visible or invisible.
         *
         * @param visible - Value to set to.
         */
        private void setMarkersVisible(boolean visible) {
            if (currentVisible != visible) {
                for (int i = 0; i < markerList.size(); i++) {

                    Marker tmp = markerList.get(i);
                    tmp.setVisible(visible);
                    currentVisible = visible;
                }
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
