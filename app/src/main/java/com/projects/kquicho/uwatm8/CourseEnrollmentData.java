package com.projects.kquicho.uwatm8;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseEnrollmentData extends AbstractExpandableData.ChildData {
    private int mEnrollmentCapacity = 0;
    private int mEnrollmentTotal = 0;
    private String mGroup = "";
    private boolean mHasAnimationRan = false;

    public CourseEnrollmentData(int enrollmentCapacity, int enrollmentTotal, String group){
        mEnrollmentCapacity = enrollmentCapacity;
        mEnrollmentTotal = enrollmentTotal;
        mGroup = group;
    }

    public int getEnrollmentTotal() {
        return mEnrollmentTotal;
    }

    public int getEnrollmentCapacity() {
        return mEnrollmentCapacity;
    }

    public boolean hasAnimationRan() {
        return mHasAnimationRan;
    }

    public void setHasAnimationRan(boolean animationRan) {
        mHasAnimationRan = animationRan;
    }

    public String getGroup() {
        return mGroup;
    }
}
