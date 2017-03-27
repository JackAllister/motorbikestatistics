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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();

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

    private float calcDistance(LatLng start, LatLng end)
    {
        float[] results = new float[1];

        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        /* Create our info window adapter class that is shown when marker clicked */
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

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
        });


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

}
