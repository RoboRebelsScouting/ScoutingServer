package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ScoutClient> mDevices;

    public DeviceAdapter(Context context, ArrayList<ScoutClient> devices) {
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
        ScoutClient client = mDevices.get(position);
        BluetoothDevice device = client.getBluetoothDevice();
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
        client.setClientStateChangeListener(new ScoutClient.ClientStateChangeListener() {
            @Override
            public void onConnected() {
                holder.stateIcon.setImageResource(R.drawable.ic_bluetooth_status_connected);
            }

            @Override
            public void onDisconnected() {
                holder.stateIcon.setImageResource(R.drawable.ic_bluetooth_status_disconnected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(ScoutClient a) {
        mDevices.add(a);
        notifyItemInserted(mDevices.size() - 1);
    }

    public void remove(int pos) {
        mDevices.remove(pos);
        notifyItemRemoved(pos);
    }

    public ArrayList<ScoutClient> getDeviceList() {
        return mDevices;
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