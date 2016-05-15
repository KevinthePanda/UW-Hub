package com.projects.kquicho.network.Terms;


public class TermCourse {
    private float mUnits; // Course credits/units
    private String mCatalogNumber; //Registrar assigned class number
    private String mSubject;  // Requested subject acronym
    private String mTitle; // class name and title

    //getters
    public float getUnits(){
        return mUnits;
    }

    public String getCatalogNumber(){
        return mCatalogNumber;
    }

    public String getSubject(){
        return mSubject;
    }

    public String getTitle(){
        return mTitle;
    }

    //setters
    public void setUnits(float units){
        mUnits = units;
    }

    public void setCatalogNumber(String catalogNumber){
        mCatalogNumber = catalogNumber;
    }

    public void setSubject(String subject){
        mSubject = subject;
    }

    public void setTitle(String title){
        mTitle = title;
    }


}
