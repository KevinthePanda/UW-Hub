package com.projects.kquicho.uw_api_client.Course;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class Classes{
    private String mStartTime = null;
    private String mEndTime = null;
    private String mWeekdays = null;
    private String mStartDate = null;
    private String mEndDate = null;
    private Boolean mIsTBA = null;
    private Boolean mIsCancelled = null;
    private Boolean mIsClosed = null;
    private String mBuilding = null;
    private String mRoom = null;
    private ArrayList<String> mInstructors = null;

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }

    public String getWeekdays() {
        return mWeekdays;
    }

    public void setWeekdays(String weekdays) {
        mWeekdays = weekdays;
    }

    public String getStartDate() {
        return mStartDate;
    }

    public void setStartDate(String startDate) {
        mStartDate = startDate;
    }

    public String getEndDate() {
        return mEndDate;
    }

    public void setEndDate(String endDate) {
        mEndDate = endDate;
    }

    public Boolean getIsTBA() {
        return mIsTBA;
    }

    public void setIsTBA(Boolean isTBA) {
        mIsTBA = isTBA;
    }

    public Boolean getIsCancelled() {
        return mIsCancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        mIsCancelled = isCancelled;
    }

    public Boolean getIsClosed() {
        return mIsClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        mIsClosed = isClosed;
    }

    public String getBuilding() {
        return mBuilding;
    }

    public void setBuilding(String building) {
        mBuilding = building;
    }

    public String getRoom() {
        return mRoom;
    }

    public void setRoom(String room) {
        mRoom = room;
    }

    public ArrayList<String> getInstructors() {
        return mInstructors;
    }

    public void setInstructors(ArrayList<String> instructors) {

        mInstructors = instructors;
    }

}

