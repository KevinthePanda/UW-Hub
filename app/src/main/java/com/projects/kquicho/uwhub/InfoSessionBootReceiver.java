package com.projects.kquicho.uwhub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class InfoSessionBootReceiver  extends BroadcastReceiver{
    public final static String TAG = "InfoSessionBootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(context);
            List<InfoSessionDBModel> infoSessionDBModels = dbHelper.getAllInfoSessions();

            for(InfoSessionDBModel infoSessionDBModel : infoSessionDBModels) {
                Intent i = new Intent(context.getApplicationContext(), InfoSessionAlarmReceiver.class);
                i.putExtra(InfoSessionAlarmReceiver.INFO_SESSION_MODEL, infoSessionDBModel);

                final PendingIntent pIntent = PendingIntent.getBroadcast(context,
                        infoSessionDBModel.getId(), i, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.set(AlarmManager.RTC_WAKEUP, infoSessionDBModel.getAlarmTime(), pIntent);
            }
        }
    }
}
