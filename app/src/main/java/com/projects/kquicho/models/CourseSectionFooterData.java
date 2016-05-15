package com.projects.kquicho.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/21/2016.
 */
public class CourseSectionFooterData extends AbstractExpandableData.ChildData{
    private int mClassNumber;
    private String mEventID;
    private boolean mIsBeingWatched;


    public CourseSectionFooterData(int classNumber, String eventID, boolean isBeingWatched) {
        mClassNumber = classNumber;
        mEventID = eventID;
        mIsBeingWatched = isBeingWatched;
    }

    public int getClassNumber(){
        return mClassNumber;
    }
    public String getEventID(){
        return mEventID;
    }

    public boolean isBeingWatched(){
        return mIsBeingWatched;
    }

    public void setBeingWatched(boolean beingWatched){
        mIsBeingWatched = beingWatched;
    }

    public void setEventID(String eventID){
        mEventID = eventID;
    }

    public CourseSectionFooterData(Parcel in){
        super(in);
        mClassNumber = in.readInt();
        mEventID = in.readString();
        mIsBeingWatched = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(AbstractExpandableData.ChildData.COURSE_SECTION_FOOTER_DATA);
        super.writeToParcel(dest, flags);
        dest.writeInt(mClassNumber);
        dest.writeString(mEventID);
        dest.writeByte((byte) (mIsBeingWatched ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CourseSectionFooterData createFromParcel(Parcel in) {
            return new CourseSectionFooterData(in);
        }

        public CourseSectionFooterData[] newArray(int size) {
            return new CourseSectionFooterData[size];
        }
    };
}
