package com.walpolerobotics.scouting.scoutingserver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.lib.ClientAcceptTask;
import com.walpolerobotics.scouting.scoutingserver.lib.ClientAcceptThread;
import com.walpolerobotics.scouting.scoutingserver.lib.ClientHandlerThread;
import com.walpolerobotics.scouting.scoutingserver.lib.Match;
import com.walpolerobotics.scouting.scoutingserver.lib.MatchFile;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.io.File;
import java.util.ArrayList;

public class ServerService extends Service {

    private static final String TAG = "ServerService";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private IBinder mBinder = new ServerBinder();

    private ArrayList<ScoutClient> mClients = new ArrayList<>();
    private ArrayList<OnClientListChanged> mClientListeners = new ArrayList<>();

    private File mParentDirectory = new File(Environment.getExternalStorageDirectory(),
    ClientHandlerThread.FILE_WRITE_LOCATION);
    private FileObserver mObserver;
    private ArrayList<Match> mMatches = new ArrayList<>();
    private ArrayList<MatchUpdateListener> mMatchListeners = new ArrayList<>();

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
        int watchEvents = FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF |
                FileObserver.MOVED_FROM | FileObserver.MOVED_TO | FileObserver.MOVE_SELF;
        mObserver = new FileObserver(mParentDirectory.getPath(), watchEvents) {
            @Override
            public void onEvent(final int event, final String path) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "File Event: " + event + ", File Path: " + path);
                        onFileChange(event, path);
                    }
                });
            }
        };
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

    private void onFileChange(int event, String pathName) {
        switch (event) {
            case FileObserver.CREATE:
            case FileObserver.MOVED_TO:
                MatchFile newFile = new MatchFile(mParentDirectory, pathName);
                // Look to see if this file belongs to an already existing match, if so add it
                int pos = 0;
                for (Match match : mMatches) {
                    if (match.getMatchNumber() == newFile.getMatchNumber()) {
                        match.addMatchFile(newFile);
                        for (MatchUpdateListener listener : mMatchListeners) {
                            listener.onMatchUpdated(pos);
                        }
                        return;
                    }
                    pos++;
                }
                // The file does not belong to an already existing match, create a new match and
                // add it to that match
                Match newMatch = new Match(newFile.getMatchNumber());
                newMatch.addMatchFile(newFile);
                mMatches.add(newMatch);
                for (MatchUpdateListener listener : mMatchListeners) {
                    listener.onNewMatchCreated(mMatches.size() - 1);
                }
                break;
            case FileObserver.DELETE:
            case FileObserver.MOVED_FROM:
                // TODO: What to do if a file is removed from directory?
                break;
            case FileObserver.DELETE_SELF:
            case FileObserver.MOVE_SELF:
                // TODO: What to do if directory is removed?
                break;
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

    public interface MatchUpdateListener {
        void onNewMatchCreated(int pos);
        void onMatchUpdated(int pos);
    }

    public class ServerBinder extends Binder {
        public ServerService getInstance() {
            return ServerService.this;
        }
    }
}