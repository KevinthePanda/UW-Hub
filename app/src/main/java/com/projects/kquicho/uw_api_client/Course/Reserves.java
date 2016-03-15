package com.projects.kquicho.uw_api_client.Course;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class Reserves{
    private String mReserveGroup = null;
    private int mEnrollmentCapacity = 0;
    private int mEnrollmentTotal = 0;

    public String getReserveGroup() {
        return mReserveGroup;
    }

    public void setReserveGroup(String reserveGroup) {
        mReserveGroup = reserveGroup;
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
}