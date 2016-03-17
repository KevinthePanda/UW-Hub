package com.projects.kquicho.uw_api_client.Terms;
import android.util.Log;

import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.MetaData;
import com.projects.kquicho.uw_api_client.Core.MetaDataParser;
import com.projects.kquicho.uw_api_client.Core.UWParser;
import com.projects.kquicho.uw_api_client.Course.Classes;
import com.projects.kquicho.uw_api_client.Course.Course;
import com.projects.kquicho.uw_api_client.Course.CourseSchedule;
import com.projects.kquicho.uw_api_client.Course.Reserve;
import com.projects.kquicho.uw_api_client.Course.Reserves;
import com.projects.kquicho.uw_api_client.Course.ScheduleData;
import com.projects.kquicho.uw_api_client.Course.UWClass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class TermsParser extends UWParser {
    public static final String NEW_SUBJECT_LETTER = "new_subject_letter:";

    // end point strings
    private static final String TERM_END_POINT = "terms/list"; // //terms/list
    private static final String EXAM_END_POINT = "terms/%s/examschedule"; // /terms/{term}/examschedule
    private static final String SUBJECT_END_POINT = "terms/%s/%s/schedule"; // /terms/{term}/{subject}/schedule
    private static final String CATALOG_END_POINT = "terms/%s/%s/%s/schedule"; // /terms/{term}/{subject}/{catalog_number}/schedule
    private static final String TERM_COURSES_END_POINT = "terms/%s/courses"; // /terms/{term}/courses

    // JSON Object leaf node tags
    private static final String COURSE_ID_TAG = "course_id";
    private static final String COURSE_TAG = "course";
    private static final String SECTION_TAG = "section";
    private static final String DAY_TAG = "day";
    private static final String DATE_TAG = "date";
    private static final String LOCATION_TAG = "location";
    private static final String NOTES_TAG = "notes";
    private static final String DESCRIPTION_TAG = "description";

    private static final String SUBJECT_TAG = "subject";
    private static final String CATALOG_NUMBER_TAG = "catalog_number";
    private static final String UNITS_TAG = "units";
    private static final String TITLE_TAG = "title";
    private static final String NOTE_TAG = "note";
    private static final String CLASS_NUMBER_TAG = "class_number";
    private static final String CAMPUS_TAG = "campus";
    private static final String ASSOCIATED_CLASS_TAG = "associated_class";
    private static final String RELATED_COMPONENT_1_TAG = "related_component_1";
    private static final String RELATED_COMPONENT_2_TAG = "related_component_2";
    private static final String ENROLLMENT_CAPACITY_TAG = "enrollment_capacity";
    private static final String ENROLLMENT_TOTAL_TAG = "enrollment_total";
    private static final String WAITING_CAPACITY_TAG = "waiting_capacity";
    private static final String WAITING_TOTAL_TAG = "waiting_total";
    private static final String TOPIC_TAG = "topic";
    private static final String RESERVES_TAG = "reserves";
    private static final String TERM_TAG = "term";
    private static final String ACADEMIC_LEVEL_TAG = "academic_level";
    private static final String LAST_UPDATED_TAG = "last_updated";
    private static final String START_TIME_TAG = "start_time";
    private static final String END_TIME_TAG = "end_time";
    private static final String WEEKDAYS_TAG = "weekdays";
    private static final String START_DATE_TAG = "start_date";
    private static final String END_DATE_TAG = "end_date";
    private static final String BUILDING_TAG = "building";
    private static final String ROOM_TAG = "room";
    private static final String RESERVE_GROUP_TAG = "reserve_group";
    private static final String IS_TBA_TAG = "is_tba";
    private static final String IS_CANCELLED_TAG = "is_cancelled";
    private static final String IS_CLOSED_TAG = "is_closed";
    private static final String CURRENT_TERM_TAG = "current_term";
    private static final String NEXT_TERM_TAG = "next_term";

    // JSON Array/Object Tags
    private static final String DATA_TAG = "data";
    private static final String SECTIONS_TAG = "sections";
    private static final String CLASSES_TAG = "classes";
    private static final String DATES_TAG = "date";
    private static final String INSTRUCTORS_TAG = "instructors";
    private static final String HELD_WITH_TAG = "held_with";


    // contains all JSON information
    APIResult apiResult = null;

    public enum ParseType {
        TERM_LIST,
        EXAM_SCHEDULE,
        SUBJECT_SCHEDULE,
        CATALOG_SCHEDULE,
        TERM_COURSES
    }

    // /terms/list
    private String currentTerm = null;
    private String nextTerm = null;

    // /terms/{term}/examschedule variables
    private ArrayList<CourseExamSchedule> examSchedules = new ArrayList<>();

    // /terms/{term}/{subject}/schedule variables
    private ArrayList<UWClass> subjectClasses = new ArrayList<>();

    // /terms/{term}/{subject}/{catalog_number}/schedule variables
    private ArrayList<CourseSchedule> catalogNumberClasses = null;

    // /terms/{term}/courses /term course variables
    private ArrayList<TermCourse> mTermCourses = new ArrayList<>();

    //term course subjects
    private ArrayList<String> mTermSubjects = new ArrayList<>();


    private ParseType parseType = ParseType.EXAM_SCHEDULE;

    public ParseType getParseType() {
        return parseType;
    }

    public void setParseType(ParseType parseType) {
        this.parseType = parseType;
    }

    @Override
    public void parseJSON() {
        if(apiResult == null || apiResult.getResultJSON() == null) return;
        switch (parseType){
            case TERM_LIST:
                parseTermListJSON();
                break;
            case EXAM_SCHEDULE:
                parseExamScheduleJSON();
                break;
            case SUBJECT_SCHEDULE:
                parseSubjectScheduleJSON();
                break;
            case CATALOG_SCHEDULE:
                parseCatalogScheduleJSON();
                break;
            case TERM_COURSES:
                parseTermCoursesJSON();
                break;
        }
    }

    private void parseTermListJSON(){
        try {
            JSONObject termListObject = apiResult.getResultJSON().getJSONObject(DATA_TAG);
            currentTerm = termListObject.getString(CURRENT_TERM_TAG);
            nextTerm = termListObject.getString(NEXT_TERM_TAG);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseExamScheduleJSON(){
        try {
            JSONArray scheduleArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int scheduleArrayLength = scheduleArray.length();

            for(int i = 0; i < scheduleArrayLength; i++){
                JSONObject scheduleObject = scheduleArray.getJSONObject(i);
                CourseExamSchedule schedule = new CourseExamSchedule();

                if(!scheduleObject.isNull(COURSE_TAG))
                    schedule.setCourse(scheduleObject.getString(COURSE_TAG));

                JSONArray sectionArray = scheduleObject.getJSONArray(SECTIONS_TAG);
                int sectionArrayLength = sectionArray.length();
                ArrayList<Section> sections = new ArrayList<>();

                for(int j = 0; j < sectionArrayLength; j++){
                    JSONObject sectionObject = sectionArray.getJSONObject(j);
                    Section section = new Section();

                    if(!sectionObject.isNull(SECTION_TAG))
                        section.setSection(sectionObject.getString(SECTION_TAG));

                    if(!sectionObject.isNull(DAY_TAG))
                        section.setDay(sectionObject.getString(DAY_TAG));

                    if(!sectionObject.isNull(DATE_TAG))
                        section.setDate(sectionObject.getString(DATE_TAG));

                    if(!sectionObject.isNull(START_TIME_TAG))
                        section.setStartTime(sectionObject.getString(START_TIME_TAG));

                    if(!sectionObject.isNull(END_TIME_TAG))
                        section.setEndTime(sectionObject.getString(END_TIME_TAG));

                    if(!sectionObject.isNull(LOCATION_TAG))
                        section.setLocation(sectionObject.getString(LOCATION_TAG));

                    if(!sectionObject.isNull(NOTES_TAG))
                        section.setNotes(sectionObject.getString(NOTES_TAG));

                    sections.add(section);
                }

                schedule.setSections(sections);
                examSchedules.add(schedule);
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseSubjectScheduleJSON(){
        try {
            JSONArray classArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int classArrayLength = classArray.length();

            for (int i = 0; i < classArrayLength; i++) {
                JSONObject classObject = classArray.getJSONObject(i);
                UWClass uwClass = parseSingleClass(classObject);
                subjectClasses.add(uwClass);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseCatalogScheduleJSON(){
        try {
            catalogNumberClasses = new ArrayList<>();
            JSONArray courseScheduleArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            for(int i = 0; i < courseScheduleArray.length(); i++){
                JSONObject courseScheduleObject = courseScheduleArray.getJSONObject(i);
                CourseSchedule courseSchedule = new CourseSchedule();

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

              /*  Reserves reserves = new Reserves();
                JSONObject reserveObject = courseScheduleObject.getJSONObject(RESERVES_TAG);
                reserves.setReserveGroup(reserveObject.getString(RESERVE_GROUP_TAG));
                reserves.setEnrollmentCapacity(reserveObject.getInt(ENROLLMENT_CAPACITY_TAG));
                reserves.setEnrollmentTotal(reserveObject.getInt(ENROLLMENT_TOTAL_TAG));
                courseSchedule.setReserves(reserves);*/

                ArrayList<Classes> classes = new ArrayList<>();
                JSONArray classesArray = courseScheduleObject.getJSONArray(CLASSES_TAG);
                for(int j = 0; j < classesArray.length(); j ++){
                    JSONObject classesObject = classesArray.getJSONObject(j);
                    JSONObject dateObject = classesObject.getJSONObject(DATE_TAG);
                    Classes singleClass = new Classes();
                    singleClass.setStartTime(dateObject.getString(START_TIME_TAG));
                    singleClass.setEndTime(dateObject.getString(END_TIME_TAG));
                    singleClass.setWeekdays(dateObject.getString(WEEKDAYS_TAG));
                    singleClass.setStartDate(dateObject.getString(START_DATE_TAG));
                    singleClass.setEndDate(dateObject.getString(END_DATE_TAG));
                    singleClass.setIsTBA(dateObject.getBoolean(IS_TBA_TAG));
                    singleClass.setIsCancelled(dateObject.getBoolean(IS_CANCELLED_TAG));
                    singleClass.setIsClosed(dateObject.getBoolean(IS_CLOSED_TAG));

                    JSONObject buildingObject = classesObject.getJSONObject(LOCATION_TAG);
                    singleClass.setBuilding(buildingObject.getString(BUILDING_TAG));
                    singleClass.setRoom(buildingObject.getString(ROOM_TAG));

                    singleClass.setInstructors(classesObject.getString(INSTRUCTORS_TAG));
                    classes.add(singleClass);
                }
                courseSchedule.setClasses(classes);

                courseSchedule.setHeldWith(courseScheduleObject.getString(HELD_WITH_TAG));
                courseSchedule.setTerm(courseScheduleObject.getInt(TERM_TAG));
                courseSchedule.setLastUpdated(courseScheduleObject.getString(LAST_UPDATED_TAG));
                Log.i("test", courseSchedule.getSection() + ", " + courseSchedule.getTerm());
                catalogNumberClasses.add(courseSchedule);
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseTermCoursesJSON(){
        try {
            JSONArray termCourseArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int termCourseArrayLength = termCourseArray.length();

            String prevSubject = "-1";
            for (int i = 0; i < termCourseArrayLength; i++) {
                JSONObject termCourseObject = termCourseArray.getJSONObject(i);
                TermCourse termCourse = new TermCourse();

                if(!termCourseObject.isNull(UNITS_TAG)){
                    termCourse.setUnits((float)termCourseObject.getDouble(UNITS_TAG));
                }

                if(!termCourseObject.isNull(CATALOG_NUMBER_TAG)){
                    termCourse.setCatalogNumber(termCourseObject.getString(CATALOG_NUMBER_TAG));
                }

                String currentSubject = "";
                if(!termCourseObject.isNull(SUBJECT_TAG)){
                    currentSubject = termCourseObject.getString(SUBJECT_TAG);
                    termCourse.setSubject(currentSubject);
                }

                if(!termCourseObject.isNull(TITLE_TAG)){
                    termCourse.setTitle(termCourseObject.getString(TITLE_TAG));
                }

                mTermCourses.add(termCourse);

                if(!prevSubject.equals(currentSubject)){
                    char firstLetter = currentSubject.charAt(0);
                    if(prevSubject.charAt(0) != firstLetter){
                        mTermSubjects.add(NEW_SUBJECT_LETTER + firstLetter);
                    }
                    mTermSubjects.add(currentSubject);
                    prevSubject = currentSubject;
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }


    public String getCurrentTerm(){
        return currentTerm;
    }

    public String getNextTerm(){
        return nextTerm;
    }

    public ArrayList<CourseExamSchedule> getExamSchedules() {
        return examSchedules;
    }

    public ArrayList<UWClass> getSubjectClasses() {
        return subjectClasses;
    }

    public ArrayList<CourseSchedule> getCatalogNumberClasses() {
        return catalogNumberClasses;
    }

    public ArrayList<TermCourse> getTermCourses(){
        return mTermCourses;
    }

    public ArrayList<String> getTermSubjects(){
        return mTermSubjects;
    }

    private UWClass parseSingleClass(JSONObject classObject) throws JSONException {
        UWClass uwClass = new UWClass();

        if (!classObject.isNull(SUBJECT_TAG))
            uwClass.setSubject(classObject.getString(SUBJECT_TAG));

        if (!classObject.isNull(CATALOG_NUMBER_TAG))
            uwClass.setCatalog_number(classObject.getString(CATALOG_NUMBER_TAG));

        if(!classObject.isNull(UNITS_TAG))
            uwClass.setUnits(classObject.getDouble(UNITS_TAG));

        if (!classObject.isNull(TITLE_TAG))
            uwClass.setTitle(classObject.getString(TITLE_TAG));

        if(!classObject.isNull(NOTE_TAG))
            uwClass.setNotes(classObject.getString(NOTE_TAG));

        if (!classObject.isNull(CLASS_NUMBER_TAG))
            uwClass.setClass_number(classObject.getInt(CLASS_NUMBER_TAG));

        if (!classObject.isNull(SECTION_TAG))
            uwClass.setSection(classObject.getString(SECTION_TAG));

        if(!classObject.isNull(CAMPUS_TAG))
            uwClass.setCampus(classObject.getString(CAMPUS_TAG));

        if(!classObject.isNull(ASSOCIATED_CLASS_TAG))
            uwClass.setAssociatedClass(classObject.getInt(ASSOCIATED_CLASS_TAG));

        if(!classObject.isNull(RELATED_COMPONENT_1_TAG))
            uwClass.setRelatedComponent1(classObject.getString(RELATED_COMPONENT_1_TAG));

        if(!classObject.isNull(RELATED_COMPONENT_2_TAG))
            uwClass.setRelatedComponent2(classObject.getString(RELATED_COMPONENT_2_TAG));

        if(!classObject.isNull(ENROLLMENT_CAPACITY_TAG))
            uwClass.setEnroll_cap(classObject.getInt(ENROLLMENT_CAPACITY_TAG));

        if (!classObject.isNull(ENROLLMENT_TOTAL_TAG))
            uwClass.setEnroll_total(classObject.getInt(ENROLLMENT_TOTAL_TAG));

        if(!classObject.isNull(WAITING_CAPACITY_TAG))
            uwClass.setWaiting_cap(classObject.getInt(WAITING_CAPACITY_TAG));

        if(!classObject.isNull(WAITING_TOTAL_TAG))
            uwClass.setWaiting_total(classObject.getInt(WAITING_TOTAL_TAG));

        if(!classObject.isNull(TOPIC_TAG))
            uwClass.setTopic(classObject.getString(TOPIC_TAG));

        JSONArray reserveArray = classObject.getJSONArray(RESERVES_TAG);
        int reserveArrayLength = reserveArray.length();
        ArrayList<Reserve> reserves = new ArrayList<>();

        for(int j = 0; j < reserveArrayLength; j++){
            Reserve reserve = new Reserve();
            JSONObject reserveObject = reserveArray.getJSONObject(j);

            if(!reserveObject.isNull(RESERVE_GROUP_TAG))
                reserve.setReserveGroup(reserveObject.getString(RESERVE_GROUP_TAG));

            if(!reserveObject.isNull(ENROLLMENT_CAPACITY_TAG))
                reserve.setEnrollmentCapacity(reserveObject.getInt(ENROLLMENT_CAPACITY_TAG));

            if(!reserveObject.isNull(ENROLLMENT_TOTAL_TAG))
                reserve.setEnrollmentTotal(reserveObject.getInt(ENROLLMENT_TOTAL_TAG));

            reserves.add(reserve);
        }

        uwClass.setReserves(reserves);

        ArrayList<ScheduleData> SDArray = new ArrayList<>();
        JSONArray classesArray = classObject.getJSONArray(CLASSES_TAG);

        for(int j = 0; j < classesArray.length(); j++) {
            ScheduleData SD = new ScheduleData();
            JSONObject innerClassObject = classesArray.getJSONObject(j);
            JSONObject datesObject = innerClassObject.getJSONObject(DATES_TAG);
            JSONObject locationObject = innerClassObject.getJSONObject(LOCATION_TAG);

            if (!datesObject.isNull(WEEKDAYS_TAG))
                SD.setWeekdays(datesObject.getString(WEEKDAYS_TAG));

            if (!datesObject.isNull(START_TIME_TAG))
                SD.setStart_time(datesObject.getString(START_TIME_TAG));

            if (!datesObject.isNull(END_TIME_TAG))
                SD.setEnd_time(datesObject.getString(END_TIME_TAG));

            if (!datesObject.isNull(START_DATE_TAG))
                SD.setStart_date(datesObject.getString(START_DATE_TAG));

            if (!datesObject.isNull(END_DATE_TAG))
                SD.setEnd_date(datesObject.getString(END_DATE_TAG));

            if(!datesObject.isNull(IS_TBA_TAG))
                SD.setIs_tba(datesObject.getBoolean(IS_TBA_TAG));

            if(!datesObject.isNull(IS_CANCELLED_TAG))
                SD.setIs_cancelled(datesObject.getBoolean(IS_CANCELLED_TAG));

            if(!datesObject.isNull(IS_CLOSED_TAG))
                SD.setIs_closed(datesObject.getBoolean(IS_CLOSED_TAG));

            // load instructor data
            JSONArray offeringInstructors = innerClassObject.getJSONArray(INSTRUCTORS_TAG);
            ArrayList<String> instructors = new ArrayList<>();
            int num_instructors = offeringInstructors.length();

            for (int k = 0; k < num_instructors; k++) {
                instructors.add(offeringInstructors.getString(k));
            }

            if (num_instructors > 0) {
                SD.setInstructors(instructors);
            }

            if (!locationObject.isNull(BUILDING_TAG))
                SD.setBuilding(locationObject.getString(BUILDING_TAG));

            if (!classObject.isNull(ROOM_TAG))
                SD.setRoom(classObject.getString(ROOM_TAG));

            SDArray.add(SD);
        }

        uwClass.setScheduleData(SDArray);

        if (!classObject.isNull(LAST_UPDATED_TAG))
            uwClass.setLast_updated(classObject.getString(LAST_UPDATED_TAG));

        if (!classObject.isNull(TERM_TAG))
            uwClass.setTerm(classObject.getInt(TERM_TAG));

        if(!classObject.isNull(ACADEMIC_LEVEL_TAG))
            uwClass.setAcademic_level(classObject.getString(ACADEMIC_LEVEL_TAG));

        return uwClass;
    }

    @Override
    public void setAPIResult(APIResult apiResult) {
        this.apiResult = apiResult;
    }

    @Override
    public APIResult getAPIResult() {
        return apiResult;
    }

    @Override
    public void setParseType(int parseType) {
        this.parseType = ParseType.values()[parseType];
    }

    @Override
    public MetaData getMeta() {
        MetaDataParser parser = new MetaDataParser();
        parser.setAPIResult(apiResult);
        parser.parseJSON();
        return parser.getMeta();
    }

    @Override
    public String getEndPoint() {
        switch (parseType){
            case TERM_LIST:
                return TERM_END_POINT;
            case EXAM_SCHEDULE:
                return EXAM_END_POINT;
            case SUBJECT_SCHEDULE:
                return SUBJECT_END_POINT;
            case CATALOG_SCHEDULE:
                return CATALOG_END_POINT;
            case TERM_COURSES:
                return TERM_COURSES_END_POINT;
            default:
                return "";
        }
    }

        public String getEndPoint(String term){
        switch (parseType){
            case EXAM_SCHEDULE:
                return String.format(EXAM_END_POINT, term);
            default:
                return String.format(EXAM_END_POINT, term);
        }
    }

    public String getEndPoint(String term, String subject){
        return String.format(SUBJECT_END_POINT, term, subject);
    }

    public String getEndPoint(String term, String subject, String catalogNumber){
        return String.format(CATALOG_END_POINT, term, subject, catalogNumber);
    }

    public String getTermCoursesEndPoint(String term){
        return String.format(TERM_COURSES_END_POINT, term);
    }
}
