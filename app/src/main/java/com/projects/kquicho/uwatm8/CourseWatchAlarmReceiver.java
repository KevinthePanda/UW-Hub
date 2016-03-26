package com.projects.kquicho.uwatm8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Kevin Quicho on 3/26/2016.
 */
public class CourseWatchAlarmReceiver extends BroadcastReceiver {
    public final static String TAG = "CourseWatchReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Intent intentService = new Intent(context, CourseWatchService.class);
        intentService.putExtra(CourseWatchService.ID, intent.getIntExtra(CourseWatchService.ID, 1));
        intentService.putExtra(CourseWatchService.TITLE, intent.getStringExtra(CourseWatchService.TITLE));
        intentService.putExtra(CourseWatchService.MESSAGE, intent.getStringExtra(CourseWatchService.MESSAGE));
        context.startService(intentService);
    }
}
