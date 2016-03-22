package com.projects.kquicho.uwatm8;


/**
 * Created by Kevin Quicho on 3/21/2016.
 */
public class CourseSectionFooterData extends AbstractExpandableData.ChildData{
    private int mClassNumber;


    public CourseSectionFooterData(int classNumber) {
        mClassNumber = classNumber;
    }


    public int getClassNumber(){
        return mClassNumber;
    }
}
