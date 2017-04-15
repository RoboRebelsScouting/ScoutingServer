package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClientHandlerThread extends Thread {

    private static final String TAG = "ClientHandlerThread";

    private static final String SOCKET_DISCONNECTED_MESSAGE = "bt socket closed, read return: -1";

    private static final String FILE_WRITE_LOCATION = "scouting/match-files";
    private static final int FILE_MAX_BYTE_SIZE = 16000;

    private static final short MESSAGE_FILE = 1;
    private static final short MESSAGE_SCOUT_CHANGE = 2;
    private static final short MESSAGE_SCOUT_SET = 3;
    private static final short MESSAGE_TEAM_CHANGE = 4;
    private static final short MESSAGE_TEAM_SET = 5;
    private static final short MESSAGE_TEAM_REQUEST = 6;

    private ScoutClient mClient;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    public ClientHandlerThread(ScoutClient client, BluetoothSocket socket) {
        mClient = client;
        setSocket(socket);
    }

    private void setSocket(BluetoothSocket socket) {
        mSocket = socket;

        try {
            mInputStream = socket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't create InputStream");
            e.printStackTrace();
        }

        try {
            mOutputStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't create OutputStream");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                // Read first to bytes as message type header
                short msgType = readShort();
                Log.v(TAG, "Read Header from incoming message: " + msgType);

                // Read following bytes based on message type
                switch (msgType) {
                    case MESSAGE_FILE:
                        Log.v(TAG, "Processing File In");
                        fileIn();
                        break;
                    case MESSAGE_SCOUT_CHANGE:
                        Log.v(TAG, "Processing Scout In");
                        scoutIn();
                        break;
                    case MESSAGE_TEAM_CHANGE:
                        Log.v(TAG, "Processing Team In");
                        teamIn();
                        break;
                    case MESSAGE_TEAM_REQUEST:
                        Log.v(TAG, "Processing Team Request");

                        break;
                }
            } catch (IOException e) {
                if (e.getMessage().equals(SOCKET_DISCONNECTED_MESSAGE)) {
                    mClient.handleEvent(null, ClientHandlerTask.EVENT_SOCKET_DISCONNECTED);
                } else {
                    mClient.handleEvent(null, ClientHandlerTask.EVENT_SOCKET_DISCONNECT);
                    Log.e(TAG, "IOException occurred, deliberately dropping connection with " +
                            "client");
                }
                e.printStackTrace();
                break;
            }
        }
        Log.v(TAG, "Loop broken");
    }

    void stopLoop() {
        interrupt();

        try {
            mInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fileIn() throws IOException {
        // First 20 bytes are the SHA-1 checksum of the file
        byte[] preChecksum = new byte[20];
        mInputStream.read(preChecksum);
        Log.v(TAG, "Read pre-checksum: " + new String(preChecksum));

        // Next 50 bytes are string of the filename
        byte[] fileNameRaw = new byte[50];
        mInputStream.read(fileNameRaw);
        String fileName = new String(fileNameRaw);
        fileName = fileName.trim();
        fileName += ".csv";
        Log.v(TAG, "Read file name: " + fileName);

        // Next 4 bytes are an int representing the amount of bytes of the file
        int fileSize = readInt();
        Log.v(TAG, "Read file size: " + fileSize);

        // Read the file
        if (fileSize > FILE_MAX_BYTE_SIZE) {
            interrupt();
            mSocket.close();
            mInputStream.close();
            mOutputStream.close();
            return;
        }
        byte[] file = new byte[fileSize];
        //Log.v(TAG, "Read bytes: " + mInputStream.read(file));
        byte[] fileByte = new byte[1];
        for (int c = 0; c < fileSize; c++) {
            mInputStream.read(fileByte);
            file[c] = fileByte[0];
        }
        Log.v(TAG, "Read the file");

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
                if ((!pathFile.exists() && !pathFile.mkdir()) || (!writeFile.exists() &&
                        !writeFile.createNewFile())) {
                    // The file and/or directory does not exist and we couldn't instantiate it
                    Log.e(TAG, "Could not create new directory/file");
                    ClientHandlerTask task = new ClientHandlerTask();
                    task.fileName = fileName;
                    mClient.handleEvent(task, ClientHandlerTask.EVENT_FILE_ERROR_EXTERNAL);
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(writeFile);
                    fileOutputStream.write(file);
                    fileOutputStream.close();
                }
            } else {
                Log.e(TAG, "Cannot write to external storage");
                mClient.handleEvent(null, ClientHandlerTask.EVENT_FILE_ERROR_EXTERNAL);
            }
        } else {
            // Display a message to a data analyst so the transfer error can be corrected
            Log.e(TAG, "Checksums did not equal");
            mClient.handleEvent(null, ClientHandlerTask.EVENT_FILE_ERROR_CHECKSUM);
        }
    }

    private void scoutIn() throws IOException {
        // First 2 bytes are a short representing the amount of bytes of the scout name
        short scoutSize = readShort();

        // Read the scout name and convert it to a String
        byte[] scoutName = new byte[scoutSize];
        mInputStream.read(scoutName);
        ClientHandlerTask task = new ClientHandlerTask();
        task.scout = new String(scoutName);
        mClient.handleEvent(task, ClientHandlerTask.EVENT_SCOUT_CHANGE);
    }

    private void teamIn() throws IOException {
        // First 4 bytes are an int representing the FRC team being scouted
        ClientHandlerTask task = new ClientHandlerTask();
        task.team = readInt();
        mClient.handleEvent(task, ClientHandlerTask.EVENT_TEAM_CHANGE);
    }

    private void teamRequest() throws IOException {
        // First byte is a byte representing the Alliance Color of the client
        byte alliance = readByte();
        // Next byte represents the position of the client
        byte client = readByte();
        // Next 2 bytes are a short representing the match number to queue
        short match = readShort();
    }

    private void sendTeam(int team) throws IOException {
        writeShort(MESSAGE_SCOUT_SET);
        writeInt(team);
    }

    private byte readByte() throws IOException {
        byte[] byteTmp = new byte[1];
        mInputStream.read(byteTmp);
        return byteTmp[0];
    }

    private short readShort() throws IOException {
        byte[] shortTmp = new byte[2];
        mInputStream.read(shortTmp);
        ByteBuffer bb = ByteBuffer.wrap(shortTmp);
        return bb.getShort();
    }

    private void writeShort(short in) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.putShort(in);
        mOutputStream.write(bb.array());
    }

    private int readInt() throws IOException {
        byte[] intTmp = new byte[4];
        mInputStream.read(intTmp);
        ByteBuffer bb = ByteBuffer.wrap(intTmp);
        return bb.getInt();
    }

    private void writeInt(int in) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(in);
        mOutputStream.write(bb.array());
    }
}