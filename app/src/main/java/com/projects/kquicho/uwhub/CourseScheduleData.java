package com.projects.kquicho.uwhub;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseScheduleData extends  AbstractExpandableData implements Parcelable{
    ArrayList<Pair<GroupData, ArrayList<ChildData>>> mData;

    public CourseScheduleData(ArrayList<Pair<GroupData, ArrayList<ChildData>>> data){
        mData = data;
    }
    @Override
    public ChildData getChildItem(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        final List<ChildData> children = mData.get(groupPosition).second;

        if (childPosition < 0 || childPosition >= children.size()) {
            throw new IndexOutOfBoundsException("childPosition = " + childPosition);
        }

        return children.get(childPosition);
    }

    @Override
    public GroupData getGroupItem(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        return mData.get(groupPosition).first;
    }

    @Override
    public int getChildCount(int groupPosition) {
        return mData.get(groupPosition).second.size();
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    public void changeData(ArrayList<Pair<GroupData, ArrayList<ChildData>>> data){
        mData.clear();
        mData.addAll(data);
    }

    public CourseScheduleData(Parcel in){
        // ArrayList<Pair<GroupData, ArrayList<ChildData>>> mData;
        int size = in.readInt();
        mData = new ArrayList<>();
        for(int i = 0; i < size; i++){
            GroupData first = in.readParcelable(GroupData.class.getClassLoader());
            ArrayList<ChildData> second = new ArrayList<>();
            in.readTypedList(second, ChildData.CREATOR);
            mData.add(new Pair<>(first, second));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mData.size());
        for(Pair<GroupData, ArrayList<ChildData>> entry : mData){
            dest.writeParcelable(entry.first, 0);
            dest.writeTypedList(entry.second);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public CourseScheduleData createFromParcel(Parcel in) {
            return new CourseScheduleData(in);
        }

        public CourseScheduleData[] newArray(int size) {
            return new CourseScheduleData[size];
        }
    };

}
