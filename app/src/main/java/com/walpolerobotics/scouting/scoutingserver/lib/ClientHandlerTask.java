package com.walpolerobotics.scouting.scoutingserver.lib;

class ClientHandlerTask {

    static final int EVENT_SCOUT_CHANGE = 0;
    static final int EVENT_TEAM_CHANGE = 1;
    static final int EVENT_FILE_ERROR_EXTERNAL = 2;
    static final int EVENT_FILE_ERROR_CHECKSUM = 3;

    String scout;
    int team;
}