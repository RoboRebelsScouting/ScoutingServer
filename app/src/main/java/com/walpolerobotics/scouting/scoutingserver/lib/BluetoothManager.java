package com.walpolerobotics.scouting.scoutingserver.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.adapter.DeviceAdapter;

import java.util.ArrayList;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";

    private static BluetoothManager mBluetoothManager;

    private ArrayList<ScoutClient> mTempClients = new ArrayList<>();
    private DeviceAdapter mListAdapter;

    private ClientAcceptThread mAcceptThread;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            ClientAcceptTask task = (ClientAcceptTask) inputMessage.obj;
            ScoutClient client = task.client;
            switch (inputMessage.what) {
                case ClientAcceptTask.EVENT_ACCEPT_NEW:
                    registerClient(client);
                    break;
                case ClientAcceptTask.EVENT_RECONNECT:
                    client.setNewBluetoothSocket(task.socket);
                    break;
            }
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
            mListAdapter = new DeviceAdapter(context, mTempClients);
            mTempClients.clear();
        }

        return mListAdapter;
    }

    public void searchForDevices() {
        if (mAcceptThread == null || !mAcceptThread.isAlive()) {
            mAcceptThread = new ClientAcceptThread();
            mAcceptThread.start();
        }
    }

    public ArrayList<ScoutClient> getClientList() {
        return mListAdapter.getDeviceList();
    }

    public void cancelSearch() {
        if (mAcceptThread != null) {
            mAcceptThread.cancelClientAccept();
        }
    }

    public boolean isSearching() {
        return mAcceptThread != null && mAcceptThread.isAlive();
    }

    void handleAcceptedClient(ClientAcceptTask task, int event) {
        Log.v(TAG, "Handling accepted client");
        Message acceptMessage = mHandler.obtainMessage(event, task);
        acceptMessage.sendToTarget();
    }

    private void registerClient(ScoutClient client) {
        if (mListAdapter != null) {
            mListAdapter.add(client);
        } else {
            mTempClients.add(client);
        }
    }

    private void removeClient(int pos) {
        if (mListAdapter != null) {
            mListAdapter.remove(pos);
        } else {
            mTempClients.remove(pos);
        }
    }
}