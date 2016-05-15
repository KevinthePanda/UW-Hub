package com.projects.kquicho.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/26/2016.
 */
public class CourseWatchDBModel implements Parcelable {
    private String mCourseID;
    private String mSection;
    private String mURL;
    private String mTitle;
    private String mMessage;
    private int mID;

    public CourseWatchDBModel(String section, String term, String subject, String catalogNumber){
        mCourseID = section + " " + subject + catalogNumber + term;
        mSection = section;
        mURL = term + "/" + subject + "/" + catalogNumber;
        mTitle = section + " - " + subject + " " + catalogNumber;
        mMessage = "Section for " + term + " now has an opening";
        mID = 1;
    }
    public CourseWatchDBModel(String courseID, String section, String url, String title, String message, int id) {
        mCourseID = courseID;
        mSection = section;
        mURL = url;
        mTitle = title;
        mMessage = message;
        mID = id;
    }

    public String getSection() {
        return mSection;
    }

    public String getURL() {
        return mURL;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getCourseID() {
        return mCourseID;
    }

    public int getID() {
        return mID;
    }

    public void setID(int id){
        mID = id;
    }

    public CourseWatchDBModel(){}

    public CourseWatchDBModel(Parcel in){
        mSection = in.readString();
        mURL = in.readString();
        mTitle = in.readString();
        mMessage = in.readString();
        mCourseID = in.readString();
        mID = in.readInt();
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public CourseWatchDBModel createFromParcel(Parcel in) {
            return new CourseWatchDBModel(in);
        }

        public CourseWatchDBModel[] newArray(int size) {
            return new CourseWatchDBModel[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSection);
        dest.writeString(mURL);
        dest.writeString(mTitle);
        dest.writeString(mMessage);
        dest.writeString(mCourseID);
        dest.writeInt(mID);
    }

}
