package com.projects.kquicho.uw_api_client.Course;


import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class ExamSchedule {
    public String getCourse() {
        return mCourse;
    }

    public void setCourse(String course) {
        mCourse = course;
    }

    public ArrayList<Section> getSections() {
        return mSections;
    }

    public void setSections(ArrayList<Section> sections) {
        mSections = sections;
    }

    private String mCourse = null;
    private ArrayList<Section> mSections = null;

}
