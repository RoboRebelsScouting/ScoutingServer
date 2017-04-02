package com.walpolerobotics.scouting.scoutingserver.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.walpolerobotics.scouting.scoutingserver.R;

public class BluetoothNotEnabledDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_bluetooth_disabled_title)
                .setMessage(R.string.dialog_bluetooth_disabled_msg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the app, it can do nothing without Bluetooth
                        getActivity().finish();
                    }
                });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // Close the app, it can do nothing without Bluetooth
        getActivity().finish();
    }
}