package com.projects.kquicho.uwatm8;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.projects.kquicho.uw_api_client.Codes.Subject;

public class GroupSubjectData {
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

}
