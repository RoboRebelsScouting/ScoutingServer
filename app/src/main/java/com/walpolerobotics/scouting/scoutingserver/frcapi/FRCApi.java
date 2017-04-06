package com.walpolerobotics.scouting.scoutingserver.frcapi;

import android.os.AsyncTask;
import android.support.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FRCApi {

    private static final String SEASON = "2017";
    // TODO: Add this to a settings section of the app
    /**
     * Event Code representing the event to pull match schedule from
     * New England District Championship: NECMP
     * FIRST Championship - St. Louis: CMPMO
     */
    private static final String EVENT_CODE = "NECMP";
    /**
     * Tournament Level to filter the desired match schedule
     * Possible Values: qual, playoff
     */
    private static final String TOURNAMENT_LEVEL = "qual";

    private static final String API_BASE = "https://frc-api.firstinspires.org/v2.0/" + SEASON;
    private static final String API_MATCH_SCHEDULE = "/schedule/" + EVENT_CODE + "?tournamentLevel="
            + TOURNAMENT_LEVEL;

    private String mApiKey;

    public FRCApi(String apiKey) {
        mApiKey = apiKey;
    }

    public void downloadMatchFile(final MatchFileDownloadedCallback callback) {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                return downloadMatchFile();
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                callback.onMatchFileDownloaded(result);
            }
        }.execute();
    }

    @WorkerThread
    private JSONObject downloadMatchFile() {
        try {
            URL url = new URL(API_BASE + API_MATCH_SCHEDULE);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return new JSONObject(response.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface MatchFileDownloadedCallback {
        JSONObject onMatchFileDownloaded(JSONObject file);
    }
}