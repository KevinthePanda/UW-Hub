package com.projects.kquicho.models;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoSessionDBModel implements Parcelable{
    private int mId;
    private long mAlarmTime;
    private String mEmployer;
    private String mLocation;
    private String mDate;
    private String mTime;

    public InfoSessionDBModel(int id, long alarmTime, String employer, String location, String date, String time) {
        mId = id;
        mAlarmTime = alarmTime;
        mEmployer = employer;
        mLocation = location;
        mDate = date;
        mTime = time;
    }

    public InfoSessionDBModel(){}

    public InfoSessionDBModel(Parcel in){
        mId = in.readInt();
        mAlarmTime = in.readLong();
        mEmployer = in.readString();
        mLocation = in.readString();
        mDate = in.readString();
        mTime = in.readString();
    }

    public long getAlarmTime() {
        return mAlarmTime;
    }

    public void setAlarmTime(long alarmTime) {
        mAlarmTime = alarmTime;
    }

    public String getEmployer() {
        return mEmployer;
    }

    public void setEmployer(String employer) {
        mEmployer = employer;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getTime() {
        return mTime;
    }

    public int getId(){
        return mId;
    }
    public void setId(int id){
        mId = id;
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public InfoSessionDBModel createFromParcel(Parcel in) {
            return new InfoSessionDBModel(in);
        }

        public InfoSessionDBModel[] newArray(int size) {
            return new InfoSessionDBModel[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeLong(mAlarmTime);
        dest.writeString(mEmployer);
        dest.writeString(mLocation);
        dest.writeString(mDate);
        dest.writeString(mTime);
    }
}
