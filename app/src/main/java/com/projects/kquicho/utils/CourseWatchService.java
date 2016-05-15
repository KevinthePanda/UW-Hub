package com.projects.kquicho.utils;

import android.app.AlarmManager;
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

import com.projects.kquicho.activities.CourseTabActivity;
import com.projects.kquicho.database.CourseDBHelper;
import com.projects.kquicho.models.CourseWatchDBModel;
import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.UWOpenDataAPI;
import com.projects.kquicho.network.Terms.TermsParser;
import com.projects.kquicho.utils.CourseWatchAlarmReceiver;
import com.projects.kquicho.uwhub.R;

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
            Intent deleteAlarmIntent = new Intent(getApplicationContext(), CourseWatchAlarmReceiver.class);
            final PendingIntent deletePIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    courseWatchDBModel.getID(), deleteAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager deleteAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            deleteAlarm.cancel(deletePIntent);

            String subject = courseWatchDBModel.getURL().split("/")[1];
            String catalogNumber = courseWatchDBModel.getURL().split("/")[2];
            sendNotification(courseWatchDBModel.getTitle(), courseWatchDBModel.getMessage(), subject,
                    catalogNumber);
        }

    }

    private void sendNotification(String title, String message, String subject, String catalogNumber){
        Log.d(TAG, "Preparing to send notification...: " + title + " " + message);
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences settings = getSharedPreferences("Settings", 0);
        int id = settings.getInt(NOTIFICATION_ID, 1);

        Intent intent = new Intent(this, CourseTabActivity.class);
        intent.putExtra(CourseTabActivity.CATALOG_NUMBER, catalogNumber);
        intent.putExtra(CourseTabActivity.SUBJECT, subject);

        PendingIntent contentIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
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
