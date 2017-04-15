package com.walpolerobotics.scouting.scoutingserver.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.walpolerobotics.scouting.scoutingserver.R;
import com.walpolerobotics.scouting.scoutingserver.frcapi.FRCApi;
import com.walpolerobotics.scouting.scoutingserver.frcapi.Schedule;

public class DownloadScheduleDialog extends DialogFragment {

    private FRCApi mApi;

    private EditText mEventCodeInput;
    private RadioGroup mMatchTypeInput;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);

        ViewGroup view = (ViewGroup) View.inflate(getContext(), R.layout.dialog_download_schedule,
                null);

        mEventCodeInput = (EditText) view.findViewById(R.id.eventCodeInput);
        mMatchTypeInput = (RadioGroup) view.findViewById(R.id.matchTypeInput);

        builder.setTitle(R.string.dialog_download_schedule_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (mApi != null) {
                            String eventCode = mEventCodeInput.getText().toString();
                            String matchType;
                            switch (mMatchTypeInput.getCheckedRadioButtonId()) {
                                case R.id.radioQuals:
                                    matchType = FRCApi.TOURNAMENT_LEVEL_QUALIFICATIONS;
                                    break;
                                case R.id.radioPlayoffs:
                                    matchType = FRCApi.TOURNAMENT_LEVEL_PLAYOFF;
                                    break;
                                default:
                                    matchType = FRCApi.TOURNAMENT_LEVEL_QUALIFICATIONS;
                                    break;
                            }
                            mApi.downloadMatchFile(eventCode, matchType, new
                                    FRCApi.MatchFileDownloadedCallback() {
                                @Override
                                public void onMatchFileDownloaded(Schedule schedule) {
                                    if (schedule != null) {
                                        schedule.saveToDeviceStorage();
                                    }
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        return builder.create();
    }

    public void setFRCApi(FRCApi api) {
        mApi = api;
    }
}