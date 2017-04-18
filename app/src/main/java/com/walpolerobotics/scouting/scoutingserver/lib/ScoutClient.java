package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.ServerService;

import java.util.ArrayList;

public class ScoutClient {

    public static final int ALLIANCE_RED = 0;
    public static final int ALLIANCE_BLUE = 1;
    public static final int POSITION_1 = 0;
    public static final int POSITION_2 = 1;
    public static final int POSITION_3 = 2;
    public static final int STATE_CONNECTED = 0;
    public static final int STATE_DISCONNECTED = 1;
    private static final String TAG = "ScoutClient";
    private final BluetoothDevice mDevice;
    private ServerService mParentService;
    private ClientHandlerThread mThread;
    private int mState;
    private ArrayList<ClientStateChangeListener> mStateListeners = new ArrayList<>();
    private String mScout;
    private ScoutNameChangeListener mScoutListener;
    private int mTeam;
    private TargetTeamChangeListener mTeamListener;
    private FileTransferErrorListener mFileErrorListener;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            ClientHandlerTask task = (ClientHandlerTask) inputMessage.obj;
            switch (inputMessage.what) {
                case ClientHandlerTask.EVENT_SCOUT_CHANGE:
                    mScout = task.scout;
                    if (mScoutListener != null) {
                        mScoutListener.onNameChange(mScout);
                    }
                    break;
                case ClientHandlerTask.EVENT_TEAM_CHANGE:
                    mTeam = task.team;
                    if (mTeamListener != null) {
                        mTeamListener.onTeamChange(mTeam);
                    }
                    break;
                case ClientHandlerTask.EVENT_FILE_ERROR_EXTERNAL:
                    if (mFileErrorListener != null) {
                        mFileErrorListener.onFileTransferError(task.fileName,
                                FileTransferErrorListener.REASON_EXTERNAL_STORAGE_WRITE_ERROR);
                    }
                    break;
                case ClientHandlerTask.EVENT_FILE_ERROR_CHECKSUM:
                    if (mFileErrorListener != null) {
                        mFileErrorListener.onFileTransferError(task.fileName,
                                FileTransferErrorListener.REASON_CHECKSUM_NOT_EQUAL);
                    }
                    break;
                case ClientHandlerTask.EVENT_SOCKET_DISCONNECTED:
                    notifyDisconnect();
                    break;
                case ClientHandlerTask.EVENT_SOCKET_DISCONNECT:
                    disconnect();
                    break;
            }
        }
    };
    private int mAlliance;
    private int mPosition;

    public ScoutClient(BluetoothSocket socket, ServerService service) {
        mDevice = socket.getRemoteDevice();
        mParentService = service;
        initHandlerThread(socket);
    }

    public BluetoothDevice getBluetoothDevice() {
        return mDevice;
    }

    public void setAlliancePosition(int alliance, int position) {
        mAlliance = alliance;
        mPosition = position;
    }

    public int getAlliance() {
        return mAlliance;
    }

    public int getPosition() {
        return mPosition;
    }

    public String getScout() {
        return mScout;
    }

    public int getTeam() {
        return mTeam;
    }

    public int getAllianceColor(Context context) {
        switch (mAlliance) {
            case ALLIANCE_BLUE:
                return ContextCompat.getColor(context, R.color.colorAllianceBlue);
            case ALLIANCE_RED:
                return ContextCompat.getColor(context, R.color.colorAllianceRed);
            default:
                return ContextCompat.getColor(context, R.color.colorAllianceBlue);
        }
    }

    public String getAllianceString(Context context) {
        Resources res = context.getResources();

        String allianceColor;
        switch (mAlliance) {
            case ALLIANCE_BLUE:
                allianceColor = res.getString(R.string.alliance_blue);
                break;
            case ALLIANCE_RED:
                allianceColor = res.getString(R.string.alliance_red);
                break;
            default:
                allianceColor = res.getString(R.string.alliance_blue);
                break;
        }

        int position = mPosition + 1;

        return res.getString(R.string.alliance_position, allianceColor, position);
    }

    public int getState() {
        return mState;
    }

    public void addClientStateChangeListener(ClientStateChangeListener listener) {
        mStateListeners.add(listener);
    }

    public void removeClientStateChangeListener(ClientStateChangeListener listener) {
        mStateListeners.remove(listener);
    }

    public void setScoutListener(ScoutNameChangeListener listener) {
        mScoutListener = listener;
    }

    public void setTeamListener(TargetTeamChangeListener listener) {
        mTeamListener = listener;
    }

    public void setFileTransferErrorListener(FileTransferErrorListener listener) {
        mFileErrorListener = listener;
    }

    public void disconnect() {
        mThread.stopLoop();
        mParentService.removeClient(this);
    }

    private void notifyDisconnect() {
        mState = STATE_DISCONNECTED;
        for (ClientStateChangeListener listener : mStateListeners) {
            listener.onDisconnected(this);
        }
    }

    public void setNewBluetoothSocket(BluetoothSocket socket) {
        mThread.stopLoop();
        initHandlerThread(socket);
    }

    private void initHandlerThread(BluetoothSocket socket) {
        mThread = new ClientHandlerThread(this, socket);
        mThread.start();
        mState = STATE_CONNECTED;
        for (ClientStateChangeListener listener : mStateListeners) {
            listener.onConnected(this);
        }
    }

    void handleEvent(ClientHandlerTask task, int state) {
        Message eventMessage = mHandler.obtainMessage(state, task);
        eventMessage.sendToTarget();
    }

    public interface ClientStateChangeListener {
        void onConnected(ScoutClient client);

        void onDisconnected(ScoutClient client);
    }

    public interface ScoutNameChangeListener {
        void onNameChange(String newName);
    }

    public interface TargetTeamChangeListener {
        void onTeamChange(int newTeam);
    }

    public interface FileTransferErrorListener {
        int REASON_CHECKSUM_NOT_EQUAL = 0;
        int REASON_EXTERNAL_STORAGE_WRITE_ERROR = 1;
        void onFileTransferError(String fileName, int reason);
    }
}