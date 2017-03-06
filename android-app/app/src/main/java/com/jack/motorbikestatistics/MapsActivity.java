package com.jack.motorbikestatistics;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

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

import java.util.ArrayList;

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

                    lineOpts.add(location);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

                    /*
                     * Check if distance between this point and
                     * last marker is greater than 5m otherwise don't add marker.
                     * Adding markers every 5 metres prevents the map being spammed with
                     * thousands of readings.
                     */
                    if ((lastMarker == null) || (calcDistance(location, lastMarker) > 5))
                    {
                        MarkerOptions marker = new MarkerOptions();
                        marker.position(location);
                        marker.title("Reading " + Integer.toString(i));
                        mMap.addMarker(marker);

                        lastMarker = location;
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

    private float calcDistance(LatLng start, LatLng end)
    {
        float[] results = new float[1];

        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }
}
