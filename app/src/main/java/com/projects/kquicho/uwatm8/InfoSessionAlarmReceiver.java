package com.projects.kquicho.uwatm8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class InfoSessionAlarmReceiver extends BroadcastReceiver {
    public final static String TAG = "InfoSessionAlarmReceive";
    public final static String INFO_SESSION = "infoSession";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        InfoSession infoSession = intent.getParcelableExtra(INFO_SESSION);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        Date date = null;
        try {
            date = format.parse(infoSession.getDate());
        } catch (ParseException exception) {
            Log.e(TAG, "onReceive ParseException: " + exception.getMessage());
        }
        if (date == null) {
            return;
        }
        InfoSessionDBHelper.getInstance(context)
                .addInfoSession(new InfoSessionDBModel(String.valueOf(infoSession.getId()), date.getTime()));


        Intent intentService = new Intent(context, InfoSessionService.class);
        intentService.putExtra(InfoSessionService.ID, infoSession.getId());
        intentService.putExtra(InfoSessionService.EMPLOYER, infoSession.getEmployer());
        intentService.putExtra(InfoSessionService.TIME, infoSession.getStart_time() + "-" + infoSession.getEnd_time());
        intentService.putExtra(InfoSessionService.BUILDING, infoSession.getBuildingCode());
        context.startService(intentService);
    }

}
