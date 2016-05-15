package com.projects.kquicho.network.Course;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class CourseSchedule extends  Course{
    private String mNote = null;
    private int mClassNumber = 0;
    private String mSection = null;
    private String mCampus = null;
    private int mAssociatedClass = 0;
    private String mRelatedComponent1 = null;
    private String mRelatedComponent2 = null;
    private int mEnrollmentCapacity = 0;
    private int mEnrollmentTotal = 0;
    private int mWaitingCapacity = 0;
    private int mWaitingTotal = 0;
    private String mTopic = null;
    private Reserves mReserves = null;
    private ArrayList<Classes> mClasses = null;
    private String mHeldWith = null;
    private int mTerm = 0;
    private String mLastUpdated = null;

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public int getClassNumber() {
        return mClassNumber;
    }

    public void setClassNumber(int classNumber) {
        mClassNumber = classNumber;
    }

    public String getSection() {
        return mSection;
    }

    public void setSection(String section) {
        mSection = section;
    }

    public String getCampus() {
        return mCampus;
    }

    public void setCampus(String campus) {
        mCampus = campus;
    }

    public int getAssociatedClass() {
        return mAssociatedClass;
    }

    public void setAssociatedClass(int associatedClass) {
        mAssociatedClass = associatedClass;
    }

    public String getRelatedComponent1() {
        return mRelatedComponent1;
    }

    public void setRelatedComponent1(String relatedComponent1) {
        mRelatedComponent1 = relatedComponent1;
    }

    public String getRelatedComponent2() {
        return mRelatedComponent2;
    }

    public void setRelatedComponent2(String relatedComponent2) {
        mRelatedComponent2 = relatedComponent2;
    }

    public int getEnrollmentCapacity() {
        return mEnrollmentCapacity;
    }

    public void setEnrollmentCapacity(int enrollmentCapacity) {
        mEnrollmentCapacity = enrollmentCapacity;
    }

    public int getEnrollmentTotal() {
        return mEnrollmentTotal;
    }

    public void setEnrollmentTotal(int enrollmentTotal) {
        mEnrollmentTotal = enrollmentTotal;
    }

    public int getWaitingCapacity() {
        return mWaitingCapacity;
    }

    public void setWaitingCapacity(int waitingCapacity) {
        mWaitingCapacity = waitingCapacity;
    }

    public int getWaitingTotal() {
        return mWaitingTotal;
    }

    public void setWaitingTotal(int waitingTotal) {
        mWaitingTotal = waitingTotal;
    }

    public String getTopic() {
        return mTopic;
    }

    public void setTopic(String topic) {
        mTopic = topic;
    }

    public Reserves getReserves() {
        return mReserves;
    }

    public void setReserves(Reserves reserves) {
        mReserves = reserves;
    }

    public ArrayList<Classes> getClasses() {
        return mClasses;
    }

    public void setClasses(ArrayList<Classes> classes) {
        mClasses = classes;
    }

    public String getHeldWith() {
        return mHeldWith;
    }

    public void setHeldWith(String heldWith) {
        mHeldWith = heldWith;
    }

    public int getTerm() {
        return mTerm;
    }

    public void setTerm(int term) {
        mTerm = term;
    }

    public String getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        mLastUpdated = lastUpdated;
    }





}
