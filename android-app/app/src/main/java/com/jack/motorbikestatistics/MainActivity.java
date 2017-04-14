/**
 * @file MainActivity.java
 * @brief Main activity class responsible for tabbing.
 *
 * Responsible for navigation between each fragment/tab.
 * Sends relevant commands to switch system modes on the logging device
 * as well.
 *
 * @author Jack Allister - 23042098
 * @date 2016-2017
 */
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

/**
 * @brief Main activity class for fragment navigation.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /** @brief Command for switching to realtime logging. */
    private static final String REALTIME_CHAR = "1";
    /** @brief Command for loading all saved trip details. */
    private static final String LIST_SAVED_CHAR = "2";

    /** @brief UI fragment for realtime statistic display. */
    private static RealtimeFragment rtFragment = null;
    /** @brief UI fragment for loading previous trips. */
    private static LoadDeviceFragment ldFragment = null;
    /** @brief UI fragment for pairing to a logging device. */
    private static PairDeviceFragment pdFragment = null;

    /**
     * @brief Function called when main activity is loaded.
     *
     * Procedure is called when application is first started,
     * sets up UI and creates relevant fragments.
     *
     * @param savedInstanceState - Information holding last previous state.
     */
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

    /**
     * @brief Responsible for closing navigation drawer when
     * back button pressed.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * @brief Changes active fragment when a tab has been pressed.
     *
     * Responsible for changing to the new chosen fragment on the UI.
     * Opening of realtime and loaddevice fragments not possible when
     * not connected to the logging device.
     *
     * Method also responsible for change system state machine on the logging
     * device, this is done by transmitting command code.
     *
     * @param item - New selected fragment/tab to display.
     */
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
