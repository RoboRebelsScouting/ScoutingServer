package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BluetoothBroadcastRe...";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            // This is simply testing code to see if one of these events is called sooner
            Log.v(TAG, "Bluetooth disconnect requested");
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            // Device has disconnected
            BluetoothManager bluetoothManager = BluetoothManager.getBluetoothManager();
            for (ScoutClient client : bluetoothManager.getClientList()) {
                BluetoothDevice clientDevice = client.getBluetoothDevice();
                String clientAddress = clientDevice.getAddress();
                if (clientAddress.equals(device.getAddress())) {
                    client.notifyDisconnect();
                    break;
                }
            }
        }
    }
}