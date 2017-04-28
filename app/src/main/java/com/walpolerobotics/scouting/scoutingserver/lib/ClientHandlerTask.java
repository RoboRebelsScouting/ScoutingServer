package com.walpolerobotics.scouting.scoutingserver.lib;

public class ClientHandlerTask {

    public static final int EVENT_SCOUT_CHANGE = 0;
    public static final int EVENT_TEAM_CHANGE = 1;
    public static final int EVENT_FILE_ERROR_EXTERNAL = 2;
    public static final int EVENT_FILE_ERROR_CHECKSUM = 3;
    public static final int EVENT_SOCKET_DISCONNECTED = 4;
    /**
     * Ask the ScoutClient to call the disconnect method, used to deliberately drop connections
     * from the ClientHandlerThread if an exception or other error occurs
     */
    public static final int EVENT_SOCKET_DISCONNECT = 5;

    String scout;
    int team;
    String fileName;
}