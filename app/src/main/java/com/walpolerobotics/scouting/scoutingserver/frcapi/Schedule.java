package com.walpolerobotics.scouting.scoutingserver.frcapi;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Schedule {

    private static final String TAG = "Schedule";
    private static final String FILE_WRITE_LOCATION = ".scouting/schedules";

    private JSONObject mObject;

    public Schedule(JSONObject in) {
        mObject = in;
    }

    public String getEventName() {
        throw new UnsupportedOperationException();
    }

    public String getEventCode() {
        throw new UnsupportedOperationException();
    }

    public String getType() {
        throw new UnsupportedOperationException();
    }

    public Match getMatch(int matchNumber) {
        throw new UnsupportedOperationException();
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
                        byte[] file = mObject.toString().getBytes();
                        File pathFile = new File(Environment.getExternalStorageDirectory(),
                                FILE_WRITE_LOCATION);
                        File writeFile = new File(pathFile, fileName);
                        if ((!pathFile.exists() && !pathFile.mkdir()) || (!writeFile.exists() &&
                                !writeFile.createNewFile())) {
                            return null;
                        }

                        FileOutputStream fileOutputStream = new FileOutputStream(writeFile);
                        fileOutputStream.write(file);
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "Cannot write to external storage");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                Log.e(TAG, "Successfully wrote schedule to storage");
            }
        }.execute();
    }
}