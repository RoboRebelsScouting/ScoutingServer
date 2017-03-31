package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";

    private static BluetoothManager mBluetoothManager;
    private ArrayList<ScoutClient> mClients = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothManager getBluetoothManager() {
        if (mBluetoothManager == null) {
            mBluetoothManager = new BluetoothManager();
        }

        return mBluetoothManager;
    }

    public void searchForDevices() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Log.v(TAG, "Beginning search for tablet");
                    UUID id = UUID.fromString("35c2ad3a-14dc-11e7-93ae-92361f002671");
                    BluetoothServerSocket serverSocket = mBluetoothAdapter
                            .listenUsingRfcommWithServiceRecord("ScoutingServer", id);
                    ScoutClient client = new ScoutClient(ScoutClient.ALLIANCE_BLUE,
                            ScoutClient.POSITION_1);
                    Log.v(TAG, "Now accepting connections");
                    client.setBluetoothSocket(serverSocket.accept());
                    Log.v(TAG, "Connection accepted");
                    mClients.add(client);
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}
