package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

import java.util.ArrayList;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ScoutClient> mDevices = new ArrayList<>();

    public DeviceAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_device, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScoutClient client = mDevices.get(position);
        holder.titleStrip.setBackgroundColor(client.getAllianceColor(mContext));
        holder.deviceRole.setText(client.getAllianceString(mContext));
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(ScoutClient a) {
        mDevices.add(a);
        notifyItemInserted(mDevices.size());
    }

    public void remove(int pos) {
        mDevices.remove(pos);
        notifyItemRemoved(pos);
    }

    public void updateDeviceList(ArrayList<ScoutClient> list) {
        mDevices = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout titleStrip;
        TextView deviceRole;

        private ViewHolder(View itemView) {
            super(itemView);

            titleStrip = (FrameLayout) itemView.findViewById(R.id.titleStrip);
            deviceRole = (TextView) itemView.findViewById(R.id.deviceRole);
        }
    }
}
