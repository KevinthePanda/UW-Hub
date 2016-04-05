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
    public final static String INFO_SESSION_MODEL = "infoSessionModel";


    public InfoSessionService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        sendNotification((InfoSessionDBModel)intent.getParcelableExtra(INFO_SESSION_MODEL));
    }

    private void sendNotification(InfoSessionDBModel infoSessionDBModel){
        int id = infoSessionDBModel.getId();
       Log.d("InfoSessionService", "Preparing to send notification...: " + id);
       NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);


        PendingIntent contentIntent = PendingIntent.getActivity(this, id,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Info Session w/ " + infoSessionDBModel.getEmployer())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(infoSessionDBModel.getTime() + " at " + infoSessionDBModel.getLocation())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOnlyAlertOnce(true);


        builder.setContentIntent(contentIntent);
        notificationManager.notify(id, builder.build());
        Log.d(TAG, "Notification sent.");
    }
}
