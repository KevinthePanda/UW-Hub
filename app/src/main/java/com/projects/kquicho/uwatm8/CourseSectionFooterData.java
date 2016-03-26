package com.projects.kquicho.uwatm8;


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

}
