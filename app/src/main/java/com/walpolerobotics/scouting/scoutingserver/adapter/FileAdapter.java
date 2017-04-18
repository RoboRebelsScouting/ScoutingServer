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
import com.walpolerobotics.scouting.scoutingserver.util.SortableArrayList;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private static final String TAG = "FileAdapter";

    private Context mContext;
    private File mParentDirectory;
    private SortableArrayList<File> mFiles = new SortableArrayList<>();
    private Handler mHandler;
    private String[] mExtensions;
    // FileObserver must be a member variable to prevent it from being garbage collected by the VM
    private FileObserver mObserver;
    private Comparator<File> mFileComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            Date do1 = new Date(o1.lastModified());
            Date do2 = new Date(o2.lastModified());
            return do2.compareTo(do1);
        }
    };

    public FileAdapter(Context context, File parentDirectory, String[] extensions) {
        mContext = context;
        mParentDirectory = parentDirectory;
        mHandler = new Handler(mContext.getMainLooper());
        mExtensions = extensions;

        File[] files = mParentDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return acceptFileExtension(name);
            }
        });
        if (files != null) {
            Collections.addAll(mFiles, files);
            Collections.sort(mFiles, mFileComparator);
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
                        if (path == null || acceptFileExtension(path)) {
                            onFileChange(event, path);
                        }
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

    private String getFileExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i > 0) {
            return name.substring(i + 1);
        } else {
            return "";
        }
    }

    private boolean acceptFileExtension(String name) {
        if (mExtensions == null || mExtensions.length == 0) {
            return true;
        }

        String extension = getFileExtension(name);

        for (String test : mExtensions) {
            if (extension.equalsIgnoreCase(test)) {
                return true;
            }
        }
        return false;
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
                File newFile = new File(mParentDirectory, pathName);
                notifyItemInserted(mFiles.add(newFile, mFileComparator));
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