package com.jack.motorbikestatistics;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String REALTIME_CHAR = "1";
    private static final String LIST_SAVED_CHAR = "2";

    private static RealtimeFragment rtFragment = null;
    private static LoadDeviceFragment ldFragment = null;
    private static PairDeviceFragment pdFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                    message.obj = (String) REALTIME_CHAR;
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

                    ldFragment.setBTConnection(btConn);

                    //TODO: Implement inheritance for the handlers etc. since rt and ld share same functionality
                    btConn.setRXHandler(ldFragment.RXHandler);
                    Message message = new Message();
                    message.obj = (String) LIST_SAVED_CHAR;
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
