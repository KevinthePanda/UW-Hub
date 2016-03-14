package com.projects.kquicho.uwatm8;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class InfoSessionDBHelper extends SQLiteOpenHelper{
    public final static String TAG = "InfoSessionDBHelper";
    private static InfoSessionDBHelper sInstance;
    private Context mContext;

    // Database Info
    private static final String DATABASE_NAME = "InfoSessionDB";
    private static final int DATABASE_VERSION = 1;

    // Table
    private static final String TABLE_INFO_SESSIONS = "infoSessions";

    // Table Columns
    private static final String KEY_INFO_SESSIONS_ID = "id";
    private static final String KEY_INFO_SESSIONS_NAME_EVENT_ID = "eventId";
    private static final String KEY_INFO_SESSIONS_TIME = "userId";
    private static final String KEY_INFO_SESSIONS_TITLE = "title";
    private static final String KEY_INFO_SESSIONS_MSG = "msg";

    public static synchronized InfoSessionDBHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new InfoSessionDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private  InfoSessionDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    // Called when the database connection is being configured.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_INFO_SESSIONS +
                "(" +
                    KEY_INFO_SESSIONS_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                    KEY_INFO_SESSIONS_NAME_EVENT_ID + " INTEGER," +
                    KEY_INFO_SESSIONS_TIME + " INTEGER," +
                    KEY_INFO_SESSIONS_TITLE + " TEXT," +
                    KEY_INFO_SESSIONS_MSG + " TEXT" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO_SESSIONS);
            onCreate(db);
        }
    }

    public void addInfoSession(InfoSessionDBModel infoSession){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        String count = "SELECT count(*) FROM " + TABLE_INFO_SESSIONS;
        Cursor cursor = db.rawQuery(count, null);
        cursor.moveToFirst();
        int icount = cursor.getInt(0);
        if(icount == 0){
            ComponentName receiver = new ComponentName(mContext, InfoSessionBootReceiver.class);
            PackageManager pm = mContext.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            Log.d(TAG, "Enabling InfoSessionBootReceiver");
        }
        cursor.close();

            // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_INFO_SESSIONS_NAME_EVENT_ID, infoSession.getId());
            values.put(KEY_INFO_SESSIONS_TIME, infoSession.getTime());
            values.put(KEY_INFO_SESSIONS_TITLE, infoSession.getTitle());
            values.put(KEY_INFO_SESSIONS_MSG, infoSession.getMsg());

            db.insertOrThrow(TABLE_INFO_SESSIONS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    public List<InfoSessionDBModel> getAllInfoSessions() {
        List<InfoSessionDBModel> infoSessions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INFO_SESSIONS, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    InfoSessionDBModel newInfoSession = new InfoSessionDBModel();
                    newInfoSession.setId(cursor.getInt(cursor.getColumnIndex(KEY_INFO_SESSIONS_NAME_EVENT_ID)));
                    newInfoSession.setTime(cursor.getLong(cursor.getColumnIndex(KEY_INFO_SESSIONS_TIME)));
                    newInfoSession.setTitle(cursor.getString(cursor.getColumnIndex(KEY_INFO_SESSIONS_TITLE)));
                    newInfoSession.setMsg(cursor.getString(cursor.getColumnIndex(KEY_INFO_SESSIONS_MSG)));
                    infoSessions.add(newInfoSession);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get info sessions from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return infoSessions;
    }

    public boolean checkForInfoSession(String id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INFO_SESSIONS + " WHERE " +
                KEY_INFO_SESSIONS_NAME_EVENT_ID + "='" + id +"'", null);
        boolean doesExist =  cursor.moveToFirst();
        cursor.close();
        return doesExist;
    }

    public void deleteInfoSession(InfoSessionDBModel infoSessions){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_INFO_SESSIONS, KEY_INFO_SESSIONS_NAME_EVENT_ID + " = ?",
                new String[]{String.valueOf(infoSessions.getId()) });

        String count = "SELECT count(*) FROM " + TABLE_INFO_SESSIONS;
        Cursor cursor = db.rawQuery(count, null);
        cursor.moveToFirst();
        int icount = cursor.getInt(0);
        if(icount == 0){
            ComponentName receiver = new ComponentName(mContext, InfoSessionBootReceiver.class);
            PackageManager pm = mContext.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Log.d(TAG, "Disabling InfoSessionBootReceiver");
        }
        cursor.close();
    }

    public void deleteAllInfoSessions() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_INFO_SESSIONS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }

}
