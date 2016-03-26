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

import com.projects.kquicho.uw_api_client.Core.APIResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Kevin Quicho on 3/26/2016.
 */
public class CourseWatchService extends IntentService {
    public final static String TAG = "CourseWatchService";
    public final static String ID = "id";
    public final static String TITLE = "title";
    public final static String MESSAGE = "msg";


    public CourseWatchService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        sendNotification(intent.getIntExtra(ID, 1), intent.getStringExtra(TITLE), intent.getStringExtra(MESSAGE));
    }

    private void sendNotification(int id, String title, String message){

        InputStream is = null;
        String url = "";
        try {
            URL requesturl = new URL(url);
            URLConnection connection = requesturl.openConnection();
            is = connection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }

        String rawDownloadedJSON = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n"); // read each line, add to string builder
            }
            is.close();
            rawDownloadedJSON = sb.toString();
        } catch (Exception e) {

        }
        // try to parse the string
        APIResult apiResult = new APIResult();
        try {
            JSONObject jsonObject = new JSONObject(rawDownloadedJSON);

            apiResult.setResultJSON(jsonObject);
            apiResult.setIndex(0);
            apiResult.setRawJSON(rawDownloadedJSON);
            apiResult.setUrl(url);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Preparing to send notification...: " + title + " " + message);
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);


        PendingIntent contentIntent = PendingIntent.getActivity(this, id,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setOnlyAlertOnce(true);


        builder.setContentIntent(contentIntent);
        notificationManager.notify(id, builder.build());
        Log.d(TAG, "Notification sent.");
    }
}
