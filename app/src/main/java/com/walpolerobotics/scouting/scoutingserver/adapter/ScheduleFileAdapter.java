package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.util.ArrayList;
import java.util.HashMap;

public class ScheduleFileAdapter extends RecyclerView.Adapter<ScheduleFileAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ScoutClient> mDevices;
    private HashMap<ScoutClient, ScoutClient.ClientStateChangeListener> mStateListeners =
            new HashMap<>();

    public ScheduleFileAdapter(Context context, ArrayList<ScoutClient> devices) {
        mContext = context;
        mDevices = devices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_device, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ScoutClient client = mDevices.get(position);
        final BluetoothDevice device = client.getBluetoothDevice();
        holder.primary.setText(device.getName());
        holder.secondary.setText(device.getAddress());
        switch (client.getState()) {
            case ScoutClient.STATE_CONNECTED:
                holder.stateIcon.setImageResource(R.drawable.ic_bluetooth_status_connected);
                break;
            case ScoutClient.STATE_DISCONNECTED:
                holder.stateIcon.setImageResource(R.drawable.ic_bluetooth_status_disconnected);
                break;
        }
        ScoutClient.ClientStateChangeListener listener = new ScoutClient
                .ClientStateChangeListener() {

            @Override
            public void onConnected(ScoutClient client) {
                holder.stateIcon.setImageResource(R.drawable.ic_bluetooth_status_connected);
            }

            @Override
            public void onDisconnected(ScoutClient client) {
                holder.stateIcon.setImageResource(R.drawable.ic_bluetooth_status_disconnected);
            }
        };
        client.addClientStateChangeListener(listener);
        mStateListeners.put(client, listener);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void onDestroyView() {
        for (ScoutClient client : mDevices) {
            client.removeClientStateChangeListener(mStateListeners.get(client));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView primary;
        private TextView secondary;
        private ImageView stateIcon;

        private ViewHolder(View itemView) {
            super(itemView);

            primary = (TextView) itemView.findViewById(R.id.firstLine);
            secondary = (TextView) itemView.findViewById(R.id.secondLine);
            stateIcon = (ImageView) itemView.findViewById(R.id.stateIcon);
        }
    }
}