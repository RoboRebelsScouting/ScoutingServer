package com.walpolerobotics.scouting.scoutingserver;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.walpolerobotics.scouting.scoutingserver.adapter.FileAdapter;
import com.walpolerobotics.scouting.scoutingserver.dialog.DownloadScheduleDialog;
import com.walpolerobotics.scouting.scoutingserver.frcapi.FRCApi;
import com.walpolerobotics.scouting.scoutingserver.frcapi.Schedule;

import java.io.File;

public class EventScheduleActivity extends AppCompatActivity {

    private FRCApi mApi = new
            FRCApi("***REMOVED***");

    private RecyclerView mList;
    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (NavigationView) findViewById(R.id.drawer);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mDrawer);
            }
        });
        mDrawer.setCheckedItem(R.id.scheduleDrawerAction);
        mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.homeDrawerAction:
                        startActivity(new Intent(EventScheduleActivity.this, MainActivity.class));
                        return true;
                    default:
                        return false;
                }
            }
        });

        mList = (RecyclerView) findViewById(R.id.recyclerView);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        initListAdapter();
    }

    private void initListAdapter() {
        File parentDirectory = new File(Environment.getExternalStorageDirectory(),
                Schedule.FILE_WRITE_LOCATION);
        if (parentDirectory.exists() || parentDirectory.mkdirs()) {
            String[] extensions = {"json"};
            FileAdapter adapter = new FileAdapter(this, parentDirectory, extensions);
            mList.setAdapter(adapter);
        }
    }

    public void actionDownload(View view) {
        FragmentManager fm = getSupportFragmentManager();
        DownloadScheduleDialog dialog = new DownloadScheduleDialog();
        dialog.setFRCApi(mApi);
        dialog.show(fm, "downloadScheduleDialog");
    }
}