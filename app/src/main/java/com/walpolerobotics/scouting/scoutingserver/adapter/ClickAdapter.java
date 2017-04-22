package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class ClickAdapter<T extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<T> {

    protected OnItemClickListener mListener;
    protected OnItemLongClickListener mLongListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public abstract class ClickViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {

        private OnItemClickListener mListener;
        private OnItemLongClickListener mLongListener;

        public ClickViewHolder(View v) {
            super(v);

            v.setClickable(true);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        public void attachToListener(OnItemClickListener listener) {
            mListener = listener;
        }

        public void attachToLongListener(OnItemLongClickListener listener) {
            mLongListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mLongListener != null) {
                mLongListener.onItemLongClick(getAdapterPosition());
            }

            return true;
        }
    }
}