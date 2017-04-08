package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.walpolerobotics.scouting.scoutingserver.ServerService;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BluetoothBroadcastRe...";

    public BluetoothBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, ServerService.class);
        ServerService.ServerBinder binder = (ServerService.ServerBinder) peekService(context,
                serviceIntent);
        if (binder == null) {
            return;
        }
        ServerService service = binder.getInstance();

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) && service != null) {
            // Device has disconnected
            for (ScoutClient client : service.getClientList()) {
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