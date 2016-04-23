package com.projects.kquicho.uwatm8;


import android.os.Parcel;
import android.os.Parcelable;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

public class InfoSessionData implements Parcelable {
    private InfoSession mInfoSession;
    private boolean mIsAlertSet;
    private boolean mPinned = false;
    private long mTime;

    public InfoSessionData(InfoSession infoSession, boolean isAlertSet, long time){
        mInfoSession = infoSession;
        mIsAlertSet = isAlertSet;
        mTime = time;
    }

    private InfoSessionData(Parcel in){
        mInfoSession = in.readParcelable(InfoSession.class.getClassLoader());
        mIsAlertSet = in.readByte() != 0;
        mPinned = in.readByte() != 0;
        mTime = in.readLong();
    }

    public InfoSession getInfoSession(){
        return mInfoSession;
    }
    public long getTime(){
        return mTime;
    }

    public boolean isAlertSet(){
        return mIsAlertSet;
    }

    public boolean toggleAlert(){
        mIsAlertSet = !mIsAlertSet;
        return mIsAlertSet;
    }

    public void setAlert(boolean alert) {
        mIsAlertSet = alert;
    }

    public boolean isPinned(){
        return mPinned;
    }

    public void setPinned(boolean pinned) {
        mPinned = pinned;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public InfoSessionData createFromParcel(Parcel in) {
            return new InfoSessionData(in);
        }

        public InfoSessionData[] newArray(int size) {
            return new InfoSessionData[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mInfoSession, 0);
        dest.writeByte((byte) (mIsAlertSet ? 1 : 0));
        dest.writeByte((byte) (mPinned ? 1 : 0));
        dest.writeLong(mTime);
    }

}
