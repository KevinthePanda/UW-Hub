package com.projects.kquicho.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseSectionHeaderData extends AbstractExpandableData.GroupData {

    private String mHeader = null;
    public CourseSectionHeaderData(long id, String header){
        mHeader = header;
    }

    public String getHeader(){
        return mHeader;
    }


    public CourseSectionHeaderData(Parcel in){
        mHeader = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mHeader);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CourseSectionHeaderData createFromParcel(Parcel in) {
            return new CourseSectionHeaderData(in);
        }

        public CourseSectionHeaderData[] newArray(int size) {
            return new CourseSectionHeaderData[size];
        }
    };
}
