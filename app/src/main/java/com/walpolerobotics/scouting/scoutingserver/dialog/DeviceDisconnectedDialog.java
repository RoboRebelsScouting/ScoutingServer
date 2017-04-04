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

public class DeviceDisconnectedDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);

        String deviceName = getArguments().getString("deviceName");
        Resources res = getContext().getResources();
        String msg = res.getString(R.string.dialog_disconnected_msg, deviceName);
        CharSequence formattedMsg;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            formattedMsg = Html.fromHtml(msg, 0);
        } else {
            formattedMsg = Html.fromHtml(msg);
        }

        builder.setTitle(R.string.dialog_disconnected_title)
                .setMessage(formattedMsg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        return builder.create();
    }

    public static DeviceDisconnectedDialog createDialog(String deviceName) {
        DeviceDisconnectedDialog dialog = new DeviceDisconnectedDialog();

        Bundle args = new Bundle();
        args.putString("deviceName", deviceName);

        dialog.setArguments(args);

        return dialog;
    }
}