package com.projects.kquicho.uw_api_client.Course;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class Section {
    public final static String SECTION_TAG = "section";
    public final static String DAY_TAG = "day";
    public final static String DATE_TAG = "date";
    public final static String START_TIME_TAG = "start_time";
    public final static String END_TIME_TAG = "end_time";
    public final static String LOCATION_TAG = "location";
    public final static String NOTES_TAG = "notes";

    private String mSection = null;
    private String mDay = null;
    private String mDate = null;
    private String mStartTime = null;
    private String mEndTime = null;
    private String mLocation = null;
    private String mNotes = null;

    public String getSection() {
        return mSection;
    }

    public void setSection(String section) {
        mSection = section;
    }

    public String getDay() {
        return mDay;
    }

    public void setDay(String day) {
        mDay = day;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

}
