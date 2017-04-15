package com.walpolerobotics.scouting.scoutingserver.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.ServerService;
import com.walpolerobotics.scouting.scoutingserver.adapter.FileAdapter;
import com.walpolerobotics.scouting.scoutingserver.lib.ClientHandlerThread;

import java.io.File;

public class FilesFragment extends Fragment implements ServerService.OnClientListChanged {

    public static final int POSITION = 1;
    public static final String FRAGMENT_TITLE = "Files";
    private static final String TAG = "FilesFragment";

    private RecyclerView mList;
    private FileAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);

        mList = (RecyclerView) view.findViewById(R.id.recyclerView);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        initListAdapter();

        return view;
    }

    private void initListAdapter() {
        File parentDirectory = new File(Environment.getExternalStorageDirectory(),
                ClientHandlerThread.FILE_WRITE_LOCATION);
        mAdapter = new FileAdapter(getContext(), parentDirectory);
        mList.setAdapter(mAdapter);
    }

    @Override
    public void onClientAdded(int pos) {
        mAdapter.notifyItemInserted(pos);
    }

    @Override
    public void onClientRemoved(int pos) {
        mAdapter.notifyItemRemoved(pos);
    }
}