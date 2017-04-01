package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.adapter.DeviceAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";
    private static final UUID ID = UUID.fromString("35c2ad3a-14dc-11e7-93ae-92361f002671");

    private static BluetoothManager mBluetoothManager;

    private ArrayList<ScoutClient> mClients = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private DeviceAdapter mListAdapter;

    private AcceptThread mAcceptThread;
    private BluetoothServerSocket mServerSocket;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            BluetoothTask task = (BluetoothTask) inputMessage.obj;
        }
    };

    private BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothManager getBluetoothManager() {
        if (mBluetoothManager == null) {
            mBluetoothManager = new BluetoothManager();
        }

        return mBluetoothManager;
    }

    public DeviceAdapter getListAdapter(Context context) {
        if (mListAdapter == null) {
            mListAdapter = new DeviceAdapter(context);
            mListAdapter.setDevices(mClients);
        }

        return mListAdapter;
    }

    public void searchForDevices() {
        if (mAcceptThread == null || !mAcceptThread.isAlive()) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public void cancelSearch() {
        if (mAcceptThread != null) {
            mAcceptThread.interrupt();
        }

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSearching() {
        return mAcceptThread != null && mAcceptThread.isAlive();
    }

    private void registerClient(ScoutClient client) {
        mClients.add(client);
        /*if (mListAdapter != null) {
            mListAdapter.add(client);
        }*/
    }

    private void removeClient(int pos) {
        mClients.remove(pos);
        /*if (mListAdapter != null) {
            mListAdapter.remove(pos);
        }*/
    }

    private class AcceptThread extends Thread {
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
                        registerClient(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.v(TAG, "No longer accepting connections");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
