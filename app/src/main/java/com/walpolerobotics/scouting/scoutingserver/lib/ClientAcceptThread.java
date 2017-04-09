package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.ServerService;

import java.io.IOException;
import java.util.UUID;

public class ClientAcceptThread extends Thread {

    private static final String TAG = "AcceptThread";
    private static final UUID ID = UUID.fromString("35c2ad3a-14dc-11e7-93ae-92361f002671");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private ServerService mService;

    public ClientAcceptThread(ServerService service) {
        mService = service;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void run() {
        try {
            Log.v(TAG, "Beginning search for tablet");
            mServerSocket = mBluetoothAdapter
                    .listenUsingRfcommWithServiceRecord("ScoutingServer", ID);
            iteration: while (!interrupted()) {
                Log.v(TAG, "Now accepting connections");
                try {
                    BluetoothSocket socket = mServerSocket.accept();
                    BluetoothDevice device = socket.getRemoteDevice();
                    for (ScoutClient client : mService.getClientList()) {
                        BluetoothDevice clientDevice = client.getBluetoothDevice();
                        String clientAddress = clientDevice.getAddress();
                        if (clientAddress.equals(device.getAddress())) {
                            Log.v(TAG, "Accepted already connected client: " + device.getName());
                            ClientAcceptTask task = new ClientAcceptTask();
                            task.client = client;
                            task.socket = socket;
                            mService.handleAcceptedClient(task,
                                    ClientAcceptTask.EVENT_RECONNECT);
                            continue iteration;
                        }
                    }
                    Log.v(TAG, "Accepted new client: " + device.getName());
                    ClientAcceptTask task = new ClientAcceptTask();
                    task.client = new ScoutClient(socket);
                    mService.handleAcceptedClient(task, ClientAcceptTask.EVENT_ACCEPT_NEW);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.v(TAG, "No longer accepting connections");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelClientAccept() {
        interrupt();
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
