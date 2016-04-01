package com.projects.kquicho.uw_api_client.Course;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class Course implements Parcelable{
    private String mCourseId = null;
    private String mSubject = null;
    private String mCatalogNumber = null;
    private String mTitle = null;
    private String mUnits = null;
    private String mDescription = null;
    private String mAcademicLevel = null;

    public String getCourseId() {
        return mCourseId;
    }

    public void setCourseId(String courseId) {
        this.mCourseId = courseId;
    }


    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }


    public String getCatalogNumber() {
        return mCatalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.mCatalogNumber = catalogNumber;
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }


    public String getUnits() {
        return mUnits;
    }

    public void setUnits(String units) {
        this.mUnits = units;
    }


    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }


    public String getAcademicLevel() {
        return mAcademicLevel;
    }

    public void setAcademicLevel(String academicLevel) {
        this.mAcademicLevel = academicLevel;
    }


    public Course(){

    }

    public Course(Parcel in){
        mCatalogNumber = in.readString();
        mTitle = in.readString();
    }


    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCatalogNumber);
        dest.writeString(mTitle);
    }
}
