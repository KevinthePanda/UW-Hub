package com.projects.kquicho.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.projects.kquicho.models.CourseWatchDBModel;

/**
 * Created by Kevin Quicho on 3/26/2016.
 */
public class CourseWatchAlarmReceiver extends BroadcastReceiver {
    public final static String TAG = "CourseWatchReceiver";
    public final static String COURSE_WATCH_DB_MODEL = CourseWatchService.COURSE_WATCH_DB_MODEL;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        Intent intentService = new Intent(context, CourseWatchService.class);
        CourseWatchDBModel courseWatchDBModel = intent.getParcelableExtra(COURSE_WATCH_DB_MODEL);
        intentService.putExtra(COURSE_WATCH_DB_MODEL, courseWatchDBModel);
        context.startService(intentService);
    }
}
