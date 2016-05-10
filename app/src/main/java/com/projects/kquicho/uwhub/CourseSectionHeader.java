package com.projects.kquicho.uwhub;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseSectionHeader extends AbstractExpandableData.GroupData {

    private String mHeader = null;
    public CourseSectionHeader(long id, String header){
        mHeader = header;
    }

    public String getHeader(){
        return mHeader;
    }


    public CourseSectionHeader(Parcel in){
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
        public CourseSectionHeader createFromParcel(Parcel in) {
            return new CourseSectionHeader(in);
        }

        public CourseSectionHeader[] newArray(int size) {
            return new CourseSectionHeader[size];
        }
    };
}
