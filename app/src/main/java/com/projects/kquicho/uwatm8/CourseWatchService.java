package com.projects.kquicho.uwatm8;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Terms.TermsParser;

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
    public final static String COURSE_WATCH_DB_MODEL = "courseWatchDBModel";
    public final static String NOTIFICATION_ID = "notificationID";


    public CourseWatchService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent){
        CourseWatchDBModel courseWatchDBModel = intent.getParcelableExtra(COURSE_WATCH_DB_MODEL);
        Log.i("test",courseWatchDBModel.getCourseID() + "");
        TermsParser parse = new TermsParser();
        String url = UWOpenDataAPI.buildURL(parse.getCheckOpenEnrollmentEndPoint(courseWatchDBModel.getURL()));

        InputStream is = null;
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

        parse.setAPIResult(apiResult);
        if(parse.getIsEnrollmentOpen(courseWatchDBModel.getSection())){
            CourseDBHelper dbHelper = CourseDBHelper.getInstance(getApplicationContext());
            dbHelper.deleteCourseWatch(courseWatchDBModel.getCourseID());
            sendNotification(courseWatchDBModel.getTitle(), courseWatchDBModel.getMessage());
        }

    }

    private void sendNotification(String title, String message){
        Log.d(TAG, "Preparing to send notification...: " + title + " " + message);
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences settings = getSharedPreferences("Settings", 0);
        int id = settings.getInt(NOTIFICATION_ID, 1);
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

        //ensures unique notification id
        settings.edit().putInt(NOTIFICATION_ID, id + 1).apply();
        Log.d(TAG, "Notification sent.");
    }
}
