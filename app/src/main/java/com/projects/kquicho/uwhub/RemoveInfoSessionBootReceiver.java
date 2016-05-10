package com.projects.kquicho.uwhub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Map;

/**
 * Created by Kevin Quicho on 4/5/2016.
 */
public class RemoveInfoSessionBootReceiver extends BroadcastReceiver {
    public final static String TAG = "RemoveISBootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(context);
            Map<Integer, Long> toRemoveMap = dbHelper.getAllToRemove();

            for (Map.Entry<Integer, Long> entry : toRemoveMap.entrySet())
            {
                Intent i = new Intent(context.getApplicationContext(), RemoveInfoSessionAlarmReceiver.class);
                i.putExtra(RemoveInfoSessionAlarmReceiver.ID, entry.getKey());

                final PendingIntent pIntent = PendingIntent.getBroadcast(context,
                entry.getKey(), i, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.set(AlarmManager.RTC_WAKEUP, entry.getValue(), pIntent);
            }
        }
    }
}
