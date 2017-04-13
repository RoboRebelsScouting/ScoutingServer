package com.walpolerobotics.scouting.scoutingserver.frcapi;

public class Match {

    public static final int RED_1 = 0;
    public static final int RED_2 = 1;
    public static final int RED_3 = 2;
    public static final int BLUE_1 = 3;
    public static final int BLUE_2 = 4;
    public static final int BLUE_3 = 5;

    private int mNumber;
    private int[] mRobots = new int[6];

    public Match(int number, int red1, int red2, int red3, int blue1, int blue2, int blue3) {
        mNumber = number;

        mRobots[RED_1] = red1;
        mRobots[RED_2] = red2;
        mRobots[RED_3] = red3;
        mRobots[BLUE_1] = blue1;
        mRobots[BLUE_2] = blue2;
        mRobots[BLUE_3] = blue3;
    }

    public int getNumber() {
        return mNumber;
    }

    public int getRed1() {
        return mRobots[RED_1];
    }

    public int getRed2() {
        return mRobots[RED_2];
    }

    public int getRed3() {
        return mRobots[RED_3];
    }

    public int getBlue1() {
        return mRobots[BLUE_1];
    }

    public int getBlue2() {
        return mRobots[BLUE_2];
    }

    public int getBlue3() {
        return mRobots[BLUE_3];
    }
}