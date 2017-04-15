package com.walpolerobotics.scouting.scoutingserver.adapter;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.walpolerobotics.scouting.scoutingserver.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private static final String TAG = "FileAdapter";

    private Context mContext;
    private File mParentDirectory;
    private ArrayList<File> mFiles = new ArrayList<>();
    private Handler mHandler;
    private FileObserver mObserver;

    public FileAdapter(Context context, File parentDirectory) {
        mContext = context;
        mParentDirectory = parentDirectory;
        mHandler = new Handler(mContext.getMainLooper());

        File[] files = mParentDirectory.listFiles();
        if (files != null) {
            Collections.addAll(mFiles, files);
        }

        int watchEvents = FileObserver.CREATE | FileObserver.DELETE | FileObserver.DELETE_SELF |
                FileObserver.MOVED_FROM | FileObserver.MOVED_TO | FileObserver.MOVE_SELF;
        mObserver = new FileObserver(mParentDirectory.getPath(), watchEvents) {
            @Override
            public void onEvent(final int event, final String path) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "File Event: " + event + ", File Path: " + path);
                        onFileChange(event, path);
                    }
                });
            }
        };
        mObserver.startWatching();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_view, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File file = mFiles.get(position);
        holder.primary.setText(file.getName());
        holder.secondary.setText(file.getPath());
        holder.icon.setImageResource(R.drawable.ic_file);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    private File getFile(String pathName) {
        for (File file : mFiles) {
            if (file.getName().equals(pathName)) {
                return file;
            }
        }
        return null;
    }

    private void onFileChange(int event, String pathName) {
        switch (event) {
            case FileObserver.CREATE:
            case FileObserver.MOVED_TO:
                int newPos = mFiles.size();
                File newFile = new File(mParentDirectory, pathName);
                mFiles.add(newFile);
                notifyItemInserted(newPos);
                break;
            case FileObserver.DELETE:
            case FileObserver.MOVED_FROM:
                File deletedFile = getFile(pathName);
                int deletedPos = mFiles.indexOf(deletedFile);
                mFiles.remove(deletedFile);
                notifyItemRemoved(deletedPos);
                break;
            case FileObserver.DELETE_SELF:
            case FileObserver.MOVE_SELF:
                mFiles.clear();
                notifyDataSetChanged();
                break;
        }
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