package com.projects.kquicho.uwatm8;

/**
 * Created by Kevin Quicho on 3/19/2016.
 */
public class CourseSectionHeader extends AbstractExpandableData.GroupData {

    private String mHeader = null;
    public CourseSectionHeader(long id, String header){
        super(id);
        mHeader = header;
    }

    public String getHeader(){
        return mHeader;
    }
}
