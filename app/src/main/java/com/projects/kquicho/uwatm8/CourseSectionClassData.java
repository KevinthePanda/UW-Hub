package com.projects.kquicho.uwatm8;

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
