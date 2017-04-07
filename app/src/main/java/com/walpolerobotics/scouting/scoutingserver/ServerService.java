package com.walpolerobotics.scouting.scoutingserver;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.adapter.DeviceAdapter;
import com.walpolerobotics.scouting.scoutingserver.lib.ClientAcceptTask;
import com.walpolerobotics.scouting.scoutingserver.lib.ClientAcceptThread;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.util.ArrayList;

public class ServerService extends Service {

    private static final String TAG = "ServerService";

    private ServerBinder mBinder = new ServerBinder();

    private ArrayList<ScoutClient> mClients = new ArrayList<>();
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

    public ServerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public ServerBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Service destroyed");
    }

    public DeviceAdapter getListAdapter(AppCompatActivity context) {
        if (mListAdapter == null) {
            mListAdapter = new DeviceAdapter(context, mClients);
        }

        return mListAdapter;
    }

    public void searchForDevices() {
        if (mAcceptThread == null || !mAcceptThread.isAlive()) {
            mAcceptThread = new ClientAcceptThread(this);
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
        Log.v(TAG, "Cancelled searching");
    }

    public boolean isSearching() {
        Log.v(TAG, "Accept Thread: " + mAcceptThread);
        return mAcceptThread != null && mAcceptThread.isAlive();
    }

    public void handleAcceptedClient(ClientAcceptTask task, int event) {
        Log.v(TAG, "Handling accepted client");
        Message acceptMessage = mHandler.obtainMessage(event, task);
        acceptMessage.sendToTarget();
    }

    private void registerClient(ScoutClient client) {
        mClients.add(client);
    }

    private void removeClient(int pos) {
        mClients.remove(pos);
    }

    public class ServerBinder extends Binder {
        public ServerService getInstance() {
            return ServerService.this;
        }
    }
}