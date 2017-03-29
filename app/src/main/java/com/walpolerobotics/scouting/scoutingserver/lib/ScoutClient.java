package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import com.walpolerobotics.scouting.scoutingserver.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ScoutClient {

    public static final int ALLIANCE_RED = 0;
    public static final int ALLIANCE_BLUE = 1;

    public static final int POSITION_1 = 0;
    public static final int POSITION_2 = 1;
    public static final int POSITION_3 = 2;

    public static final int STATE_CONNECTED = 0;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_SEARCHING = 2;

    private static final String FILE_WRITE_LOCATION = "Scouting";

    private static final short MESSAGE_FILE = 1;
    private static final short MESSAGE_SCOUT_CHANGE = 2;
    private static final short MESSAGE_SCOUT_SET = 3;
    private static final short MESSAGE_TEAM_CHANGE = 4;
    private static final short MESSAGE_TEAM_SET = 5;

    private BluetoothSocket mSocket;
    private BluetoothThread mThread;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private int mState;
    private ClientStateChangeListener mStateListener;
    private String mScout;
    private ScoutNameChangeListener mScoutListener;
    private int mTeam;
    private TargetTeamChangeListener mTeamListener;
    private FileTransferErrorListener mFileErrorListener;
    private int mAlliance;
    private int mPosition;

    public ScoutClient(int alliance, int position) {
        mAlliance = alliance;
        mPosition = position;
    }

    public int getAlliance() {
        return mAlliance;
    }

    public int getPosition() {
        return mPosition;
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

    public void setBluetoothSocket(BluetoothSocket socket) {
        mSocket = socket;

        try {
            mInputStream = mSocket.getInputStream();
        } catch (IOException e){
            e.printStackTrace();
        }

        try {
            mOutputStream = mSocket.getOutputStream();
        } catch (IOException e){
            e.printStackTrace();
        }

        if (mThread == null) {
            mThread = new BluetoothThread();
            mThread.start();
        }
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
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class BluetoothThread extends Thread {

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    // Read first to bytes as message type header
                    short msgType = readShort();

                    // Read following bytes based on message type
                    switch (msgType) {
                        case MESSAGE_FILE:
                            fileIn();
                            break;
                        case MESSAGE_SCOUT_CHANGE:
                            scoutIn();
                            break;
                        case MESSAGE_TEAM_CHANGE:
                            teamIn();
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        private void fileIn() throws IOException {
            // First 20 bytes are the SHA-1 checksum of the file
            byte[] preChecksum = new byte[20];
            mInputStream.read(preChecksum);

            // Next 50 bytes are string of the filename
            byte[] fileNameRaw = new byte[50];
            mInputStream.read(fileNameRaw);
            String fileName = new String(fileNameRaw);
            fileName += ".csv";

            // Next 4 bytes are an int representing the amount of bytes of the file
            int fileSize = readInt();

            // Read the file
            byte[] file = new byte[fileSize];
            mInputStream.read(file);

            // Calculate SHA-1 checksum of the incoming file
            byte[] checksum = null;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                md.update(file);
                checksum = md.digest();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            // Cross-check the checksum for transfer errors
            if (MessageDigest.isEqual(preChecksum, checksum)) {
                // Save incoming file to the local filesystem
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    // External media is writable, go ahead and save the file
                    File pathFile = new File(Environment.getExternalStorageDirectory(),
                            FILE_WRITE_LOCATION);
                    File writeFile = new File(pathFile, fileName);

                    FileOutputStream fileOutputStream = new FileOutputStream(writeFile);
                    fileOutputStream.write(file);
                    fileOutputStream.close();
                } else {
                    if (mFileErrorListener != null) {
                        mFileErrorListener.onFileTransferError(ScoutClient.this,
                                FileTransferErrorListener.REASON_EXTERNAL_STORAGE_WRITE_ERROR);
                    }
                }
            } else {
                // Display a message to a data analyst so the transfer error can be corrected
                if (mFileErrorListener != null) {
                    mFileErrorListener.onFileTransferError(ScoutClient.this,
                            FileTransferErrorListener.REASON_CHECKSUM_NOT_EQUAL);
                }
            }
        }

        private void scoutIn() throws IOException {
            // First 2 bytes are a short representing the amount of bytes of the scout name
            short scoutSize = readShort();

            // Read the scout name and convert it to a String
            byte[] scoutName = new byte[scoutSize];
            mInputStream.read(scoutName);
            mScout = new String(scoutName);
            if (mScoutListener != null) {
                mScoutListener.onNameChange(mScout);
            }
        }

        private void teamIn() throws IOException {
            // First 4 bytes are an int representing the FRC team being scouted
            mTeam = readInt();
            if (mTeamListener != null) {
                mTeamListener.onTeamChange(mTeam);
            }
        }

        private short readShort() throws IOException {
            byte[] shortTmp = new byte[2];
            mInputStream.read(shortTmp);
            ByteBuffer bb = ByteBuffer.wrap(shortTmp);
            return bb.getShort();
        }

        private int readInt() throws IOException {
            byte[] intTmp = new byte[4];
            mInputStream.read(intTmp);
            ByteBuffer bb = ByteBuffer.wrap(intTmp);
            return bb.getInt();
        }
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