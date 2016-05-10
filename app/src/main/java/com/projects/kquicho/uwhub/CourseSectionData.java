package com.projects.kquicho.uwhub;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseSectionData extends AbstractExpandableData.GroupData {
    private String mSection = null;
    private String mCampus = null;
    private int mEnrollmentCapacity = 0;
    private int mEnrollmentTotal = 0;
    private String mStartTime = null;
    private String mEndTime = null;
    private String mWeekdays = null;
    private String mBuilding = null;
    private String mRoom = null;
    private String mInstructor = null;
    private String mDate = null;
    private String mEventID = null;

    private CourseSectionData(String section, String campus, int enrollmentCapacity, int enrollmentTotal,
                              String startTime, String endTime, String weekdays, String building,
                              String room, String instructor, String date, String eventID) {
        mSection = section;
        mCampus = campus;
        mEnrollmentCapacity = enrollmentCapacity;
        mEnrollmentTotal = enrollmentTotal;
        mStartTime = startTime;
        mEndTime = endTime;
        mWeekdays = weekdays;
        mBuilding = building;
        mRoom = room;
        mInstructor = instructor;
        mDate = date == null ? "" : date;
        mEventID = eventID;
    }


    public String getSection() {
        return mSection;
    }
    public String getCampus() {
        return mCampus;
    }

    public int getEnrollmentCapacity() {
        return mEnrollmentCapacity;
    }

    public int getEnrollmentTotal() {
        return mEnrollmentTotal;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public String getWeekdays() {
        return mWeekdays;
    }

    public String getBuilding() {
        return mBuilding;
    }

    public String getRoom() {
        return mRoom;
    }

    public String getInstructor() {
        return mInstructor;
    }

    public String getDate() {
        return mDate;
    }


    public String getEventID() {
        return mEventID;
    }
    public void setEventID(String eventID) {
        mEventID = eventID;
    }

    public CourseSectionData(Parcel in){
        mSection = in.readString();
        mCampus = in.readString();
        mEnrollmentCapacity = in.readInt();
        mEnrollmentTotal = in.readInt();
        mStartTime = in.readString();
        mEndTime = in.readString();
        mWeekdays = in.readString();
        mBuilding = in.readString();
        mRoom = in.readString();
        mInstructor = in.readString();
        mDate = in.readString();
        mEventID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSection);
        dest.writeString(mCampus);
        dest.writeInt(mEnrollmentCapacity);
        dest.writeInt(mEnrollmentTotal);
        dest.writeString(mStartTime);
        dest.writeString(mEndTime);
        dest.writeString(mWeekdays);
        dest.writeString(mBuilding);
        dest.writeString(mRoom);
        dest.writeString(mInstructor);
        dest.writeString(mDate);
        dest.writeString(mEventID);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CourseSectionData createFromParcel(Parcel in) {
            return new CourseSectionData(in);
        }

        public CourseSectionData[] newArray(int size) {
            return new CourseSectionData[size];
        }
    };


    public static class Builder{
        private String mSection = null;
        private String mCampus = null;
        private int mEnrollmentCapacity = 0;
        private int mEnrollmentTotal = 0;
        private String mStartTime = null;
        private String mEndTime = null;
        private String mWeekdays = null;
        private String mBuilding = null;
        private String mRoom = null;
        private String mInstructor = null;
        private String mDate = null;
        private String mEventID = null;

        public Builder section(String section){
            mSection = section;
            return this;
        }

        public Builder campus(String campus){
            mCampus = campus;
            return this;
        }

        public Builder enrollmentCapacity(int enrollmentCapacity){
            mEnrollmentCapacity = enrollmentCapacity;
            return this;
        }

        public Builder enrollmentTotal(int enrollmentTotal){
            mEnrollmentTotal = enrollmentTotal;
            return this;
        }

        public Builder startTime(String startTime){
            mStartTime = startTime;
            return this;
        }

        public Builder endTime(String endTime){
            mEndTime = endTime;
            return this;
        }

        public Builder weekdays(String weekdays){
            mWeekdays = weekdays;
            return this;
        }

        public Builder building(String building){
            mBuilding = building;
            return this;
        }

        public Builder room(String room){
            mRoom = room;
            return this;
        }

        public Builder instructor(String instructor){
            mInstructor = instructor;
            return this;
        }

        public Builder date(String date){
            mDate = date;
            return this;
        }

        public Builder eventID(String eventID){
            mEventID = eventID;
            return this;
        }


        public CourseSectionData createCourseSectionData(){
            return new CourseSectionData(mSection, mCampus, mEnrollmentCapacity, mEnrollmentTotal,
                    mStartTime, mEndTime, mWeekdays, mBuilding, mRoom, mInstructor, mDate, mEventID);
        }
    }

}
