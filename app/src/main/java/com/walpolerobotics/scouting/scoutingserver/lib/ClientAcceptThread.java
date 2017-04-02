package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

class ClientAcceptThread extends Thread {

    private static final String TAG = "AcceptThread";
    private static final UUID ID = UUID.fromString("35c2ad3a-14dc-11e7-93ae-92361f002671");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;

    ClientAcceptThread() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void run() {
        try {
            Log.v(TAG, "Beginning search for tablet");
            mServerSocket = mBluetoothAdapter
                    .listenUsingRfcommWithServiceRecord("ScoutingServer", ID);
            while (!interrupted()) {
                Log.v(TAG, "Now accepting connections");
                try {
                    ScoutClient client = new ScoutClient(mServerSocket.accept());
                    Log.v(TAG, "Connection accepted");
                    BluetoothManager bluetoothManager = BluetoothManager.getBluetoothManager();
                    bluetoothManager.handleAcceptedClient(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.v(TAG, "No longer accepting connections");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void cancelClientAccept() {
        interrupt();
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
