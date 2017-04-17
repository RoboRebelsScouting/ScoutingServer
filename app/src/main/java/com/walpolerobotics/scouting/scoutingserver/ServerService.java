package com.walpolerobotics.scouting.scoutingserver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.lib.ClientAcceptTask;
import com.walpolerobotics.scouting.scoutingserver.lib.ClientAcceptThread;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.util.ArrayList;

public class ServerService extends Service {

    private static final String TAG = "ServerService";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private IBinder mBinder = new ServerBinder();

    private ArrayList<ScoutClient> mClients = new ArrayList<>();
    private ArrayList<OnClientListChanged> mClientListeners = new ArrayList<>();

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
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent actionIntent = new Intent(this, ServerService.class);
        actionIntent.setAction("requestStopService");
        PendingIntent actionPendingIntent = PendingIntent.getService(this, 0, actionIntent, 0);

        Resources res = getResources();
        String actionTitle = res.getString(R.string.notification_action_stop);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_close_black_24dp, actionTitle,
                        actionPendingIntent).build();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_msg))
                .setSmallIcon(R.drawable.ic_find_replace_white_24px)
                .setContentIntent(pendingIntent)
                .addAction(action)
                .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals("requestStopService")) {
                Log.v(TAG, "Stopping service");
                stopForeground(true);
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Binding to service");
        Log.v(TAG, "onBind - Accept Thread: " + mAcceptThread);
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Service destroyed");
        cancelSearch();
        for (ScoutClient client : mClients) {
            client.disconnect();
        }
    }

    public void searchForDevices() {
        if (mAcceptThread == null || !mAcceptThread.isAlive()) {
            mAcceptThread = new ClientAcceptThread(this);
            mAcceptThread.start();
        }
    }

    public void addOnClientListChangedListener(OnClientListChanged listener) {
        mClientListeners.add(listener);
    }

    public void removeOnClientListChangedListener(OnClientListChanged listener) {
        mClientListeners.remove(listener);
    }

    public ArrayList<ScoutClient> getClientList() {
        return mClients;
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
        for (OnClientListChanged listener : mClientListeners) {
            listener.onClientAdded(mClients.size() - 1);
        }
    }

    public void removeClient(ScoutClient client) {
        int pos = mClients.indexOf(client);
        mClients.remove(pos);
        for (OnClientListChanged listener : mClientListeners) {
            listener.onClientRemoved(pos);
        }
    }

    public interface OnClientListChanged {
        void onClientAdded(int pos);

        void onClientRemoved(int pos);
    }

    public class ServerBinder extends Binder {
        public ServerService getInstance() {
            return ServerService.this;
        }
    }
}