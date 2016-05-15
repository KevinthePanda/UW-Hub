package com.projects.kquicho.utils;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.projects.kquicho.activities.InfoSessionActivity;
import com.projects.kquicho.database.InfoSessionDBHelper;
import com.projects.kquicho.models.InfoSessionDBModel;
import com.projects.kquicho.uwhub.R;

public class InfoSessionService extends IntentService {
    public final static String TAG = "InfoSessionService";
    public final static String INFO_SESSION_MODEL = "infoSessionModel";


    public InfoSessionService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        InfoSessionDBModel infoSessionDBModel = intent.getParcelableExtra(INFO_SESSION_MODEL);
        sendNotification(infoSessionDBModel);

        InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(getApplicationContext());
        dbHelper.addToRemove(infoSessionDBModel.getId(), infoSessionDBModel.getAlarmTime() +  3600000);

        Intent newIntent = new Intent(getApplicationContext(), RemoveInfoSessionAlarmReceiver.class);
        newIntent.putExtra(RemoveInfoSessionAlarmReceiver.ID, infoSessionDBModel.getId());

        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(),
                infoSessionDBModel.getId(),newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //set the alarm an hour before the start time
        alarm.set(AlarmManager.RTC_WAKEUP, infoSessionDBModel.getAlarmTime() +  3600000 , pIntent);
    }

    private void sendNotification(InfoSessionDBModel infoSessionDBModel){
        int id = infoSessionDBModel.getId();
       Log.d("InfoSessionService", "Preparing to send notification...: " + id);
       NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);


        Intent infoSessionIntent = new Intent(this, InfoSessionActivity.class);
        infoSessionIntent.putExtra(InfoSessionActivity.INFO_SESSION_ID, infoSessionDBModel.getId());

        PendingIntent contentIntent = PendingIntent.getActivity(this, id,
                infoSessionIntent , PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Info Session w/ " + infoSessionDBModel.getEmployer())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(infoSessionDBModel.getTime() + " at " + infoSessionDBModel.getLocation())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOnlyAlertOnce(true);


        builder.setContentIntent(contentIntent);
        notificationManager.notify(id, builder.build());
        Log.d(TAG, "Notification sent.");
    }
}
