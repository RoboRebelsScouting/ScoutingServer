package com.walpolerobotics.scouting.scoutingserver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.walpolerobotics.scouting.scoutingserver.frcapi.FRCApi;

import org.json.JSONObject;

public class EventScheduleActivity extends AppCompatActivity {

    private FRCApi mApi = new
            FRCApi("***REMOVED***");

    private EditText mEventCodeInput;
    private TextView mSchedulePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventScheduleActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mEventCodeInput = (EditText) findViewById(R.id.eventCode);
        mSchedulePreview = (TextView) findViewById(R.id.previewMatchSchedule);
    }

    public void actionDownload(View view) {
        String eventCode = mEventCodeInput.getText().toString();
        mApi.downloadMatchFile(eventCode, FRCApi.TOURNAMENT_LEVEL_QUALIFICATIONS,
                new FRCApi.MatchFileDownloadedCallback() {
            @Override
            public void onMatchFileDownloaded(JSONObject file) {
                mSchedulePreview.setText(file.toString());
            }
        });
    }
}