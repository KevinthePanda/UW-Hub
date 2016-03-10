package com.projects.kquicho.uwatm8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class InfoSessionAlarmReceiver extends BroadcastReceiver {
    public final static String TAG = "InfoSessionAlarmReceive";
    public final static String INFO_SESSION_MODEL = "infoSessionModel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        InfoSessionDBModel infoSessionDBModel = intent.getParcelableExtra(INFO_SESSION_MODEL);
        Intent intentService = new Intent(context, InfoSessionService.class);
        intentService.putExtra(InfoSessionService.INFO_SESSION_MODEL, infoSessionDBModel);
        context.startService(intentService);
    }

}
