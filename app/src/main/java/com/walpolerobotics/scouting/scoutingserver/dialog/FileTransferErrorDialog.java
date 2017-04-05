package com.walpolerobotics.scouting.scoutingserver.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.lib.ScoutClient;

public class FileTransferErrorDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);

        builder.setTitle(R.string.dialog_file_error_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        int errorReason = getArguments().getInt("errorReason");
        int msgResource = 0;
        switch (errorReason) {
            case ScoutClient.FileTransferErrorListener.REASON_CHECKSUM_NOT_EQUAL:
                msgResource = R.string.dialog_file_error_checksum_msg;
                break;
            case ScoutClient.FileTransferErrorListener.REASON_EXTERNAL_STORAGE_WRITE_ERROR:
                msgResource = R.string.dialog_file_error_external_msg;
                break;
        }
        String fileName = getArguments().getString("fileName");
        Resources res = getContext().getResources();
        String msg = res.getString(msgResource, fileName);
        CharSequence formattedMsg;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedMsg = Html.fromHtml(msg, 0);
        } else {
            formattedMsg = Html.fromHtml(msg);
        }
        builder.setMessage(formattedMsg);

        return builder.create();
    }

    public static FileTransferErrorDialog createDialog(int reason, String fileName) {
        FileTransferErrorDialog dialog = new FileTransferErrorDialog();

        Bundle args = new Bundle();
        args.putInt("errorReason", reason);
        args.putString("fileName", fileName);

        dialog.setArguments(args);

        return dialog;
    }
}