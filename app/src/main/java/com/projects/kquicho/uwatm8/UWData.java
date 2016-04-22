package com.projects.kquicho.uwatm8;


import android.os.Parcel;
import android.os.Parcelable;

public class UWData implements Parcelable{
    private String mWidgetTag;
    private boolean mPinned = false;
    private boolean mLoading = true;

    protected UWData(){}
    public UWData(String widgetTag){
        mWidgetTag = widgetTag;
    }

    public UWData(Parcel in){
        mWidgetTag = in.readString();
        mPinned = in.readByte() != 0;
        mLoading = in.readByte() != 0;
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

    public boolean isLoading(){
        return mLoading;
    }
    protected void finishedLoading(){
        mLoading = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mWidgetTag);
        dest.writeByte((byte) (mPinned ? 1 : 0));
        dest.writeByte((byte) (mLoading ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UWData createFromParcel(Parcel in) {
            return new UWData(in);
        }

        public UWData[] newArray(int size) {
            return new UWData[size];
        }
    };

}
