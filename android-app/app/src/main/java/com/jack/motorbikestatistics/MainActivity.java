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

    private static RealtimeFragment rtFragment = null;
    private static LoadDeviceFragment ldFragment = null;
    private static PairDeviceFragment pdFragment = null;
    private static Fragment activeFragment = null;

    private static String receiveString = "";

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

        /* Handle navigation view clicks here */
        FragmentManager fragmentManager = getFragmentManager();
        int id = item.getItemId();

        switch (id)
        {
            case R.id.nav_realtime:
            {
                activeFragment = rtFragment;
                break;
            }

            case R.id.nav_loaddevice:
            {
                activeFragment = ldFragment;
                break;
            }

            case R.id.nav_pairdevice:
            {
                activeFragment = pdFragment;
                pdFragment.setRXHandler(jsonHandler);
            }

        }

        if (activeFragment != null)
        {
            /* Replaces content frame with newly selected one */
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, activeFragment)
                    .commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final Handler jsonHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            receiveString += (String)msg.obj;

            /* Check that new line exists, otherwise wait till next time called */
            if (receiveString.indexOf("\r\n") >= 0)
            {
                String[] line = receiveString.split("\r\n");

                for (int i = 0; i < line.length; i++)
                {
                    /* Try parse each line for JSON data */
                    try {
                        JSONObject tmpJSON = new JSONObject(line[i]);

                        /* Send data to current active fragment */
                        if (activeFragment == rtFragment)
                        {
                            rtFragment.newData(tmpJSON);
                        }
                    } catch (JSONException e)
                    {
                        /* Ignore line if exception */
                    }
                }

                receiveString = "";
            }
        }
    };

}
