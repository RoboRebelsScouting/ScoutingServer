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

    // Event Code representing the event to pull match schedule from
    public static final String EVENT_CODE_NE_DISTRICT = "NECMP";
    public static final String EVENT_CODE_ST_LOUIS = "CMPMO";

    // Tournament Level to filter the desired match schedule
    public static final String TOURNAMENT_LEVEL_QUALIFICATIONS = "qual";
    public static final String TOURNAMENT_LEVEL_PLAYOFF = "playoff";

    private static final String API_BASE = "https://frc-api.firstinspires.org/v2.0/" + SEASON;

    private String mApiKey;

    public FRCApi(String apiKey) {
        mApiKey = apiKey;
    }

    public void downloadMatchFile(final String event, final String level,
                                  final MatchFileDownloadedCallback callback) {
        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                return downloadMatchFile(event, level);
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                callback.onMatchFileDownloaded(result);
            }
        }.execute();
    }

    @WorkerThread
    private JSONObject downloadMatchFile(String event, String level) {
        try {
            String matchSchedule = "/schedule/" + event + "?tournamentLevel=" + level;
            URL url = new URL(API_BASE + matchSchedule);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Basic " + mApiKey);
            conn.setRequestProperty("Accept", "application/JSON");
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
        void onMatchFileDownloaded(JSONObject file);
    }
}