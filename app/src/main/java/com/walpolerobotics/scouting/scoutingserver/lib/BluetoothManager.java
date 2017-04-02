package com.walpolerobotics.scouting.scoutingserver.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.walpolerobotics.scouting.scoutingserver.adapter.DeviceAdapter;

import java.util.ArrayList;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";

    private static BluetoothManager mBluetoothManager;

    private ArrayList<ScoutClient> mClients = new ArrayList<>();
    private DeviceAdapter mListAdapter;

    private ClientAcceptThread mAcceptThread;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            ScoutClient client = (ScoutClient) inputMessage.obj;
            registerClient(client);
        }
    };

    private BluetoothManager() {
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
            mAcceptThread = new ClientAcceptThread();
            mAcceptThread.start();
        }
    }

    public void cancelSearch() {
        if (mAcceptThread != null) {
            mAcceptThread.cancelClientAccept();
        }
    }

    public boolean isSearching() {
        return mAcceptThread != null && mAcceptThread.isAlive();
    }

    void handleAcceptedClient(ScoutClient client) {
        Message acceptMessage = mHandler.obtainMessage(0, client);
        acceptMessage.sendToTarget();
    }

    private void registerClient(ScoutClient client) {
        mClients.add(client);
        if (mListAdapter != null) {
            mListAdapter.add(client);
        }
    }

    private void removeClient(int pos) {
        mClients.remove(pos);
        if (mListAdapter != null) {
            mListAdapter.remove(pos);
        }
    }
}