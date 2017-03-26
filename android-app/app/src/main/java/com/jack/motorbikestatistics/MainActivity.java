package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String REALTIME_MODE = "R";
    private static final String LOAD_PREVIOUS_MODE = "L";

    private static RealtimeFragment rtFragment = null;
    private static LoadDeviceFragment ldFragment = null;
    private static PairDeviceFragment pdFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* Create our fragments for different sections of UI */
        rtFragment = new RealtimeFragment();
        ldFragment = new LoadDeviceFragment();
        pdFragment = new PairDeviceFragment();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment activeFragment = null;

        /* Handle navigation view clicks here */
        FragmentManager fragmentManager = getFragmentManager();
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_realtime: {
                /* Get our bluetooth connection from pairing fragment */
                BTConnection btConn = pdFragment.getBTConnection();

                if (btConn != null && btConn.isConnected()) {
                    /* We set our RX handler and also send our command to indicate mode change */
                    btConn.setRXHandler(rtFragment.RXHandler);
                    Message message = new Message();
                    message.obj = (String)REALTIME_MODE;
                    message.setTarget(btConn.txHandler);
                    message.sendToTarget();

                    /* Change to our new active fragment */
                    activeFragment = rtFragment;
                } else {
                    /* Indicate that we are not connected to device */
                    View rootView = findViewById(R.id.content_main);
                    Snackbar.make(rootView, "Please connect to a device first.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            }

            case R.id.nav_loaddevice: {
                /* Get our bluetooth connection from pairing fragment */
                BTConnection btConn = pdFragment.getBTConnection();

                if (btConn != null && btConn.isConnected()) {
                    /* We set our RX handler and also send our command to indicate mode change */

                    //TODO: Implement inheritance for the handlers etc. since rt and ld share same functionality
                    //btConn.setRXHandler(ldFragment.RXHandler);
                    Message message = new Message();
                    message.obj = (String)LOAD_PREVIOUS_MODE;
                    message.setTarget(btConn.txHandler);
                    message.sendToTarget();

                    /* Change to our new active fragment */
                    activeFragment = ldFragment;
                } else {
                    /* Indicate that we are not connected to device */
                    View rootView = findViewById(R.id.content_main);
                    Snackbar.make(rootView, "Please connect to a device first.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            }

            case R.id.nav_pairdevice: {
                activeFragment = pdFragment;
            }

        }

        if (activeFragment != null) {
            /* Replaces content frame with newly selected one */
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, activeFragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return (activeFragment != null);
    }
}
