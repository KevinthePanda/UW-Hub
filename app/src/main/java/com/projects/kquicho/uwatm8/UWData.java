package com.projects.kquicho.uwatm8;


import com.projects.kquicho.uw_api_client.Core.UWParser;

public class UWData {
    private UWParser mParser;
    private boolean mPinned = false;

    public UWData(UWParser parser){
        mParser = parser;
    }
    public UWParser getParser(){
        return mParser;
    }

    public boolean isPinned(){
        return mPinned;
    }

    public void setPinned(boolean pinned) {
        mPinned = pinned;
    }

}
