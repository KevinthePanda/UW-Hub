package com.projects.kquicho.uwatm8;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseSectionClassData extends AbstractExpandableData.ChildData {
    private String mStartTime = null;
    private String mEndTime = null;
    private String mWeekdays = null;
    private String mBuilding = null;
    private String mRoom = null;
    private String mCampus = null;
    private String mDate = null;

    private CourseSectionClassData( String startTime, String endTime, String weekdays, String building, String room, String campus, String date) {
        mStartTime = startTime;
        mEndTime = endTime;
        mWeekdays = weekdays;
        mBuilding = building;
        mRoom = room;
        mCampus = campus;
        mDate = date;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public String getWeekdays() {
        return mWeekdays;
    }

    public String getBuilding() {
        return mBuilding;
    }

    public String getRoom() {
        return mRoom;
    }

    public String getCampus() {
        return mCampus;
    }

    public CourseSectionClassData(Parcel in){
        super(in);
        mStartTime = in.readString();
        mEndTime = in.readString();
        mWeekdays = in.readString();
        mBuilding = in.readString();
        mRoom = in.readString();
        mCampus = in.readString();
        mDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(AbstractExpandableData.ChildData.COURSE_SECTION_CLASS_DATA);
        super.writeToParcel(dest, flags);
        dest.writeString(mStartTime);
        dest.writeString(mEndTime);
        dest.writeString(mWeekdays);
        dest.writeString(mBuilding);
        dest.writeString(mRoom);
        dest.writeString(mCampus);
        dest.writeString(mDate);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CourseSectionClassData createFromParcel(Parcel in) {
            return new CourseSectionClassData(in);
        }

        public CourseSectionClassData[] newArray(int size) {
            return new CourseSectionClassData[size];
        }
    };

    public static class Builder{
        private String mStartTime = null;
        private String mEndTime = null;
        private String mWeekdays = null;
        private String mBuilding = null;
        private String mRoom = null;
        private String mCampus = null;
        private String mDate = null;


        public Builder startTime(String startTime){
            mStartTime = startTime;
            return this;
        }

        public Builder endTime(String endTime){
            mEndTime = endTime;
            return this;
        }

        public Builder weekdays(String weekdays){
            mWeekdays = weekdays;
            return this;
        }

        public Builder building(String building){
            mBuilding = building;
            return this;
        }

        public Builder room(String room){
            mRoom = room;
            return this;
        }

        public Builder campus(String campus){
            mCampus = campus;
            return this;
        }

        public Builder date(String date){
            mDate = date;
            return this;
        }


        public CourseSectionClassData createCourseSectionClassData(){
            return new CourseSectionClassData(mStartTime, mEndTime, mWeekdays, mBuilding, mRoom, mCampus, mDate);
        }

    }
}
