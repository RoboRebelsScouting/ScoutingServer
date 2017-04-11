package com.walpolerobotics.scouting.scoutingserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.walpolerobotics.scouting.scoutingserver.adapter.MainTabAdapter;
import com.walpolerobotics.scouting.scoutingserver.dialog.NoBluetoothSupportDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 0;

    private boolean bluetoothSetup = false;

    private ServerService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainTabAdapter tabAdapter = new MainTabAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(tabAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        preSetupBluetooth();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bluetoothSetup) {
            Intent intent = new Intent(this, ServerService.class);
            startService(intent);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        if (mService != null) {
            Log.v(TAG, "Changing action button icon");
            MenuItem item = menu.findItem(R.id.itemSearch);
            if (mService.isSearching()) {
                Log.v(TAG, "We are already searching");
                item.setIcon(R.drawable.ic_stop_white_24px);
            } else {
                Log.v(TAG, "We are not yet searching");
                item.setIcon(R.drawable.ic_find_replace_white_24px);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemSearch:
                if (bluetoothSetup && mService != null) {
                    if (mService.isSearching()) {
                        mService.cancelSearch();
                        item.setIcon(R.drawable.ic_find_replace_white_24px);
                    } else {
                        mService.searchForDevices();
                        item.setIcon(R.drawable.ic_stop_white_24px);
                    }
                }
                return true;
            case R.id.scheduleActivity:
                Intent intent = new Intent(this, EventScheduleActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Bluetooth is enabled");
                    bluetoothSetup = true;
                    Intent intent = new Intent(this, ServerService.class);
                    startService(intent);
                    bindService(intent, mConnection, BIND_AUTO_CREATE);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "Bluetooth is disabled");
                    finish();
                }
                break;
        }
    }

    private void preSetupBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Check for Bluetooth support
        if (bluetoothAdapter == null) {
            // No Bluetooth support, display message
            FragmentManager fm = getSupportFragmentManager();
            new NoBluetoothSupportDialog().show(fm, "noBluetoothSupport");
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled, enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        bluetoothSetup = true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerService.ServerBinder binder = (ServerService.ServerBinder) service;
            mService = binder.getInstance();
        }
    };
}