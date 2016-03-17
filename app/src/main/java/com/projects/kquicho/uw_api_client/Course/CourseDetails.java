package com.projects.kquicho.uw_api_client.Course;


/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class CourseDetails extends Course {
    private String mInstructions = null;
    private String mPrerequisites = null;
    private String mAntirequisites = null;
    private String mCorequisites = null;
    private String mCrossListings = null;
    private String mTermsOffered = null;
    private String mNotes  =null;
    private Boolean mNeedsDepartmentConsent = false;
    private Boolean mNeedsInstructorConsent = false;
    private String mExtra = null;
    private String mCalendarYear = null;
    private String mUrl = null;

    private Boolean mOnline = null;
    private Boolean mOnlineOnly = null;
    private Boolean mStJeromes = null;
    private Boolean mStJeromesOnly = null;
    private Boolean mRenison = null;
    private Boolean mRenisonOnly = null;
    private Boolean mConradGrebel = null;
    private Boolean mConradGrebelOnly = null;

    public String getInstructions() {

        return mInstructions;
    }

    public void setInstructions(String instructions) {
        instructions = instructions.replace("[", "");
        instructions = instructions.replace("]", "");
        if(instructions.equals("")){
            mTermsOffered = "";
            return;
        }
        instructions = instructions.replace("\"", "");
        instructions = instructions.replace(",", ", ");

        mInstructions = instructions;
    }

    public String getPrerequisites() {
        return mPrerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        mPrerequisites = prerequisites;
    }

    public String getAntirequisites() {
        return mAntirequisites;
    }

    public void setAntirequisites(String antirequisites) {
        mAntirequisites = antirequisites;
    }

    public String getCorequisites() {
        return mCorequisites;
    }

    public void setCorequisites(String corequisites) {
        mCorequisites = corequisites;
    }

    public Boolean getNeedsInstructorConsent() {
        return mNeedsInstructorConsent;
    }

    public String getCrossListings() {
        return mCrossListings;
    }

    public void setCrossListings(String crossListings) {
        mCrossListings = crossListings;
    }

    public String getTermsOffered() {
        return mTermsOffered;
    }

    public void setTermsOffered(String termsOffered) {
        termsOffered = termsOffered.replace("[", "");
        termsOffered = termsOffered.replace("]", "");
        if(termsOffered.equals("")){
            mTermsOffered = termsOffered;
            return;
        }
        termsOffered = termsOffered.replace("\"", "");
        termsOffered = termsOffered.replace(",", ", ");
        mTermsOffered = termsOffered;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public Boolean isNeedsDepartmentConsent() {
        return mNeedsDepartmentConsent;
    }

    public void setNeedsDepartmentConsent(Boolean needsDepartmentConsent) {
        mNeedsDepartmentConsent = needsDepartmentConsent;
    }

    public Boolean isNeedsInstructorConsent() {
        return mNeedsInstructorConsent;
    }

    public void setNeedsInstructorConsent(Boolean needsInstructorConsent) {
        mNeedsInstructorConsent = needsInstructorConsent;
    }

    public String getExtra() {
        return mExtra;
    }

    public void setExtra(String extra) {
        mExtra = extra;
    }

    public String getCalendarYear() {
        return mCalendarYear;
    }

    public void setCalendarYear(String calendarYear) {
        mCalendarYear = calendarYear;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Boolean getNeedsDepartmentConsent() {
        return mNeedsDepartmentConsent;
    }

    public Boolean getOnline() {
        return mOnline;
    }

    public void setOnline(Boolean online) {
        mOnline = online;
    }

    public Boolean getOnlineOnly() {
        return mOnlineOnly;
    }

    public void setOnlineOnly(Boolean onlineOnly) {
        mOnlineOnly = onlineOnly;
    }

    public Boolean getStJeromes() {
        return mStJeromes;
    }

    public void setStJeromes(Boolean stJeromes) {
        mStJeromes = stJeromes;
    }

    public Boolean getStJeromesOnly() {
        return mStJeromesOnly;
    }

    public void setStJeromesOnly(Boolean stJeromesOnly) {
        mStJeromesOnly = stJeromesOnly;
    }

    public Boolean getRenison() {
        return mRenison;
    }

    public void setRenison(Boolean renison) {
        mRenison = renison;
    }

    public Boolean getRenisonOnly() {
        return mRenisonOnly;
    }

    public void setRenisonOnly(Boolean renisonOnly) {
        mRenisonOnly = renisonOnly;
    }

    public Boolean getConradGrebel() {
        return mConradGrebel;
    }

    public void setConradGrebel(Boolean conradGrebel) {
        mConradGrebel = conradGrebel;
    }

    public Boolean getConradGrebelOnly() {
        return mConradGrebelOnly;
    }

    public void setConradGrebelOnly(Boolean conradGrebelOnly) {
        mConradGrebelOnly = conradGrebelOnly;
    }

}
