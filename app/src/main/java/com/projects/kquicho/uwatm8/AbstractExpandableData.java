package com.projects.kquicho.uwatm8;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public abstract class AbstractExpandableData {
    public static abstract class GroupData implements Parcelable{
        protected GroupData(){}
    }

    public static abstract class ChildData implements Parcelable{
        public static final int COURSE_ENROLLMENT_DATA = 1;
        public static final int COURSE_SECTION_CLASS_DATA = 2;
        public static final int COURSE_SECTION_FOOTER_DATA = 3;

        protected ChildData(){}

        protected ChildData(Parcel in){}

        public static final Creator<ChildData> CREATOR = new Creator<ChildData>() {
            @Override
            public ChildData createFromParcel(Parcel source) {

                return ChildData.getConcreteClass(source);
            }

            @Override
            public ChildData[] newArray(int size) {
                return new ChildData[size];
            }
        };

        public static ChildData getConcreteClass(Parcel source) {

            switch (source.readInt()) {
                case COURSE_ENROLLMENT_DATA:
                    return new CourseEnrollmentData(source);
                case COURSE_SECTION_CLASS_DATA:
                    return new CourseSectionClassData(source);
                case COURSE_SECTION_FOOTER_DATA:
                    return new CourseSectionFooterData(source);
                default:
                    return null;
            }
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    public abstract int getGroupCount();
    public abstract int getChildCount(int groupPosition);

    public abstract GroupData getGroupItem(int groupPosition);
    public abstract ChildData getChildItem(int groupPosition, int childPosition);

}
