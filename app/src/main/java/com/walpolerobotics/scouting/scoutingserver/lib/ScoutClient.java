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

public class ScoutClient {

    private static final String TAG = "ScoutClient";

    public static final int ALLIANCE_RED = 0;
    public static final int ALLIANCE_BLUE = 1;

    public static final int POSITION_1 = 0;
    public static final int POSITION_2 = 1;
    public static final int POSITION_3 = 2;

    public static final int STATE_CONNECTED = 0;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_SEARCHING = 2;

    private final BluetoothDevice mDevice;
    private ClientHandlerThread mThread;
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
                        mFileErrorListener.onFileTransferError(ScoutClient.this,
                                FileTransferErrorListener.REASON_EXTERNAL_STORAGE_WRITE_ERROR);
                    }
                    break;
                case ClientHandlerTask.EVENT_FILE_ERROR_CHECKSUM:
                    if (mFileErrorListener != null) {
                        mFileErrorListener.onFileTransferError(ScoutClient.this,
                                FileTransferErrorListener.REASON_CHECKSUM_NOT_EQUAL);
                    }
                    break;
            }
        }
    };

    private int mState;
    private ClientStateChangeListener mStateListener;
    private String mScout;
    private ScoutNameChangeListener mScoutListener;
    private int mTeam;
    private TargetTeamChangeListener mTeamListener;
    private FileTransferErrorListener mFileErrorListener;
    private int mAlliance;
    private int mPosition;

    public ScoutClient(BluetoothSocket socket) {
        mDevice = socket.getRemoteDevice();
        mThread = new ClientHandlerThread(this, socket);
        mThread.start();
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

    public void setClientStateChangeListener(ClientStateChangeListener listener) {
        mStateListener = listener;
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
        mThread.disconnect();
    }

    public void setNewBluetoothSocket(BluetoothSocket socket) {
        disconnect();
        mThread = new ClientHandlerThread(this, socket);
        mThread.start();
    }

    void handleEvent(ClientHandlerTask task, int state) {
        Message eventMessage = mHandler.obtainMessage(state, task);
        eventMessage.sendToTarget();
    }

    public interface ClientStateChangeListener {
        void onConnected();
        void onDisconnected();
        void onSearching();
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
        void onFileTransferError(ScoutClient client, int reason);
    }
}