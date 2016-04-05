package com.projects.kquicho.uwatm8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Kevin Quicho on 4/5/2016.
 */
public class RemoveInfoSessionAlarmReceiver  extends BroadcastReceiver {
    public final static String TAG = "RemoveISAlarmReceiver";
    public final static String ID = "id";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        int id = intent.getIntExtra(ID, 0);
        Intent intentService = new Intent(context, RemoveInfoSessionDBService.class);
        intentService.putExtra(RemoveInfoSessionDBService.ID, id);
        context.startService(intentService);
    }
}
