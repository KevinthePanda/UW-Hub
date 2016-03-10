package com.projects.kquicho.uwatm8;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class InfoSessionService extends IntentService {
    public final static String TAG = "InfoSessionService";
    public final static String ID = "id";
    public final static String EMPLOYER = "employer";
    public final static String TIME = "time";
    public final static String BUILDING = "building";


    public InfoSessionService(){
        super("InfoSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        String title = intent.getStringExtra(EMPLOYER) + " - Info Session";
        String msg = "From " + intent.getStringExtra(TIME) + " at " + intent.getStringExtra(BUILDING);
        sendNotification(intent.getIntExtra(ID, -1),title,  msg);
    }

    private void sendNotification(int id, String title, String msg){
       Log.d("InfoSessionService", "Preparing to send notification...: " + msg);
       NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);


        PendingIntent contentIntent = PendingIntent.getActivity(this, id,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(msg)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOnlyAlertOnce(true);


        builder.setContentIntent(contentIntent);
        notificationManager.notify(id, builder.build());
        Log.d("AlarmService", "Notification sent.");
    }
}
