package com.projects.kquicho.uwatm8;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 * Created by Kevin Quicho on 3/26/2016.
 */
public class CourseWatchBootReceiver  extends BroadcastReceiver {
    public final static String TAG = "CourseWatchBootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            CourseDBHelper dbHelper = CourseDBHelper.getInstance(context);
            List<CourseWatchDBModel> dbModels = dbHelper.getAllCourseWatches();

            for(CourseWatchDBModel dbModel : dbModels) {
                Intent i = new Intent(context.getApplicationContext(), CourseWatchAlarmReceiver.class);
                i.putExtra(CourseWatchAlarmReceiver.COURSE_WATCH_DB_MODEL, dbModel);

                final PendingIntent pIntent = PendingIntent.getBroadcast(context,
                        dbModel.getID(), i, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30000,
                        AlarmManager.INTERVAL_HOUR, pIntent);
            }
        }
    }
}