package com.walpolerobotics.scouting.scoutingserver.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.walpolerobotics.scouting.scoutingserver.R;

public class DownloadScheduleDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);

        builder.setTitle(R.string.dialog_download_schedule_title)
                .setView(R.layout.dialog_download_schedule)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        return builder.create();
    }

    public static DownloadScheduleDialog createDialog(String deviceName) {
        DownloadScheduleDialog dialog = new DownloadScheduleDialog();

        Bundle args = new Bundle();
        args.putString("deviceName", deviceName);

        dialog.setArguments(args);

        return dialog;
    }
}