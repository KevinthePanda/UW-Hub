package com.projects.kquicho.uw_api_client.Course;


import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.MetaData;
import com.projects.kquicho.uw_api_client.Core.MetaDataParser;
import com.projects.kquicho.uw_api_client.Core.UWParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CourseParser extends UWParser{
    // end point strings
    private static final String DATA_TAG = "data";

    private static final String COURSES_END_POINT = "courses/%s"; // /courses/{subject} - list of courses by subject
    private static final String COURSE_DETAILS_END_POINT = "courses/%s/%s"; // /courses/{subject}/{catalog_number}
    private static final String COURSE_SCHEDULE_END_POINT = "courses/%s/%s/schedule"; // /courses/{subject}/schedule
    private static final String COURSE_EXAM_SCHEDULE_END_POINT = "courses/%s/%s/examschedule"; // /courses/{courses}/{subject}/examschedule

    // JSON Object leaf node tags
    private static final String COURSE_ID_TAG = "course_id";
    private static final String SUBJECT_TAG = "subject";
    private static final String CATALOG_NUMBER_TAG = "catalog_number";
    private static final String TITLE_TAG = "title";
    private static final String UNITS_TAG = "units";
    private static final String DESCRIPTION_TAG = "description";
    private static final String ACADEMIC_LEVEL_TAG = "academic_level";

    private static final String PREREQUISITES_TAG = "prerequisites";
    private static final String ANTI_REQUISITES_TAG = "antirequisites";
    private static final String CO_REQUISITES_TAG = "corequisites";
    private static final String CROSS_LISTINGS_TAG = "crosslistings";
    private static final String NEEDS_DEPARTMENT_CONSENT_TAG = "needs_department_consent";
    private static final String NEEDS_INSTRUCTOR_CONSENT_TAG = "needs_instructor_consent";
    private static final String CALENDAR_YEAR_TAG = "calendar_year";
    private static final String URL_TAG = "url";

    private static final String NOTE_TAG = "note";
    private static final String CLASS_NUMBER_TAG = "class_number";
    private static final String SECTION_TAG = "section";
    private static final String CAMPUS_TAG = "campus";
    private static final String ASSOCIATED_CLASS_TAG = "associated_class";
    private static final String RELATED_COMPONENT_1_TAG = "related_component_1";
    private static final String RELATED_COMPONENT_2_TAG = "related_component_2";
    private static final String ENROLLMENT_CAPACITY_TAG = "enrollment_capacity";
    private static final String ENROLLMENT_TOTAL_TAG = "enrollment_total";
    private static final String WAITING_CAPACITY_TAG = "waiting_capacity";
    private static final String WAITING_TOTAL_TAG = "waiting_total";
    private static final String TOPIC_TAG = "topic";
    private static final String TERM_TAG = "term";
    private static final String LAST_UPDATED_TAG = "last_updated";

    private static final String OFFERINGS_TAG = "offerings";
    private static final String ONLINE_TAG = "online";
    private static final String ONLINE_ONLY_TAG = "online_only";
    private static final String ST_JEROMES_TAG = "st_jerome";
    private static final String ST_JEROMES_ONLY_TAG = "st_jerome_only";
    private static final String RENSION_TAG = "renison";
    private static final String RENISON_ONLY_TAG = "renison_only";
    private static final String CONRAD_GREBEL_TAG = "conrad_grebel";
    private static final String CONRAD_GREBEL_ONLY_TAG = "conrad_grebel_only";
    private static final String INSTRUCTORS_TAG = "instructors";

    private static final String WEEKDAYS_TAG = "weekdays";
    private static final String START_DATE_TAG = "start_date";
    private static final String END_DATE_TAG = "end_date";
    private static final String IS_TBA_TAG = "is_tba";
    private static final String IS_CANCELLED_TAG = "is_cancelled";
    private static final String IS_CLOSED_TAG = "is_closed";

    private static final String BUILDING_TAG = "building";
    private static final String ROOM_TAG = "room";

    private static final String DAY_TAG = "day";
    private static final String DATE_TAG = "date";
    private static final String START_TIME_TAG = "start_time";
    private static final String END_TIME_TAG = "end_time";
    private static final String LOCATION_TAG = "location";
    private static final String NOTES_TAG = "notes";

    private static final String RESERVE_GROUP_TAG = "reserve_group";
    private static final String COURSE_TAG = "course";



    // JSON Array/Object Tag
    private static final String INSTRUCTIONS_TAG = "instructions";
    private static final String TERMS_OFFERED_TAG = "terms_offered";
    private static final String EXTRA_TAG = "extra";
    private static final String RESERVES_TAG = "reserves";
    private static final String CLASSES_TAG = "classes";
    private static final String HELD_WITH_TAG = "held_with";
    private static final String SECTIONS_TAG = "sections";


    // contains all JSON information
    APIResult mApiResult = null;

    public enum ParseType {
        COURSES,
        COURSE_DETAILS,
        COURSE_SCHEDULE,
        EXAM_SCHEDULE
    }

    // /courses/{subject} - list of courses by subject variables
    private ArrayList<Course> mCourses;

    // /courses/{subject}/{catalog_number} variables
    private CourseDetails mCourseDetail = null;

    // /courses/{subject}/schedule variables
    private ArrayList<CourseSchedule> mCourseSchedule = new ArrayList<>();

    // /courses/{courses}/{subject}/examschedule variables
    private ExamSchedule mExamSchedule = null;

    public ArrayList<Course> getCourses() {
        return mCourses;
    }

    public CourseDetails getCourseDetail() {
        return mCourseDetail;
    }

    public ArrayList<CourseSchedule> getCourseSchedules() {
        return mCourseSchedule;
    }

    public ExamSchedule getExamSchedule() {
        return mExamSchedule;
    }

    public ParseType mParseType = ParseType.COURSES;

    public ParseType getParseType() {
        return mParseType;
    }

    public void setParseType(ParseType mParseType) {
        this.mParseType = mParseType;
    }

    @Override
    public void parseJSON() {
        if(mApiResult == null || mApiResult.getResultJSON() == null) return;

        switch (mParseType){
            case COURSES:
                parseCoursesJSON();
                break;
            case COURSE_DETAILS:
                parseCourseDetailsJSON();
                break;
            case COURSE_SCHEDULE:
                parseCourseScheduleJSON();
                break;
            case EXAM_SCHEDULE:
                parseExamScheduleJSON();
                break;
        }
    }

    private void parseCoursesJSON() {
        mCourses = new ArrayList<>();
        try {
            JSONArray coursesArray = mApiResult.getResultJSON().getJSONArray(DATA_TAG);
            for (int i = 0; i < coursesArray.length(); i++) {
                JSONObject courseObject = coursesArray.getJSONObject(i);
                Course course = new Course();
                parseSingleCourse(course, courseObject);
                mCourses.add(course);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }


    private void parseCourseDetailsJSON() {
        try {
            JSONObject courseDetailsObject = mApiResult.getResultJSON().getJSONObject(DATA_TAG);
            mCourseDetail = new CourseDetails();

            parseSingleCourse(mCourseDetail, courseDetailsObject);
            mCourseDetail.setPrerequisites(courseDetailsObject.getString(PREREQUISITES_TAG));
            mCourseDetail.setAntirequisites(courseDetailsObject.getString(ANTI_REQUISITES_TAG));
            mCourseDetail.setCorequisites(courseDetailsObject.getString(CO_REQUISITES_TAG));
            mCourseDetail.setCrossListings(courseDetailsObject.getString(CROSS_LISTINGS_TAG));
            mCourseDetail.setTermsOffered(courseDetailsObject.getString(TERMS_OFFERED_TAG));
            mCourseDetail.setNotes(courseDetailsObject.getString(NOTES_TAG));
            mCourseDetail.setNeedsDepartmentConsent(courseDetailsObject.getBoolean(NEEDS_DEPARTMENT_CONSENT_TAG));
            mCourseDetail.setNeedsInstructorConsent(courseDetailsObject.getBoolean(NEEDS_INSTRUCTOR_CONSENT_TAG));
            mCourseDetail.setExtra(courseDetailsObject.getString(EXTRA_TAG));
            mCourseDetail.setCalendarYear(courseDetailsObject.getString(CALENDAR_YEAR_TAG));
            mCourseDetail.setUrl(courseDetailsObject.getString(URL_TAG));
            mCourseDetail.setInstructions(courseDetailsObject.getString(INSTRUCTIONS_TAG));
            JSONObject offeringsObject = courseDetailsObject.getJSONObject(OFFERINGS_TAG);

            mCourseDetail.setOnline(offeringsObject.getBoolean(ONLINE_TAG));
            mCourseDetail.setOnlineOnly(offeringsObject.getBoolean(ONLINE_ONLY_TAG));
            mCourseDetail.setStJeromes(offeringsObject.getBoolean(ST_JEROMES_TAG));
            mCourseDetail.setStJeromesOnly(offeringsObject.getBoolean(ST_JEROMES_ONLY_TAG));
            mCourseDetail.setRenison(offeringsObject.getBoolean(RENSION_TAG));
            mCourseDetail.setRenisonOnly(offeringsObject.getBoolean(RENISON_ONLY_TAG));
            mCourseDetail.setConradGrebel(offeringsObject.getBoolean(CONRAD_GREBEL_TAG));
            mCourseDetail.setConradGrebelOnly(offeringsObject.getBoolean(CONRAD_GREBEL_ONLY_TAG));
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseCourseScheduleJSON() {
        try {
            JSONArray courseScheduleArray = mApiResult.getResultJSON().getJSONArray(DATA_TAG);
            for(int i = 0; i < courseScheduleArray.length(); i++){
                JSONObject courseScheduleObject = courseScheduleArray.getJSONObject(i);
                CourseSchedule courseSchedule = new CourseSchedule();

                parseSingleCourse(courseSchedule, courseScheduleObject);
                courseSchedule.setNote(courseScheduleObject.getString(NOTE_TAG));
                courseSchedule.setSection(courseScheduleObject.getString(SECTION_TAG));
                courseSchedule.setCampus(courseScheduleObject.getString(CAMPUS_TAG));
                courseSchedule.setAssociatedClass(courseScheduleObject.getInt(ASSOCIATED_CLASS_TAG));
                courseSchedule.setRelatedComponent1(courseScheduleObject.getString(RELATED_COMPONENT_1_TAG));
                courseSchedule.setRelatedComponent2(courseScheduleObject.getString(RELATED_COMPONENT_2_TAG));
                courseSchedule.setEnrollmentCapacity(courseScheduleObject.getInt(ENROLLMENT_CAPACITY_TAG));
                courseSchedule.setEnrollmentTotal(courseScheduleObject.getInt(ENROLLMENT_TOTAL_TAG));
                courseSchedule.setWaitingCapacity(courseScheduleObject.getInt(WAITING_CAPACITY_TAG));
                courseSchedule.setWaitingTotal(courseScheduleObject.getInt(WAITING_TOTAL_TAG));
                courseSchedule.setTopic(courseScheduleObject.getString(TOPIC_TAG));

                Reserves reserves = new Reserves();
                JSONObject reserveObject = courseScheduleObject.getJSONObject(RESERVES_TAG);
                reserves.setReserveGroup(reserveObject.getString(RESERVE_GROUP_TAG));
                reserves.setEnrollmentCapacity(reserveObject.getInt(ENROLLMENT_CAPACITY_TAG));
                reserves.setEnrollmentTotal(reserveObject.getInt(ENROLLMENT_TOTAL_TAG));
                courseSchedule.setReserves(reserves);

           /*     Classes classes = new Classes();
                JSONObject classesObject = courseScheduleObject.getJSONObject(CLASSES_TAG);
                classes.setStartTime(classesObject.getString(START_DATE_TAG));
                classes.setEndTime(classesObject.getString(END_TIME_TAG));
                classes.setWeekdays(classesObject.getString(WEEKDAYS_TAG));
                classes.setStartDate(classesObject.getString(START_DATE_TAG));
                classes.setEndDate(classesObject.getString(END_DATE_TAG));
                classes.setIsTBA(classesObject.getBoolean(IS_TBA_TAG));
                classes.setIsCancelled(classesObject.getBoolean(IS_CANCELLED_TAG));
                classes.setIsClosed(classesObject.getBoolean(IS_CLOSED_TAG));
                classes.setBuilding(classesObject.getString(BUILDING_TAG));
                classes.setRoom(classesObject.getString(ROOM_TAG));
                classes.setInstructors(classesObject.getString(INSTRUCTORS_TAG));
                courseSchedule.setClasses(classes);*/

                courseSchedule.setHeldWith(courseScheduleObject.getString(HELD_WITH_TAG));
                courseSchedule.setTerm(courseScheduleObject.getInt(TERM_TAG));
                courseSchedule.setLastUpdated(courseScheduleObject.getString(LAST_UPDATED_TAG));
                mCourseSchedule.add(courseSchedule);
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void parseExamScheduleJSON(){
        try {
            JSONObject examScheduleObject = mApiResult.getResultJSON().getJSONObject(DATA_TAG);
            mExamSchedule = new ExamSchedule();

          //  parseSingleCourse(mCourseSchedule, examScheduleObject);
            mExamSchedule.setCourse(examScheduleObject.getString(COURSE_TAG));

            JSONArray sectionsArray = examScheduleObject.getJSONArray(SECTIONS_TAG);
            ArrayList<Section> sections = new ArrayList<>();
            for(int i = 0; i < sectionsArray.length(); i++){
                JSONObject sectionObject = sectionsArray.getJSONObject(i);
                Section section = new Section();
                section.setSection(sectionObject.getString(SECTION_TAG));
                section.setDay(sectionObject.getString(DAY_TAG));
                section.setDate(sectionObject.getString(DATE_TAG));
                section.setStartTime(sectionObject.getString(START_TIME_TAG));
                section.setEndTime(sectionObject.getString(END_TIME_TAG));
                section.setLocation(sectionObject.getString(LOCATION_TAG));
                section.setNotes(sectionObject.getString(NOTES_TAG));
                sections.add(section);
            }
            mExamSchedule.setSections(sections);

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseSingleCourse(Course course, JSONObject courseObject ){
        try {
            course.setCourseId(courseObject.getString(COURSE_ID_TAG));
            course.setSubject(courseObject.getString(SUBJECT_TAG));
            course.setCatalogNumber(courseObject.getString(CATALOG_NUMBER_TAG));
            course.setTitle(courseObject.getString(TITLE_TAG));
            course.setUnits(courseObject.getString(UNITS_TAG));
            course.setDescription(courseObject.getString(DESCRIPTION_TAG));
            course.setAcademicLevel(courseObject.getString(ACADEMIC_LEVEL_TAG));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void setAPIResult(APIResult mApiResult) {
        this.mApiResult = mApiResult;
    }

    @Override
    public APIResult getAPIResult() {
        return mApiResult;
    }

    @Override
    public void setParseType(int mParseType) {
        this.mParseType = ParseType.values()[mParseType];
    }

    @Override
    public MetaData getMeta() {
        MetaDataParser parser = new MetaDataParser();
        parser.setAPIResult(mApiResult);
        parser.parseJSON();
        return parser.getMeta();
    }

    @Override
    public String getEndPoint() {
        switch (mParseType){
            case COURSES:
                return  COURSES_END_POINT;
            case COURSE_DETAILS:
                return COURSE_DETAILS_END_POINT;
            case COURSE_SCHEDULE:
                return COURSE_SCHEDULE_END_POINT;
            case EXAM_SCHEDULE:
                return COURSE_EXAM_SCHEDULE_END_POINT;
            default:
                return "";
        }
    }

}
