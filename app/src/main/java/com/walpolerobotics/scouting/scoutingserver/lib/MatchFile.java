package com.walpolerobotics.scouting.scoutingserver.lib;

import android.support.annotation.NonNull;

import java.io.File;

public class MatchFile extends File {

    public MatchFile(File parent, @NonNull String child) {
        super(parent, child);
    }

    public String getEvent() {
        String[] indices = getIndices();
        return indices[0];
    }

    public int getMatchNumber() {
        String[] indices = getIndices();
        return Integer.valueOf(indices[1]);
    }

    public int getRobotNumber() {
        String[] indices = getIndices();
        return Integer.valueOf(indices[2]);
    }

    public String getScoutName() {
        String[] indices = getIndices();
        return indices[3];
    }

    private String[] getIndices() {
        String fileName = getName();
        String trimmedFileName = fileName.substring(0, fileName.length() - 4);
        return trimmedFileName.split("-");
    }
}
