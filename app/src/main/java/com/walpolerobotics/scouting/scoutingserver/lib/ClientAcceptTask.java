package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothSocket;

public class ClientAcceptTask {

    static final int EVENT_ACCEPT_NEW = 0;
    static final int EVENT_RECONNECT = 1;

    ScoutClient client;
    BluetoothSocket socket;
}
