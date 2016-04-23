package com.projects.kquicho.uw_api_client.Codes;

import android.os.Parcel;
import android.os.Parcelable;

public class Subject implements Parcelable{
    private String mSubject = null;
    private String mDescription = null;
    private String mUnit = null;
    private String mGroup = null;

    public Subject(){}

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        this.mUnit = unit;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        this.mGroup = group;
    }

    public Subject(Parcel in){
        mSubject = in.readString();
        mDescription = in.readString();
        mUnit = in.readString();
        mGroup = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSubject);
        dest.writeString(mDescription);
        dest.writeString(mUnit);
        dest.writeString(mGroup);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Subject createFromParcel(Parcel in) {
            return new Subject(in);
        }

        public Subject[] newArray(int size) {
            return new Subject[size];
        }
    };

}
