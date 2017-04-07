package com.walpolerobotics.scouting.scoutingserver.lib;

import android.bluetooth.BluetoothSocket;

public class ClientAcceptTask {

    public static final int EVENT_ACCEPT_NEW = 0;
    public static final int EVENT_RECONNECT = 1;

    public ScoutClient client;
    public BluetoothSocket socket;
}