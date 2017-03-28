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

        // Temporary code for demonstration
        mDevices.add(new ScoutClient(ScoutClient.ALLIANCE_BLUE, ScoutClient.POSITION_1));
        mDevices.add(new ScoutClient(ScoutClient.ALLIANCE_BLUE, ScoutClient.POSITION_2));
        mDevices.add(new ScoutClient(ScoutClient.ALLIANCE_BLUE, ScoutClient.POSITION_3));
        mDevices.add(new ScoutClient(ScoutClient.ALLIANCE_RED, ScoutClient.POSITION_1));
        mDevices.add(new ScoutClient(ScoutClient.ALLIANCE_RED, ScoutClient.POSITION_2));
        mDevices.add(new ScoutClient(ScoutClient.ALLIANCE_RED, ScoutClient.POSITION_3));
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
