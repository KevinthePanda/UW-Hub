package com.projects.kquicho.uw_api_client.Terms;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.util.Log;

import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.MetaData;
import com.projects.kquicho.uw_api_client.Core.MetaDataParser;
import com.projects.kquicho.uw_api_client.Core.UWParser;
import com.projects.kquicho.uw_api_client.Course.Classes;
import com.projects.kquicho.uw_api_client.Course.CourseSchedule;
import com.projects.kquicho.uw_api_client.Course.Reserve;
import com.projects.kquicho.uw_api_client.Course.ScheduleData;
import com.projects.kquicho.uw_api_client.Course.UWClass;
import com.projects.kquicho.uwatm8.AbstractExpandableData;
import com.projects.kquicho.uwatm8.CourseDBHelper;
import com.projects.kquicho.uwatm8.CourseEnrollmentData;
import com.projects.kquicho.uwatm8.CourseSectionClassData;
import com.projects.kquicho.uwatm8.CourseSectionData;
import com.projects.kquicho.uwatm8.CourseSectionFooterData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TermsParser extends UWParser {
    private static final String TAG = "TermsParser:";
    public static final String NEW_SUBJECT_LETTER = "new_subject_letter:";

    // end point strings
    private static final String TERM_END_POINT = "terms/list"; // //terms/list
    private static final String EXAM_END_POINT = "terms/%s/examschedule"; // /terms/{term}/examschedule
    private static final String SUBJECT_END_POINT = "terms/%s/%s/schedule"; // /terms/{term}/{subject}/schedule
    private static final String CATALOG_END_POINT = "terms/%s/%s/%s/schedule"; // /terms/{term}/{subject}/{catalog_number}/schedule
    private static final String TERM_COURSES_END_POINT = "terms/%s/courses"; // /terms/{term}/courses
    private static final String OPEN_ENROLLMENT_END_POINT = "terms/%s/schedule"; // /terms/{term}/{subject}/{catalog_number}/schedule

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

    // /terms/{term}/{subject}/{catalog_number}/schedule variables
    private ArrayList<Pair<AbstractExpandableData.GroupData, ArrayList<AbstractExpandableData.ChildData>>> courseSchedule = null;


    private String currentTerm;

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
                //parseCourseScheduleJSON();
                break;
            case TERM_COURSES:
                parseTermCoursesJSON();
                break;
        }
    }

    public void parseJSON(Context context) {
        if(apiResult == null || apiResult.getResultJSON() == null) return;
        switch (parseType){
            case CATALOG_SCHEDULE:
                parseCourseScheduleJSON(context);
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

    private String checkForLECorTUTEvent(ContentResolver cr, CourseDBHelper dbHelper, String eventID,
                                       String weekdays, String term){
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,
        };

        Calendar beginTime = Calendar.getInstance();
        Calendar finishTime = Calendar.getInstance();
        int firstOccurrence = 0;
        switch (weekdays.charAt(0)){
            case 'M':
                firstOccurrence = Calendar.MONDAY;
                break;
            case 'T':
                if(weekdays.length() > 1 && weekdays.charAt(1) != 'h'){
                    firstOccurrence = Calendar.TUESDAY;
                }else {
                    firstOccurrence = Calendar.THURSDAY;
                }
                break;
            case 'W':
                firstOccurrence = Calendar.WEDNESDAY;
                break;
            case 'F':
                firstOccurrence = Calendar.FRIDAY;
                break;
        }
        beginTime.set(Calendar.DAY_OF_WEEK, firstOccurrence);
        finishTime.set(Calendar.DAY_OF_WEEK, firstOccurrence);

        int year =  Integer.valueOf("20" + term.substring(1, 3));
        int month = Integer.valueOf(term.substring(3)) - 1;
        switch (month){
            case 0:
                beginTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                finishTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                break;
            case 4:
                beginTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
                finishTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);

                break;
            case 8:
                beginTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);
                finishTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                break;
        }
        beginTime.set(Calendar.YEAR, year);
        beginTime.set(Calendar.MONTH, month);
        beginTime.set(Calendar.HOUR_OF_DAY, 0);

        finishTime.set(Calendar.YEAR, year);
        finishTime.set(Calendar.MONTH, month);
        finishTime.set(Calendar.HOUR_OF_DAY, 23);


        long startMillis = beginTime.getTimeInMillis();
        long endMillis = finishTime.getTimeInMillis();

        Cursor cur = null;

        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {eventID};

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        if (cur.getCount() == 0) {
            dbHelper.deleteCourseEvent(eventID);
            eventID = null;
        }
        cur.close();
        return eventID;
    }

    private String checkForTSTEvent(ContentResolver cr, CourseDBHelper dbHelper, String eventID,
                                  String term, String startDate, String startTime){
        final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,
        };

        String year = "20" + term.substring(1,3);
        DateFormat dateFormat = new SimpleDateFormat("MM/dd yyyy HH:mm", Locale.CANADA);

        Date date = null;
        try {
            date = dateFormat.parse(startDate + " " + year + " " + startTime);
        }catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
            return null;
        }

        Cursor cur = null;

        String selection = CalendarContract.Instances.EVENT_ID + " = ?";
        String[] selectionArgs = new String[] {eventID};

        int twelveHours = 3600000 * 12;
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, date.getTime());
        ContentUris.appendId(builder, date.getTime() + twelveHours);

        cur =  cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        if (cur.getCount() == 0) {
            dbHelper.deleteCourseEvent(eventID);
            eventID = null;
        }
        cur.close();
        return eventID;
    }

    private void parseCourseScheduleJSON(Context context){
        try {
            courseSchedule = new ArrayList<>();
            JSONArray courseScheduleArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            CourseDBHelper dbHelper = CourseDBHelper.getInstance(context);

            for(int i = 0; i < courseScheduleArray.length(); i++){
                JSONObject courseSectionObject = courseScheduleArray.getJSONObject(i);

                String section = courseSectionObject.getString(SECTION_TAG);
                int classNumber = courseSectionObject.getInt(CLASS_NUMBER_TAG);
                String courseID = section + " " + courseSectionObject.getString(SUBJECT_TAG) +
                        courseSectionObject.getString(CATALOG_NUMBER_TAG)
                        + String.valueOf(courseSectionObject.getInt(TERM_TAG));

                String eventID = dbHelper.checkForCourseEvent(courseID);
                boolean isBeingWatched = dbHelper.checkForCourseWatch(courseID);

                CourseSectionData.Builder courseSectionBuilder = new CourseSectionData.Builder();
                String campus = courseSectionObject.getString(CAMPUS_TAG);
                int enrollmentCapacity = courseSectionObject.getInt(ENROLLMENT_CAPACITY_TAG);
                int enrollmentTotal = courseSectionObject.getInt(ENROLLMENT_TOTAL_TAG);
                courseSectionBuilder
                        .section(section)
                        .campus(campus)
                        .enrollmentCapacity(enrollmentCapacity)
                        .enrollmentTotal(enrollmentTotal);


                DateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.CANADA);
                DateFormat newFormat = new SimpleDateFormat("MMM d", Locale.CANADA);
                JSONArray classesArray = courseSectionObject.getJSONArray(CLASSES_TAG);
                ArrayList<AbstractExpandableData.ChildData> childDataArrayList = new ArrayList<>();
                for(int j = 0; j < classesArray.length(); j ++){
                    JSONObject singleClass = classesArray.getJSONObject(childDataArrayList.size());
                    JSONObject dateObject = singleClass.getJSONObject(DATE_TAG);
                    JSONObject locationObject = singleClass.getJSONObject(LOCATION_TAG);

                    CourseSectionClassData.Builder classBuilder = new CourseSectionClassData.Builder();

                    String startDate = dateObject.getString(START_DATE_TAG);
                    if(!startDate.equals("null")){
                        try {
                            Date date = dateFormat.parse(startDate);
                            startDate = " (" + newFormat.format(date) + ")";
                        }catch (ParseException ex){
                            ex.printStackTrace();
                        }
                    }else{
                        startDate = "";
                    }


                    if(j == 0){
                        String startTime = dateObject.getString(START_TIME_TAG);
                        String endTime = dateObject.getString(END_TIME_TAG);
                        String weekdays = dateObject.getString(WEEKDAYS_TAG);
                        String term = courseSectionObject.getString(TERM_TAG);

                        if(eventID != null){
                           if(!section.split(" ")[0].equals("TST")){
                               eventID = checkForLECorTUTEvent(context.getContentResolver(), dbHelper, eventID,
                                       weekdays, term);
                           }else{
                               eventID = checkForTSTEvent(context.getContentResolver(), dbHelper, eventID,
                                       term, dateObject.getString(START_DATE_TAG), startTime);
                           }
                        }

                        courseSectionBuilder
                                .startTime(startTime)
                                .endTime(endTime)
                                .weekdays(weekdays + startDate)
                                .date(dateObject.getString(START_DATE_TAG))
                                .eventID(eventID);

                        courseSectionBuilder.building(locationObject.getString(BUILDING_TAG));
                        courseSectionBuilder.room(locationObject.getString(ROOM_TAG));
                        String instructor = "N/A";
                        JSONArray instructorsArray = singleClass.getJSONArray(INSTRUCTORS_TAG);
                        if(instructorsArray.length() !=0) {
                            instructor = (String) instructorsArray.get(0);
                            String[] tempArray = instructor.split(",");
                            instructor = tempArray[1] + " " + tempArray[0];
                        }
                        courseSectionBuilder.instructor(instructor);
                    }else {
                        classBuilder
                                .startTime(dateObject.getString(START_TIME_TAG))
                                .endTime(dateObject.getString(END_TIME_TAG))
                                .weekdays(dateObject.getString(WEEKDAYS_TAG) + startDate)
                                .date(dateObject.getString(START_DATE_TAG))
                                .building(locationObject.getString(BUILDING_TAG))
                                .room(locationObject.getString(ROOM_TAG))
                                .campus(campus);

                        childDataArrayList.add(classBuilder.createCourseSectionClassData());
                    }
                }
                CourseSectionData courseSectionData = courseSectionBuilder.createCourseSectionData();



                //enrollment

                CourseEnrollmentData defaultEnrollmentData = new CourseEnrollmentData(enrollmentCapacity,
                        enrollmentTotal, "");
                childDataArrayList.add(defaultEnrollmentData);

                //reserves
                JSONArray reservesArray = courseSectionObject.getJSONArray(RESERVES_TAG);
                for(int j = 0; j < reservesArray.length(); j++){
                    JSONObject reservesObject = reservesArray.getJSONObject(j);
                    CourseEnrollmentData enrollmentData = new CourseEnrollmentData(
                            reservesObject.getInt(ENROLLMENT_CAPACITY_TAG), reservesObject.getInt(ENROLLMENT_TOTAL_TAG),
                            reservesObject.getString(RESERVE_GROUP_TAG));

                    childDataArrayList.add(enrollmentData);
                }

                childDataArrayList.add(new CourseSectionFooterData(classNumber, eventID, isBeingWatched));
                courseSchedule.add(new Pair<AbstractExpandableData.GroupData, ArrayList<AbstractExpandableData.ChildData>>(
                        courseSectionData, childDataArrayList));
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

    public ArrayList<Pair<AbstractExpandableData.GroupData, ArrayList<AbstractExpandableData.ChildData>>> getCourseSchedule(){
        return courseSchedule;
    }

    public boolean getIsEnrollmentOpen(String selectedSection){
        try {
            JSONArray courseScheduleArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            for(int i = 0; i < courseScheduleArray.length(); i++) {
                JSONObject courseSectionObject = courseScheduleArray.getJSONObject(i);

                String section = courseSectionObject.getString(SECTION_TAG);
                if (selectedSection.equals(section)) {
                    int enrollmentCapacity = courseSectionObject.getInt(ENROLLMENT_CAPACITY_TAG);
                    int enrollmentTotal = courseSectionObject.getInt(ENROLLMENT_TOTAL_TAG);
                    return enrollmentTotal < enrollmentCapacity;
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return false;
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
        currentTerm = term;
        return String.format(CATALOG_END_POINT, term, subject, catalogNumber);
    }

    public String getTermCoursesEndPoint(String term){
        return String.format(TERM_COURSES_END_POINT, term);
    }

    public String getCheckOpenEnrollmentEndPoint(String url){
        return String.format(OPEN_ENROLLMENT_END_POINT, url);
    }
}
