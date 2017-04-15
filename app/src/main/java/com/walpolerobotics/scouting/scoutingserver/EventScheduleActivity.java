package com.walpolerobotics.scouting.scoutingserver;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.walpolerobotics.scouting.scoutingserver.adapter.ScheduleFileAdapter;
import com.walpolerobotics.scouting.scoutingserver.dialog.DownloadScheduleDialog;
import com.walpolerobotics.scouting.scoutingserver.frcapi.FRCApi;
import com.walpolerobotics.scouting.scoutingserver.frcapi.Schedule;

import java.util.ArrayList;

public class EventScheduleActivity extends AppCompatActivity {

    private FRCApi mApi = new
            FRCApi("***REMOVED***");

    private RecyclerView mList;
    private ScheduleFileAdapter mAdapter;

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

        mList = (RecyclerView) findViewById(R.id.recyclerView);
        mList.setLayoutManager(new LinearLayoutManager(this));
        initListAdapter();
    }

    private void initListAdapter() {
        // TODO: Actually read schedules from those saved in file system
        ArrayList<Schedule> schedules = new ArrayList<>();
        mAdapter = new ScheduleFileAdapter(this, schedules);
        mList.setAdapter(mAdapter);
    }

    public void actionDownload(View view) {
        FragmentManager fm = getSupportFragmentManager();
        DownloadScheduleDialog dialog = new DownloadScheduleDialog();
        dialog.setFRCApi(mApi);
        dialog.show(fm, "downloadScheduleDialog");
    }
}