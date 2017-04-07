package com.walpolerobotics.scouting.scoutingserver.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.ServerService;

public class DevicesFragment extends Fragment {

    private static final String TAG = "DevicesFragment";

    public static final int POSITION = 0;
    public static final String FRAGMENT_TITLE = "Devices";

    private ServerService mService;

    private RecyclerView mList;

    public DevicesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), ServerService.class);
        getActivity().bindService(intent, mConnection, Activity.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unbindService(mConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_devices, container, false);

        mList = (RecyclerView) view.findViewById(R.id.recyclerView);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mService != null) {
            mList.setAdapter(mService.getListAdapter((AppCompatActivity) getActivity()));
        }

        return view;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerService.ServerBinder binder = (ServerService.ServerBinder) service;
            mService = binder.getInstance();
            if (mList != null) {
                mList.setAdapter(mService.getListAdapter((AppCompatActivity) getActivity()));
            }
        }
    };
}