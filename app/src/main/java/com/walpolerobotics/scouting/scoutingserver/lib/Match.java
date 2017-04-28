package com.walpolerobotics.scouting.scoutingserver.lib;

import java.util.ArrayList;

public class Match {

    private ArrayList<MatchFile> mMatchFiles = new ArrayList<>();
    private int mMatchNumber;

    public Match(int matchNumber) {
        mMatchNumber = matchNumber;
    }

    public int getMatchNumber() {
        return mMatchNumber;
    }

    public void addMatchFile(MatchFile obj) {
        mMatchFiles.add(obj);
    }
}