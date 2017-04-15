package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.frcapi.Schedule;

import java.util.ArrayList;

public class ScheduleFileAdapter extends RecyclerView.Adapter<ScheduleFileAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Schedule> mSchedules;

    public ScheduleFileAdapter(Context context, ArrayList<Schedule> schedules) {
        mContext = context;
        mSchedules = schedules;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_view, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Schedule schedule = mSchedules.get(position);
        holder.primary.setText(schedule.getEventCode());
    }

    @Override
    public int getItemCount() {
        return mSchedules.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView primary;
        private TextView secondary;
        private ImageView icon;

        private ViewHolder(View itemView) {
            super(itemView);

            primary = (TextView) itemView.findViewById(R.id.firstLine);
            secondary = (TextView) itemView.findViewById(R.id.secondLine);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }
}