package com.projects.kquicho.uwatm8;


import android.util.Log;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

public class InfoSessionData {
    private InfoSession mInfoSession;
    private boolean mIsAlertSet;
    private boolean mPinned = false;

    public InfoSessionData(InfoSession infoSession, boolean isAlertSet){
        mInfoSession = infoSession;
        mIsAlertSet = isAlertSet;
    }

    public InfoSession getInfoSession(){
        return mInfoSession;
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
