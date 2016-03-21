package com.projects.kquicho.uwatm8;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseSectionData extends AbstractExpandableData.GroupData {
    private String mSection = null;
    private String mCampus = null;
    private int mEnrollmentCapacity = 0;
    private int mEnrollmentTotal = 0;
    private String mStartTime = null;
    private String mEndTime = null;
    private String mWeekdays = null;
    private String mBuilding = null;
    private String mRoom = null;
    private String mInstructor = null;
    private String mDate = null;

    private CourseSectionData(long id, String section, String campus, int enrollmentCapacity, int enrollmentTotal, String startTime, String endTime, String weekdays, String building, String room, String instructor, String date) {
        super(id);
        mSection = section;
        mCampus = campus;
        mEnrollmentCapacity = enrollmentCapacity;
        mEnrollmentTotal = enrollmentTotal;
        mStartTime = startTime;
        mEndTime = endTime;
        mWeekdays = weekdays;
        mBuilding = building;
        mRoom = room;
        mInstructor = instructor;
        mDate = date == null ? "" : date;
    }


    public String getSection() {
        return mSection;
    }

    public String getCampus() {
        return mCampus;
    }

    public int getEnrollmentCapacity() {
        return mEnrollmentCapacity;
    }

    public int getEnrollmentTotal() {
        return mEnrollmentTotal;
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

    public String getInstructor() {
        return mInstructor;
    }

    public String getDate() {
        return mDate;
    }

    public static class Builder{
        private String mSection = null;
        private String mCampus = null;
        private int mEnrollmentCapacity = 0;
        private int mEnrollmentTotal = 0;
        private String mStartTime = null;
        private String mEndTime = null;
        private String mWeekdays = null;
        private String mBuilding = null;
        private String mRoom = null;
        private String mInstructor = null;
        private long mId;
        private String mDate = null;

        public Builder(long id){
            mId = id;
        }
        public Builder section(String section){
            mSection = section;
            return this;
        }

        public Builder campus(String campus){
            mCampus = campus;
            return this;
        }

        public Builder enrollmentCapacity(int enrollmentCapacity){
            mEnrollmentCapacity = enrollmentCapacity;
            return this;
        }

        public Builder enrollmentTotal(int enrollmentTotal){
            mEnrollmentTotal = enrollmentTotal;
            return this;
        }

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

        public Builder instructor(String instructor){
            mInstructor = instructor;
            return this;
        }

        public Builder date(String date){
            mDate = date;
            return this;
        }

        public CourseSectionData createCourseSectionData(){
            return new CourseSectionData(mId, mSection, mCampus, mEnrollmentCapacity, mEnrollmentTotal,
                    mStartTime, mEndTime, mWeekdays, mBuilding, mRoom, mInstructor, mDate);
        }
    }

}
