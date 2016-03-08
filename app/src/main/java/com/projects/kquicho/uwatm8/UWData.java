package com.projects.kquicho.uwatm8;


import com.projects.kquicho.uw_api_client.Core.UWParser;

public class UWData {
    private UWParser mParser;
    private String mWidgetTag;
    private boolean mPinned = false;

    public UWData(UWParser parser, String widgetTag){
        mParser = parser;
        mWidgetTag = widgetTag;
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

    public String getWidgetTag(){
        return mWidgetTag;
    }
}
