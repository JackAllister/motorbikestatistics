package com.jack.motorbikestatistics;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

        /* For now we just place our first icon, need to figure out routes... */
        //TODO: Make so shows route
        if (jsonList.size() != 0)
        {
            try
            {
                JSONObject rootJSON = jsonList.get(0);

                JSONObject gpsJSON = rootJSON.getJSONObject("gps");
                Double lat = gpsJSON.getDouble("lat");
                Double lng = gpsJSON.getDouble("lng");

                LatLng firstPoint = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(firstPoint).title("Location 0"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(firstPoint));
            }
            catch (JSONException e)
            {
                /* Do nothing */
            }
        }

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
