package com.projects.kquicho.uwatm8;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseScheduleData extends  AbstractExpandableData{
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

    public void clearData(){
        mData.clear();
    }
}
