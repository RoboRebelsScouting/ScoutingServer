package com.walpolerobotics.scouting.scoutingserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.adapter.MainTabAdapter;
import com.walpolerobotics.scouting.scoutingserver.dialog.BluetoothNotEnabledDialog;
import com.walpolerobotics.scouting.scoutingserver.dialog.NoBluetoothSupportDialog;
import com.walpolerobotics.scouting.scoutingserver.lib.BluetoothManager;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_ENABLE_BT = 0;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Bluetooth is enabled");
                    BluetoothManager.getBluetoothManager().searchForDevices();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(TAG, "Bluetooth is disabled");
                    FragmentManager fm = getSupportFragmentManager();
                    new BluetoothNotEnabledDialog().show(fm, "bluetoothNotEnabled");
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

        BluetoothManager.getBluetoothManager().searchForDevices();
    }
}