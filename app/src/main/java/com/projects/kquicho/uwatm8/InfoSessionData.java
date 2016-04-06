package com.projects.kquicho.uwatm8;


import com.projects.kquicho.uw_api_client.Resources.InfoSession;

public class InfoSessionData {
    private InfoSession mInfoSession;
    private boolean mIsAlertSet;
    private boolean mPinned = false;
    private long mTime;

    public InfoSessionData(InfoSession infoSession, boolean isAlertSet, long time){
        mInfoSession = infoSession;
        mIsAlertSet = isAlertSet;
        mTime = time;
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



}
