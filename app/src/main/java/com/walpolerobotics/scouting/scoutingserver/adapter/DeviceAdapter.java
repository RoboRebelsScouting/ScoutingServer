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
import com.walpolerobotics.scouting.scoutingserver.ServerService;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> implements
        ServerService.OnClientListChanged, ScoutClient.ClientStateChangeListener {

    private Context mContext;
    private ArrayList<ScoutClient> mDevices;

    public DeviceAdapter(Context context, ArrayList<ScoutClient> devices) {
        mContext = context;
        mDevices = devices;
        for (ScoutClient client : mDevices) {
            client.addClientStateChangeListener(this);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_view, parent, false);

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
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void unregisterListeners() {
        for (ScoutClient client : mDevices) {
            client.removeClientStateChangeListener(this);
        }
    }

    @Override
    public void onClientAdded(int pos) {
        notifyItemInserted(pos);
        ScoutClient client = mDevices.get(pos);
        client.addClientStateChangeListener(this);
    }

    @Override
    public void onClientRemoved(int pos) {
        notifyItemRemoved(pos);
    }

    @Override
    public void onConnected(ScoutClient client) {
        int pos = mDevices.indexOf(client);
        notifyItemChanged(pos);
    }

    @Override
    public void onDisconnected(ScoutClient client) {
        int pos = mDevices.indexOf(client);
        notifyItemChanged(pos);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView primary;
        private TextView secondary;
        private ImageView stateIcon;

        private ViewHolder(View itemView) {
            super(itemView);

            primary = (TextView) itemView.findViewById(R.id.firstLine);
            secondary = (TextView) itemView.findViewById(R.id.secondLine);
            stateIcon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }
}