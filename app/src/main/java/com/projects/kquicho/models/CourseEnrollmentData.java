package com.projects.kquicho.models;

import android.os.Parcel;
import android.os.Parcelable;

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

    public CourseEnrollmentData(Parcel in){
        super(in);
        mEnrollmentCapacity = in.readInt();
        mEnrollmentTotal = in.readInt();
        mGroup = in.readString();
        mHasAnimationRan = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(AbstractExpandableData.ChildData.COURSE_ENROLLMENT_DATA);
        super.writeToParcel(dest, flags);
        dest.writeInt(mEnrollmentCapacity);
        dest.writeInt(mEnrollmentTotal);
        dest.writeString(mGroup);
        dest.writeByte((byte) (mHasAnimationRan ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CourseEnrollmentData createFromParcel(Parcel in) {
            return new CourseEnrollmentData(in);
        }

        public CourseEnrollmentData[] newArray(int size) {
            return new CourseEnrollmentData[size];
        }
    };
}
