package com.projects.kquicho.uwatm8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Quicho on 3/22/2016.
 */
public class CourseDBHelper extends SQLiteOpenHelper {
    public final static String TAG = "CourseDBHelper";

    private static CourseDBHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "CourseDB";
    private static final int DATABASE_VERSION = 1;

    // Table
    private static final String TABLE_COURSE_EVENTS = "courseEvents";
    private static final String TABLE_COURSE_ENROLLMENT_WATCH = "courseEnrollmentWatch";

    // Table Columns ****************************
    // Common
    private static final String KEY_COURSE_ID = "id";
    private static final String KEY_COURSE_SECTION_ID = "courseSectionId";

    // Course Events
    private static final String KEY_COURSE_EVENT_ID = "eventId";

    // Course Watch
    private static final String KEY_COURSE_WATCH_TITLE = "title";
    private static final String KEY_COURSE_WATCH_MESSAGE = "msg";

    //****************************
    private static final String CREATE_COURSE_EVENTS_TABLE = "CREATE TABLE " + TABLE_COURSE_EVENTS +
            "(" +
            KEY_COURSE_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            KEY_COURSE_SECTION_ID + " TEXT," +
            KEY_COURSE_EVENT_ID + " TEXT" +
            ")";

    private static final String CREATE_COURSE_ENROLLMENT_WATCH_TABLE = "CREATE TABLE " + TABLE_COURSE_ENROLLMENT_WATCH +
            "(" +
            KEY_COURSE_ID + " INTEGER PRIMARY KEY," + // Define a primary key
            KEY_COURSE_SECTION_ID + " TEXT," +
            KEY_COURSE_WATCH_TITLE + " TEXT," +
            KEY_COURSE_WATCH_MESSAGE + " TEXT" +
            ")";

    // Table Create Statements ****************************
    // Course Events



    public static synchronized CourseDBHelper getInstance(Context context){
        if(sInstance == null){
            sInstance = new CourseDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private CourseDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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


        db.execSQL(CREATE_COURSE_EVENTS_TABLE);
        db.execSQL(CREATE_COURSE_ENROLLMENT_WATCH_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE_EVENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE_ENROLLMENT_WATCH);
            onCreate(db);
        }
    }

    public void addGoogleCalendarEvent(String courseId, String eventId){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_COURSE_SECTION_ID, courseId);
            values.put(KEY_COURSE_EVENT_ID, eventId);

            db.insertOrThrow(TABLE_COURSE_EVENTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }
    public void addCourseWatch(String courseId, String title, String msg){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_COURSE_SECTION_ID, courseId);
            values.put(KEY_COURSE_WATCH_TITLE, title);
            values.put(KEY_COURSE_WATCH_MESSAGE, msg);

            db.insertOrThrow(TABLE_COURSE_ENROLLMENT_WATCH, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }



    public String checkForCourseEvent(String courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSE_EVENTS + " WHERE " +
                KEY_COURSE_SECTION_ID + "='" + courseId + "'", null);
        String ret = null;
        if(cursor.moveToFirst()){
            ret = cursor.getString(cursor.getColumnIndex(KEY_COURSE_EVENT_ID));
        }
        cursor.close();
        return ret;
    }

    public boolean checkForCourseWatch(String courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSE_ENROLLMENT_WATCH + " WHERE " +
                KEY_COURSE_SECTION_ID + "='" + courseId + "'", null);
        boolean ret = cursor.moveToFirst();
        cursor.close();
        return ret;
    }


    public void deleteCourseEvent(String eventId){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_COURSE_EVENTS, KEY_COURSE_EVENT_ID + " = ?",
                new String[]{eventId});
    }

    public void deleteCourseWatch(String courseId){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_COURSE_ENROLLMENT_WATCH, KEY_COURSE_SECTION_ID + " = ?",
                new String[]{courseId });
    }


}
