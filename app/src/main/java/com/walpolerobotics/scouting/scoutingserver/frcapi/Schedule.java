package com.walpolerobotics.scouting.scoutingserver.frcapi;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Schedule {

    private static final String TAG = "Schedule";
    private static final String FILE_WRITE_LOCATION = "Event-Schedules";

    private String mEventCode;
    private String mLevel;
    private JSONArray mArray;

    public Schedule(String eventCode, String level, JSONObject in) {
        mEventCode = eventCode;
        mLevel = level;
        try {
            mArray = in.getJSONArray("Schedule");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse JSONObject");
        }
    }

    public String getEventCode() {
        return mEventCode;
    }

    public String getType() {
        return mLevel;
    }

    public Match getMatch(int matchNumber) {
        try {
            return new Match(mArray.getJSONObject(matchNumber - 1));
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not parse JSONArray");
        }
    }

    public void saveToDeviceStorage() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    try {
                        // External media is writable, go ahead and save the file
                        String fileName = getEventCode() + "-Schedule";

                        JSONObject obj = new JSONObject();
                        obj.put("Schedule", mArray);
                        byte[] file = obj.toString().getBytes();

                        File pathFile = new File(Environment.getExternalStorageDirectory(),
                                FILE_WRITE_LOCATION);
                        File writeFile = new File(pathFile, fileName);
                        if ((!pathFile.exists() && !pathFile.mkdir()) || (!writeFile.exists() &&
                                !writeFile.createNewFile())) {
                            Log.e(TAG, "Could not create new directory/file");
                            return null;
                        }

                        FileOutputStream fileOutputStream = new FileOutputStream(writeFile);
                        fileOutputStream.write(file);
                        fileOutputStream.close();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Cannot write to external storage");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
            }
        }.execute();
    }
}