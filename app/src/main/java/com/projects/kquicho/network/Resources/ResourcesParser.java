package com.projects.kquicho.network.Resources;
import android.content.Context;
import android.util.Log;

import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.MetaData;
import com.projects.kquicho.network.Core.MetaDataParser;
import com.projects.kquicho.network.Core.UWParser;
import com.projects.kquicho.database.InfoSessionDBHelper;
import com.projects.kquicho.models.InfoSessionDBModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ZainH on 02/09/2015.
 *
 * A /resources/tutors call back's JSON Object looks like
 * {
 *     ...
 *     "data":[
 *          {
 *              "subject": (String)
 *              "catalog_number": (String)
 *              "title": (String)
 *              "tutors_count": (int)
 *              "contact_url": (String)
 *          },
 *          ...
 *     ]
 * }
 *
 * A /resources/printers call back's JSON Object looks like
 * {
 *     ...
 *     "data":[
 *          {
 *              "printer": (String)
 *              "ad": (String)
 *              "server": (String)
 *              "comment": (String)
 *              "driver": (String)
 *              "room": (String)
 *              "faculty": (String)
 *          },
 *          ...
 *     ]
 * }
 *
 * A /resources/infosession call back's JSON Object looks like
 * {
 *     ...
 *     "data":[
 *          {
 *              "id": (String)
 *              "employer": (String)
 *              "date": (String)
 *              "start_time": (String)
 *              "end_time": (String)
 *              "location": (String)
 *              "website": (String)
 *              "audience": (String)
 *              "programs": (String, comma separated values)
 *              "description": (String)
 *          },
 *          ...
 *     ]
 * }
 *
 *
 * A /resources/goosewatch call back's JSON Object looks like
 * {
 *     ...
 *     "data":[
 *          {
 *             "id": (int)
 *             "location": (String)
 *             "latitude": (double)
 *             "longitude": (double)
 *             "updated": (String)
 *          },
 *          ...
 *     ]
 * }
 *
 * Proper Use:
 * 1) call setParseType()
 * 2) call getEndPoint() and use it to build a URL with UWOpenDataAPI.buildURL(...)
 * 3) once an APIResult is received in onDownloadComplete(APIResult apiResult), call setAPIResult, then call parseJSON()
 * 4) the proper data is now parsed, and can be retrieved. (This depends on your parseType). Data can be requested through
 *      * getTutors()
 *      * getPrinters()
 *      * getInfoSessions()
 *      * getGooseLocations()
 */
public class ResourcesParser extends UWParser {
    public static final String TAG = UWParser.class.toString();
    // end points
    private static final String TUTORS_END_POINT = "resources/tutors";
    private static final String PRINTERS_END_POINT = "resources/printers";
    private static final String INFOSESSIONS_END_POINT = "resources/infosessions";
    private static final String GOOSEWATCH_END_POINT = "resources/goosewatch";

    // JSON Object leaf node tags
    private static final String SUBJECT_TAG = "subject";
    private static final String CATALOG_NUMBER_TAG = "catalog_number";
    private static final String TITLE_TAG = "title";
    private static final String TUTORS_COUNT_TAG = "tutors_count";
    private static final String CONTACT_URL_TAG = "contact_url";
    private static final String PRINTER_TAG = "printer";
    private static final String AD_TAG = "ad";
    private static final String SERVER_TAG = "server";
    private static final String COMMENT_TAG = "comment";
    private static final String DRIVER_TAG = "driver";
    private static final String ROOM_TAG = "room";
    private static final String FACULTY_TAG = "faculty";
    private static final String ID_TAG = "id";
    private static final String EMPLOYER_TAG = "employer";
    private static final String DATE_TAG = "date";
    private static final String START_TIME_TAG = "start_time";
    private static final String END_TIME_TAG = "end_time";
    private static final String BUILDING_TAG = "building";
    private static final String LOCATION_TAG = "location";
    private static final String WEBSITE_TAG = "website";
    private static final String AUDIENCE_TAG = "audience";
    private static final String PROGRAMS_TAG = "programs";
    private static final String DESCRIPTION_TAG = "description";
    private static final String LATITUDE_TAG = "latitude";
    private static final String LONGITUDE_TAG = "longitude";
    private static final String UPDATED_TAG = "updated";
    private static final String CODE_TAG = "code";
    private static final String MAP_URL_TAG = "map_url";
    private static final String LINK_TAG = "link";

    // JSON Array tags
    private static final String DATA_TAG = "data";

    // contains all JSON data
    private APIResult apiResult = null;

    // parse type
    public enum ParseType {
        TUTORS,
        PRINTERS,
        INFOSESSIONS,
        GOOSEWATCH
    }

    private ParseType parseType = ParseType.TUTORS;

    // /resources/tutors variables
    private ArrayList<Tutor> tutors = new ArrayList<>();

    // /resources/printers variables
    private ArrayList<CampusPrinter> printers = new ArrayList<>();

    // /resources/infosessions variables
    private ArrayList<InfoSession> infoSessions = new ArrayList<>();

    // /resources/goosewatch variables
    private ArrayList<GooseLocation> gooseLocations = new ArrayList<>();

    private ArrayList<InfoSession> homeWidgetInfoSessions;

    private ArrayList<InfoSessionDBModel> homeWidgetSavedInfoSessions;

    @Override
    public void parseJSON() {
        if(apiResult == null || apiResult.getResultJSON() == null) return;
        switch (parseType){
            case TUTORS:
                parseTutorsJSON();
                break;
            case PRINTERS:
                parsePrintersJSON();
                break;
            case INFOSESSIONS:
                parseInfoSessionsJSON();
                break;
            case GOOSEWATCH:
                parseGooseWatchJSON();
                break;
        }
    }

    private void parseTutorsJSON(){
        try {
            JSONArray tutorArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int tutorArrayLength = tutorArray.length();

            for(int i = 0; i < tutorArrayLength; i++){
                JSONObject tutorObject = tutorArray.getJSONObject(i);
                Tutor tutor = new Tutor();

                if(!tutorObject.isNull(SUBJECT_TAG))
                    tutor.setSubject(tutorObject.getString(SUBJECT_TAG));

                if(!tutorObject.isNull(CATALOG_NUMBER_TAG))
                    tutor.setCatalogNumber(tutorObject.getString(CATALOG_NUMBER_TAG));

                if(!tutorObject.isNull(TITLE_TAG))
                    tutor.setTitle(tutorObject.getString(TITLE_TAG));

                if(!tutorObject.isNull(TUTORS_COUNT_TAG))
                    tutor.setTutorsCount(tutorObject.getInt(TUTORS_COUNT_TAG));

                if(!tutorObject.isNull(CONTACT_URL_TAG))
                    tutor.setContactUrl(tutorObject.getString(CONTACT_URL_TAG));

                tutors.add(tutor);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parsePrintersJSON(){
        try {
            JSONArray printerArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int printerArrayLength = printerArray.length();

            for(int i = 0; i < printerArrayLength; i++){
                JSONObject printerObject = printerArray.getJSONObject(i);
                CampusPrinter printer = new CampusPrinter();

                if(!printerObject.isNull(PRINTER_TAG))
                    printer.setPrinter(printerObject.getString(PRINTER_TAG));

                if(!printerObject.isNull(AD_TAG))
                    printer.setAd(printerObject.getString(AD_TAG));

                if(!printerObject.isNull(SERVER_TAG))
                    printer.setServer(printerObject.getString(SERVER_TAG));

                if(!printerObject.isNull(COMMENT_TAG))
                    printer.setComment(printerObject.getString(COMMENT_TAG));

                if(!printerObject.isNull(DRIVER_TAG))
                    printer.setDriver(printerObject.getString(DRIVER_TAG));

                if(!printerObject.isNull(ROOM_TAG))
                    printer.setRoom(printerObject.getString(ROOM_TAG));

                if(!printerObject.isNull(FACULTY_TAG))
                    printer.setFaculty(printerObject.getString(FACULTY_TAG));

                printers.add(printer);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public ArrayList<InfoSession> getHomeWidgetInfoSessions(){
        return homeWidgetInfoSessions;
    }

    public ArrayList<InfoSessionDBModel> getHomeWidgetSavedInfoSessions(){
        return homeWidgetSavedInfoSessions;
    }

    public void parseHomeWidgetInfoSessions(Context context){
        try
        {
            InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(context);
            homeWidgetSavedInfoSessions = dbHelper.getHomeWidgetSavedInfoSessions();

            homeWidgetInfoSessions = new ArrayList<>();
            JSONArray infosessionArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int infosessionArrayLength = infosessionArray.length();

            for(int i = 0; i < infosessionArrayLength; i++){
                InfoSession location = new InfoSession();
                JSONObject jsonInfoSessionLocation = infosessionArray.getJSONObject(i);
                boolean shouldContinue = false;
                for(InfoSessionDBModel dbModel : homeWidgetSavedInfoSessions){
                    if(dbModel.getId() == jsonInfoSessionLocation.getInt(ID_TAG)){
                        shouldContinue = true;
                        break;
                    }
                }
                if(shouldContinue){
                    continue;
                }
                String rawDate = jsonInfoSessionLocation.getString(DATE_TAG);
                String rawStartTime = jsonInfoSessionLocation.getString(START_TIME_TAG);
                String rawEndTime = jsonInfoSessionLocation.getString(END_TIME_TAG);

                try {
                    //check to see if infosession has not past
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA);
                    Date date = format.parse(rawDate + " " + rawStartTime);
                    if(date == null || date.getTime() < System.currentTimeMillis()){
                        continue;
                    }

                    location.setTime(date.getTime());

                    //add date
                    format = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
                    date = format.parse(rawDate);
                    format = new SimpleDateFormat("MMM d, yy", Locale.CANADA);
                    location.setDate(format.format(date));

                    //displayTime
                    format = new SimpleDateFormat("HH:mm", Locale.CANADA);
                    Date startDateTime = format.parse(rawStartTime);
                    Date endDateTime = format.parse(rawEndTime);
                    Date afterNoonDateTime = format.parse("12:00");
                    DateFormat newFormat = new SimpleDateFormat("h:mm", Locale.CANADA);
                    DateFormat newFormatWithSuffix = new SimpleDateFormat("h:mm a", Locale.CANADA);

                    String displayTimeRange;
                    boolean isStartAM = startDateTime.getTime() < afterNoonDateTime.getTime();
                    boolean isEndAM = endDateTime.getTime() < afterNoonDateTime.getTime();
                    if((isStartAM && isEndAM) || (!isStartAM && ! isEndAM)){
                        displayTimeRange = newFormat.format(startDateTime) + " - "
                                + newFormatWithSuffix.format(endDateTime);
                    }else{
                        displayTimeRange = newFormatWithSuffix.format(startDateTime) + " - "
                                + newFormatWithSuffix.format(endDateTime);
                    }

                    location.setDisplay_time_range(displayTimeRange);

                }catch (ParseException ex){
                    Log.e(TAG, "onReceive ParseException: " + ex.getMessage());
                }


                location.setEmployer(jsonInfoSessionLocation.getString(EMPLOYER_TAG));
                JSONObject jsonBuildingObject = jsonInfoSessionLocation.getJSONObject(BUILDING_TAG);
                location.setBuildingCode(jsonBuildingObject.getString(CODE_TAG));
                location.setBuildingRoom(jsonBuildingObject.getString(ROOM_TAG));
                homeWidgetInfoSessions.add(location);
                if(homeWidgetInfoSessions.size() == 10){
                    Collections.sort(homeWidgetInfoSessions, new CustonComparator());
                    homeWidgetInfoSessions = new ArrayList<>(homeWidgetInfoSessions.subList(0,3));

                    return;
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
    public class CustonComparator implements Comparator<InfoSession>{
        @Override
        public int compare(InfoSession o1, InfoSession o2) {
            return  o1.getTime()<o2.getTime()?-1:
                    o1.getTime()>o2.getTime()?1:0;
        }
    }

    public InfoSession getSingleInfoSession(int id){
        try
        {
            JSONArray infosessionArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int infosessionArrayLength = infosessionArray.length();

            for(int i = 0; i < infosessionArrayLength; i++){
                JSONObject jsonInfoSessionLocation = infosessionArray.getJSONObject(i);
                InfoSession location = new InfoSession();

                if(!jsonInfoSessionLocation.isNull(ID_TAG)) {
                    int thisID = Integer.parseInt(jsonInfoSessionLocation.getString(ID_TAG));
                    if(id == thisID ) {
                        location.setId(id);
                    }else{
                        continue;
                    }
                }else{
                    continue;
                }

                if(!jsonInfoSessionLocation.isNull(EMPLOYER_TAG)) {
                    location.setIsCancelled(false);
                    String company = jsonInfoSessionLocation.getString(EMPLOYER_TAG);
                    if(company.contains("*CANCELLED - ")){
                        company = company.replace("*CANCELLED - ", "");
                        location.setIsCancelled(true);
                    }else if(company.contains("*CANCELLED ")){
                        company = company.replace("*CANCELLED ", "");
                        location.setIsCancelled(true);
                    }
                    location.setEmployer(company);
                }

                if(!jsonInfoSessionLocation.isNull(DATE_TAG)) {
                    String dateS = jsonInfoSessionLocation.getString(DATE_TAG);
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
                    try {
                        Date date = format.parse(dateS);
                        DateFormat newFormat = new SimpleDateFormat("MMM d, yy", Locale.CANADA);
                        dateS = newFormat.format(date);
                        location.setDate(dateS);
                    }catch (ParseException ex){
                        Log.e(TAG, "onReceive ParseException: " + ex.getMessage());
                    }

                }

                if(!jsonInfoSessionLocation.isNull(END_TIME_TAG) && !jsonInfoSessionLocation.isNull(START_TIME_TAG)) {
                    String startTimeS = jsonInfoSessionLocation.getString(START_TIME_TAG);
                    String endTimeS = jsonInfoSessionLocation.getString(END_TIME_TAG);
                    location.setStart_time(startTimeS);
                    location.setEnd_time(endTimeS);

                    DateFormat format = new SimpleDateFormat("HH:mm", Locale.CANADA);
                    try {
                        Date startDateTime = format.parse(startTimeS);
                        Date endDateTime = format.parse(endTimeS);
                        Date afterNoonDateTime = format.parse("12:00");
                        DateFormat newFormat = new SimpleDateFormat("h:mm", Locale.CANADA);
                        DateFormat newFormatWithSuffix = new SimpleDateFormat("h:mm a", Locale.CANADA);

                        String displayTimeRange;
                        boolean isStartAM = startDateTime.getTime() < afterNoonDateTime.getTime();
                        boolean isEndAM = endDateTime.getTime() < afterNoonDateTime.getTime();
                        if((isStartAM && isEndAM) || (!isStartAM && ! isEndAM)){
                            displayTimeRange = newFormat.format(startDateTime) + " - "
                                    + newFormatWithSuffix.format(endDateTime);
                        }else{
                            displayTimeRange = newFormatWithSuffix.format(startDateTime) + " - "
                                    + newFormatWithSuffix.format(endDateTime);
                        }

                        location.setDisplay_time_range(displayTimeRange);

                    }catch (ParseException ex){
                        Log.e(TAG, "onReceive ParseException: " + ex.getMessage());
                    }
                }


                if(!jsonInfoSessionLocation.isNull(BUILDING_TAG)){
                    JSONObject jsonBuildingObject = jsonInfoSessionLocation.getJSONObject(BUILDING_TAG);
                    if(!jsonBuildingObject.isNull(CODE_TAG)){
                        location.setBuildingCode(jsonBuildingObject.getString(CODE_TAG));
                    }
                    if(!jsonBuildingObject.isNull(ROOM_TAG)){
                        location.setBuildingRoom(jsonBuildingObject.getString(ROOM_TAG));
                    }
                    if(!jsonBuildingObject.isNull(MAP_URL_TAG)){
                        location.setBuildingMapUrl(jsonBuildingObject.getString(MAP_URL_TAG));
                    }
                    if(!jsonBuildingObject.isNull(LATITUDE_TAG)){
                        location.setLatitude(jsonBuildingObject.getDouble(LATITUDE_TAG));
                    }
                    if(!jsonBuildingObject.isNull(LONGITUDE_TAG)){
                        location.setLongitude(jsonBuildingObject.getDouble(LONGITUDE_TAG));
                    }
                }

                if (!jsonInfoSessionLocation.isNull(WEBSITE_TAG))
                    location.setWebsite(jsonInfoSessionLocation.getString(WEBSITE_TAG));

                if (!jsonInfoSessionLocation.isNull(AUDIENCE_TAG))
                    location.setAudience(jsonInfoSessionLocation.getString(AUDIENCE_TAG));

                if (!jsonInfoSessionLocation.isNull(PROGRAMS_TAG))
                    location.setPrograms(jsonInfoSessionLocation.getString(PROGRAMS_TAG));

                if(!jsonInfoSessionLocation.isNull(DESCRIPTION_TAG))
                    location.setDescription(jsonInfoSessionLocation.getString(DESCRIPTION_TAG));

                if(!jsonInfoSessionLocation.isNull(LINK_TAG))
                    location.setLink(jsonInfoSessionLocation.getString(LINK_TAG));

                return location;
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }
    private void parseInfoSessionsJSON(){
        try
        {
            JSONArray infosessionArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int infosessionArrayLength = infosessionArray.length();

            for(int i = 0; i < infosessionArrayLength; i++){
                JSONObject jsonInfoSessionLocation = infosessionArray.getJSONObject(i);
                InfoSession location = new InfoSession();

                if(!jsonInfoSessionLocation.isNull(ID_TAG))
                    location.setId(Integer.parseInt(jsonInfoSessionLocation.getString(ID_TAG)));

                if(!jsonInfoSessionLocation.isNull(EMPLOYER_TAG)) {
                    location.setIsCancelled(false);
                    String company = jsonInfoSessionLocation.getString(EMPLOYER_TAG);
                    if(company.contains("*CANCELLED - ")){
                        company = company.replace("*CANCELLED - ", "");
                        location.setIsCancelled(true);
                    }else if(company.contains("*CANCELLED ")){
                        company = company.replace("*CANCELLED ", "");
                        location.setIsCancelled(true);
                    }
                    location.setEmployer(company);
                }

                if(!jsonInfoSessionLocation.isNull(DATE_TAG)) {
                    String dateS = jsonInfoSessionLocation.getString(DATE_TAG);
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
                    try {
                        Date date = format.parse(dateS);
                        DateFormat newFormat = new SimpleDateFormat("MMM d, yy", Locale.CANADA);
                        dateS = newFormat.format(date);
                        location.setDate(dateS);
                    }catch (ParseException ex){
                        Log.e(TAG, "onReceive ParseException: " + ex.getMessage());
                    }

                }

                if(!jsonInfoSessionLocation.isNull(END_TIME_TAG) && !jsonInfoSessionLocation.isNull(START_TIME_TAG)) {
                    String startTimeS = jsonInfoSessionLocation.getString(START_TIME_TAG);
                    String endTimeS = jsonInfoSessionLocation.getString(END_TIME_TAG);
                    location.setStart_time(startTimeS);
                    location.setEnd_time(endTimeS);

                    DateFormat format = new SimpleDateFormat("HH:mm", Locale.CANADA);
                    try {
                        Date startDateTime = format.parse(startTimeS);
                        Date endDateTime = format.parse(endTimeS);
                        Date afterNoonDateTime = format.parse("12:00");
                        DateFormat newFormat = new SimpleDateFormat("h:mm", Locale.CANADA);
                        DateFormat newFormatWithSuffix = new SimpleDateFormat("h:mm a", Locale.CANADA);

                        String displayTimeRange;
                        boolean isStartAM = startDateTime.getTime() < afterNoonDateTime.getTime();
                        boolean isEndAM = endDateTime.getTime() < afterNoonDateTime.getTime();
                        if((isStartAM && isEndAM) || (!isStartAM && ! isEndAM)){
                            displayTimeRange = newFormat.format(startDateTime) + " - "
                                    + newFormatWithSuffix.format(endDateTime);
                        }else{
                            displayTimeRange = newFormatWithSuffix.format(startDateTime) + " - "
                                    + newFormatWithSuffix.format(endDateTime);
                        }

                        location.setDisplay_time_range(displayTimeRange);

                    }catch (ParseException ex){
                        Log.e(TAG, "onReceive ParseException: " + ex.getMessage());
                    }
                }


                if(!jsonInfoSessionLocation.isNull(BUILDING_TAG)){
                    JSONObject jsonBuildingObject = jsonInfoSessionLocation.getJSONObject(BUILDING_TAG);
                    if(!jsonBuildingObject.isNull(CODE_TAG)){
                        location.setBuildingCode(jsonBuildingObject.getString(CODE_TAG));
                    }
                    if(!jsonBuildingObject.isNull(ROOM_TAG)){
                        location.setBuildingRoom(jsonBuildingObject.getString(ROOM_TAG));
                    }
                    if(!jsonBuildingObject.isNull(MAP_URL_TAG)){
                        location.setBuildingMapUrl(jsonBuildingObject.getString(MAP_URL_TAG));
                    }
                    if(!jsonBuildingObject.isNull(LATITUDE_TAG)){
                        location.setLatitude(jsonBuildingObject.getDouble(LATITUDE_TAG));
                    }
                    if(!jsonBuildingObject.isNull(LONGITUDE_TAG)){
                        location.setLongitude(jsonBuildingObject.getDouble(LONGITUDE_TAG));
                    }
                }

                if (!jsonInfoSessionLocation.isNull(WEBSITE_TAG))
                    location.setWebsite(jsonInfoSessionLocation.getString(WEBSITE_TAG));

                if (!jsonInfoSessionLocation.isNull(AUDIENCE_TAG))
                    location.setAudience(jsonInfoSessionLocation.getString(AUDIENCE_TAG));

                if (!jsonInfoSessionLocation.isNull(PROGRAMS_TAG))
                    location.setPrograms(jsonInfoSessionLocation.getString(PROGRAMS_TAG));

                if(!jsonInfoSessionLocation.isNull(DESCRIPTION_TAG))
                    location.setDescription(jsonInfoSessionLocation.getString(DESCRIPTION_TAG));

                if(!jsonInfoSessionLocation.isNull(LINK_TAG))
                    location.setLink(jsonInfoSessionLocation.getString(LINK_TAG));

                infoSessions.add(location);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void parseGooseWatchJSON(){
        try
        {
            JSONArray gooseLocationArray = apiResult.getResultJSON().getJSONArray(DATA_TAG);
            int gooseLocationArrayLength = gooseLocationArray.length();

            for(int i = 0; i < gooseLocationArrayLength; i++){
                JSONObject gooseLocationObject = gooseLocationArray.getJSONObject(i);
                GooseLocation goose = new GooseLocation();

                if(!gooseLocationObject.isNull(ID_TAG))
                    goose.setId(gooseLocationObject.getInt(ID_TAG));

                if(!gooseLocationObject.isNull(LOCATION_TAG))
                    goose.setLocation(gooseLocationObject.getString(LOCATION_TAG));

                if(!gooseLocationObject.isNull(LATITUDE_TAG))
                    goose.setLatitude(gooseLocationObject.getDouble(LATITUDE_TAG));

                if(!gooseLocationObject.isNull(LONGITUDE_TAG))
                    goose.setLongitude(gooseLocationObject.getDouble(LONGITUDE_TAG));

                if(!gooseLocationObject.isNull(UPDATED_TAG))
                    goose.setTimeUpdated(gooseLocationObject.getString(UPDATED_TAG));

                gooseLocations.add(goose);
            }

        } catch(JSONException e){
            e.printStackTrace();
        }
    }

    public ArrayList<Tutor> getTutors() {
        return tutors;
    }

    public ArrayList<CampusPrinter> getPrinters() {
        return printers;
    }

    public ArrayList<InfoSession> getInfoSessions() {
        return infoSessions;
    }

    public ArrayList<GooseLocation> getGooseLocations() {
        return gooseLocations;
    }

    @Override
    public void setAPIResult(APIResult apiResult) {
        this.apiResult = apiResult;
    }

    @Override
    public APIResult getAPIResult() {
        return apiResult;
    }

    public ParseType getParseType() {
        return parseType;
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
    public String getEndPoint(){
        switch (parseType){
            case TUTORS:
                return TUTORS_END_POINT;
            case PRINTERS:
                return PRINTERS_END_POINT;
            case INFOSESSIONS:
                return INFOSESSIONS_END_POINT;
            case GOOSEWATCH:
                return GOOSEWATCH_END_POINT;
            default:
                return TUTORS_END_POINT;
        }
    }
}
