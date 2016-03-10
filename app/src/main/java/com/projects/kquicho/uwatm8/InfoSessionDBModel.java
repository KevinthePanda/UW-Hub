package com.projects.kquicho.uwatm8;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoSessionDBModel implements Parcelable{
    private int mId;
    private long mTime;
    private String mTitle;
    private String mMsg;

    public InfoSessionDBModel(int id, long time, String title, String msg){
        mId = id;
        mTime = time;
        mTitle = title;
        mMsg = msg;
    }
    public InfoSessionDBModel(){}

    public InfoSessionDBModel(Parcel in){
        mId = in.readInt();
        mTime = in.readLong();
        mTitle = in.readString();
        mMsg = in.readString();
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


    public int getId(){
        return mId;
    }
    public long getTime(){
        return mTime;
    }
    public String getTitle(){
        return mTitle;
    }
    public String getMsg(){
        return mMsg;
    }

    public void setId(int id){
        mId = id;
    }
    public void setTime(long time){
        mTime = time;
    }

    public void setTitle(String title){
        mTitle = title;
    }

    public void setMsg(String msg){
        mMsg = msg;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeLong(mTime);
        dest.writeString(mTitle);
        dest.writeString(mMsg);
    }
}
