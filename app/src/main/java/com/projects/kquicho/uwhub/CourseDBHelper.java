package com.projects.kquicho.uwhub;

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

/**
 * Created by Kevin Quicho on 3/22/2016.
 */
public class CourseDBHelper extends SQLiteOpenHelper {
    public final static String TAG = "CourseDBHelper";
    private Context mContext;

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
    private static final String KEY_COURSE_WATCH_URL = "url";
    private static final String KEY_COURSE_WATCH_SECTION = "section";
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
            KEY_COURSE_WATCH_URL + " TEXT," +
            KEY_COURSE_WATCH_SECTION + " TEXT," +
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
    public int addCourseWatch(CourseWatchDBModel data){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();


        String count = "SELECT count(*) FROM " + TABLE_COURSE_ENROLLMENT_WATCH;
        Cursor cursor = db.rawQuery(count, null);
        cursor.moveToFirst();
        int icount = cursor.getInt(0);
        if(icount == 0){
            ComponentName receiver = new ComponentName(mContext, CourseWatchBootReceiver.class);
            PackageManager pm = mContext.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            Log.d(TAG, "Enabling CourseWatchBootReceiver");
        }
        cursor.close();

        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_COURSE_SECTION_ID, data.getCourseID());
            values.put(KEY_COURSE_WATCH_URL, data.getURL());
            values.put(KEY_COURSE_WATCH_SECTION, data.getSection());
            values.put(KEY_COURSE_WATCH_TITLE, data.getTitle());
            values.put(KEY_COURSE_WATCH_MESSAGE, data.getMessage());

            long ret = db.insertOrThrow(TABLE_COURSE_ENROLLMENT_WATCH, null, values);
            db.setTransactionSuccessful();
            return (int)ret;
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
            return 0;
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

    public int deleteCourseWatch(String courseId){
        SQLiteDatabase db = getWritableDatabase();

        Log.i("test", "deleting course with id: " + courseId);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSE_ENROLLMENT_WATCH + " WHERE " +
                KEY_COURSE_SECTION_ID + "='" + courseId + "'", null);
        if(cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID));
            db.delete(TABLE_COURSE_ENROLLMENT_WATCH, KEY_COURSE_SECTION_ID + " = ?",
                    new String[]{courseId });


            String count = "SELECT count(*) FROM " + TABLE_COURSE_ENROLLMENT_WATCH;
            Cursor countCursor = db.rawQuery(count, null);
            countCursor.moveToFirst();
            int icount = countCursor.getInt(0);
            if(icount == 0){
                ComponentName receiver = new ComponentName(mContext, CourseWatchBootReceiver.class);
                PackageManager pm = mContext.getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
                Log.d(TAG, "Disabling CourseWatchBootReceiver");
            }
            countCursor.close();

            return id;

        }else{
            return 0;
        }
    }


    public List<CourseWatchDBModel> getAllCourseWatches(){
        List<CourseWatchDBModel> courseWatchDBModels = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COURSE_ENROLLMENT_WATCH, null);
        try {
            if (cursor.moveToFirst()) {
                do {

                    String courseID = cursor.getString(cursor.getColumnIndex(KEY_COURSE_SECTION_ID));
                    String url = cursor.getString(cursor.getColumnIndex(KEY_COURSE_WATCH_URL));
                    String section = cursor.getString(cursor.getColumnIndex(KEY_COURSE_WATCH_SECTION));
                    String title = cursor.getString(cursor.getColumnIndex(KEY_COURSE_WATCH_TITLE));
                    String message = cursor.getString(cursor.getColumnIndex(KEY_COURSE_WATCH_MESSAGE));
                    int id = cursor.getInt(cursor.getColumnIndex(KEY_COURSE_ID));
                    CourseWatchDBModel newCourseWatch = new CourseWatchDBModel(courseID, section, url, title, message, id);
                    courseWatchDBModels.add(newCourseWatch);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get info sessions from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return courseWatchDBModels;
    }


}
