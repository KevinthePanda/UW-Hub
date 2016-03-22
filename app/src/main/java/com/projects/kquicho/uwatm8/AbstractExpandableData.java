package com.projects.kquicho.uwatm8;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public abstract class AbstractExpandableData {
    public static abstract class GroupData {
        protected GroupData(){

        }
    }

    public static abstract class ChildData {
        protected  ChildData(){

        }
    }

    public abstract int getGroupCount();
    public abstract int getChildCount(int groupPosition);

    public abstract GroupData getGroupItem(int groupPosition);
    public abstract ChildData getChildItem(int groupPosition, int childPosition);

}
