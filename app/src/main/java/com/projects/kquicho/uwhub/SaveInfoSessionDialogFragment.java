package com.projects.kquicho.uwhub;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

/**
 * Created by kquicho on 16-05-11.
 */
public class SaveInfoSessionDialogFragment extends DialogFragment{
    public static final String TAG = "SaveInfoDialogFragment";
    final private static String ITEMS = "items";
    final private static String INFO_SESSION_DATA = "infoSessionData";
    final private static String POSITION = "position";
    final public static String MESSAGE = "message";
    private InfoSessionData mInfoSessionData;

    public static SaveInfoSessionDialogFragment newInstance(CharSequence[] items, InfoSessionData data,
                                                            int position) {
        Bundle args = new Bundle();
        args.putCharSequenceArray(ITEMS, items);
        args.putParcelable(INFO_SESSION_DATA, data);
        args.putInt(POSITION, position);
        SaveInfoSessionDialogFragment fragment = new SaveInfoSessionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Bundle args = getArguments();
        mInfoSessionData = args.getParcelable(INFO_SESSION_DATA);

        return new AlertDialog.Builder(getActivity())
                .setItems(args.getCharSequenceArray(ITEMS), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        int position = args.getInt(POSITION);
                        intent.putExtra(InfoSessionsFragment.POSITION, position);
                        final InfoSession infoSession = mInfoSessionData.getInfoSession();

                        int time;
                        String message;
                        if(which == 0){
                            time = 0;
                            message = getString(R.string.info_session_save_at_time_of_event);
                        }else if (which == 1){
                            time = 600000;
                            message = getString(R.string.info_session_save_before, "10 minutes");

                        }else{
                            time = 1800000;
                            message = getString(R.string.info_session_save_before, "30 minutes");
                        }
                        intent.putExtra(MESSAGE, message);

                        long alarmTime = mInfoSessionData.getTime() - time;
                        int id = infoSession.getId();

                        InfoSessionDBModel infoSessionDBModel = new
                                InfoSessionDBModel(infoSession.getId(), alarmTime, infoSession.getEmployer(),
                                infoSession.getBuildingCode() + " - " + infoSession.getBuildingRoom(),
                                infoSession.getDate(),  infoSession.getDisplay_time_range());
                        InfoSessionDBHelper.getInstance(getActivity().getApplicationContext()).addInfoSession(infoSessionDBModel);

                        Intent serviceIntent = new Intent(getActivity().getApplicationContext(), InfoSessionAlarmReceiver.class);
                        serviceIntent.putExtra(InfoSessionAlarmReceiver.INFO_SESSION_MODEL, infoSessionDBModel);

                        final PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(),
                                id,serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        //set the alarm an hour before the start time
                        alarm.set(AlarmManager.RTC_WAKEUP, alarmTime, pIntent);
                        mInfoSessionData.setPinned(false);
                        Log.d(TAG, "Setting alarm for " + id + " at " + alarmTime);
                        mInfoSessionData.toggleAlert();
                        Fragment fragment = getTargetFragment();
                        if(fragment != null){
                            fragment.onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        }else{
                            ((ActivityDialogListener)getActivity()).onDialogFinish(message);
                        }
                    }
                })
                .create();
    }

    public interface ActivityDialogListener {
        void onDialogFinish(String message);
    }
}
