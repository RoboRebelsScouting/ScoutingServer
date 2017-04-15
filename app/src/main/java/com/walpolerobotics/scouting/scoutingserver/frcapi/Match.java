package com.walpolerobotics.scouting.scoutingserver.frcapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Match {

    public static final int RED_1 = 0;
    public static final int RED_2 = 1;
    public static final int RED_3 = 2;
    public static final int BLUE_1 = 3;
    public static final int BLUE_2 = 4;
    public static final int BLUE_3 = 5;

    private JSONObject mObject;

    public Match(JSONObject in) {
        mObject = in;
    }

    public int getNumber() {
        try {
            return mObject.getInt("matchNumber");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse JSONObject");
        }
    }

    public int getRobot(int pos) {
        try {
            JSONArray teams = mObject.getJSONArray("Teams");
            JSONObject team = teams.getJSONObject(pos);
            return team.getInt("teamNumber");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse JSONObject");
        }
    }

    public int getRed1() {
        return getRobot(RED_1);
    }

    public int getRed2() {
        return getRobot(RED_2);
    }

    public int getRed3() {
        return getRobot(RED_3);
    }

    public int getBlue1() {
        return getRobot(BLUE_1);
    }

    public int getBlue2() {
        return getRobot(BLUE_2);
    }

    public int getBlue3() {
        return getRobot(BLUE_3);
    }
}