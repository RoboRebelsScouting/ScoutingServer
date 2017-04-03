package com.walpolerobotics.scouting.scoutingserver.adapter;

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
        holder.titleStrip.setBackgroundColor(client.getAllianceColor(mContext));
        holder.deviceRole.setText(client.getAllianceString(mContext));
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

        private FrameLayout titleStrip;
        private TextView deviceRole;
        private ImageView stateIcon;

        private ViewHolder(View itemView) {
            super(itemView);

            titleStrip = (FrameLayout) itemView.findViewById(R.id.titleStrip);
            deviceRole = (TextView) itemView.findViewById(R.id.deviceRole);
            stateIcon = (ImageView) itemView.findViewById(R.id.statusIcon);
        }
    }
}