package com.projects.kquicho.models;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.projects.kquicho.network.Codes.Subject;

public class GroupSubjectData implements Parcelable{
    private Map<String, ArrayList<Subject>> mData;

    public  GroupSubjectData(Map<String, ArrayList<Subject>> data){
        mData = data;
    }


    public int getGroupCount() {
        return mData.size();
    }

    public int getChildCount(int groupPosition) {
        String key = (String)mData.keySet().toArray()[groupPosition];
        return mData.get(key).size();
    }

    public String getGroupItem(int groupPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        return (String)mData.keySet().toArray()[groupPosition];
    }

    public Subject getChildItem(int groupPosition, int childPosition) {
        if (groupPosition < 0 || groupPosition >= getGroupCount()) {
            throw new IndexOutOfBoundsException("groupPosition = " + groupPosition);
        }

        String key = (String)mData.keySet().toArray()[groupPosition];
        final List<Subject> children = mData.get(key);

        if (childPosition < 0 || childPosition >= children.size()) {
            throw new IndexOutOfBoundsException("childPosition = " + childPosition);
        }
        return children.get(childPosition);
    }

    public GroupSubjectData(Parcel in){
        int size = in.readInt();
        mData = new HashMap<>();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            ArrayList<Subject> value = new ArrayList<>();
            in.readTypedList(value, Subject.CREATOR);
            mData.put(key, value);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mData.size());
        for(Map.Entry<String, ArrayList<Subject>> entry : mData.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeTypedList(entry.getValue());
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GroupSubjectData createFromParcel(Parcel in) {
            return new GroupSubjectData(in);
        }

        public GroupSubjectData[] newArray(int size) {
            return new GroupSubjectData[size];
        }
    };

}
