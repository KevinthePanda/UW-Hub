package com.projects.kquicho.uwatm8;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.LinearGradient;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Course.Classes;
import com.projects.kquicho.uw_api_client.Course.CourseSchedule;
import com.projects.kquicho.uw_api_client.Course.UWClass;
import com.projects.kquicho.uw_api_client.Terms.TermsParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Kevin Quicho on 3/16/2016.
 */
public class CourseScheduleFragment extends Fragment implements JSONDownloader.onDownloadListener{
    public static final String TAG = "courseScheduleFragment";
    public static final String SUBJECT_TAG = "subject";
    public static final String CATALOG_NUMBER_TAG = "catalogNumber";
    private String mSubject;
    private String mCatalogNumber;
    private TermsParser mTermsParser = new TermsParser();
    private String mTermListUrl;
    private Map<String, ArrayList<CourseSchedule>> mSchedules;
    private String mCurrentScheduleUrl;
    private String mNextScheduleUrl;
    private String mCurrentTerm;
    private String mNextTerm;
    private LinearLayout mContainer;

    public static CourseScheduleFragment newInstance(String subject, String catalogNumber) {

        Bundle args = new Bundle();
        args.putString(SUBJECT_TAG, subject);
        args.putString(CATALOG_NUMBER_TAG, catalogNumber);
        CourseScheduleFragment fragment = new CourseScheduleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSubject = args.getString(SUBJECT_TAG);
        mCatalogNumber = args.getString(CATALOG_NUMBER_TAG);
        mSchedules = new TreeMap<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mTermsParser.setParseType(TermsParser.ParseType.TERM_LIST.ordinal());
        mTermListUrl = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint());
        mContainer = (LinearLayout)view.findViewById(R.id.container);

        JSONDownloader downloader = new JSONDownloader(mTermListUrl);
        downloader.setOnDownloadListener(this);
        downloader.start();

    }


    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    @Override
    public void onDownloadComplete(APIResult  apiResult) {
        mTermsParser.setAPIResult(apiResult);
        mTermsParser.parseJSON();
        if(apiResult.getUrl().equals(mTermListUrl)){
            mTermsParser.getCurrentTerm();
            mCurrentTerm = String.valueOf(mTermsParser.getCurrentTerm());
            mNextTerm = String.valueOf(mTermsParser.getNextTerm());
            mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());

            mCurrentScheduleUrl = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(mCurrentTerm, mSubject, mCatalogNumber));
            mNextScheduleUrl = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(mNextTerm, mSubject, mCatalogNumber));
            JSONDownloader downloader = new JSONDownloader(mCurrentScheduleUrl, mNextScheduleUrl);
            downloader.setOnDownloadListener(this);
            downloader.start();
        }else {
            final ArrayList<CourseSchedule> courseSchedules = mTermsParser.getCatalogNumberClasses();
            final String term;
            android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

            if(apiResult.getUrl().equals(mCurrentScheduleUrl)) {
                term = mCurrentTerm;
            }else{
                term = mNextTerm;
            }
            if(courseSchedules.size() > 0) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        createTerm(term);

                       /* for(CourseSchedule courseSchedule : courseSchedules) {
                            createClass(courseSchedule);
                        }*/
                    }
                };

                mSchedules.put(term, courseSchedules);
                handler.post(runnable);
            }
            Log.i(TAG, "completed " + term);
        }
    }

    private void createClass(CourseSchedule courseSchedule){
        String section = courseSchedule.getSection();
        ArrayList<Classes> classes = courseSchedule.getClasses();

        String capacity = courseSchedule.getEnrollmentTotal() + "/" + courseSchedule.getEnrollmentCapacity();
        Context context = getContext();
        Resources res = context.getResources();

        LinearLayout horizontalLinearLayout = new LinearLayout(context);
        horizontalLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLinearLayout.setWeightSum(1);

        LinearLayout column1Layout = new LinearLayout(context);
        column1Layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        column1Layout.setOrientation(LinearLayout.VERTICAL);
        column1Layout.setPadding((int) res.getDimension(R.dimen.term_side_padding), 0, 0, 0);


        LinearLayout.LayoutParams tvLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView sectionTV = new TextView(context);
        sectionTV.setText(section);
        sectionTV.setLayoutParams(tvLP);
        sectionTV.setTextSize(15);

        TextView instructorTV = new TextView(context);
       // instructorTV.setText(instructor);
        instructorTV.setLayoutParams(tvLP);
        instructorTV.setTextSize(15);

        TextView capacityTV = new TextView(context);
        capacityTV.setText(capacity);
        capacityTV.setLayoutParams(tvLP);
        capacityTV.setTextSize(15);

        column1Layout.addView(sectionTV);
        column1Layout.addView(instructorTV);
        column1Layout.addView(capacityTV);

        horizontalLinearLayout.addView(column1Layout);

        for(Classes singgleClass : classes){
            ArrayList<String> instructors = singgleClass.getInstructors();
            String instructor = "---";
            if(instructors.size() > 0){
                instructor = instructors.get(0);
            }

            String location = singgleClass.getBuilding() + " " + singgleClass.getRoom();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            DateFormat newFormat = new SimpleDateFormat("hh:mm a");
            Date startTime = null;
            Date endTime = null;
            try{
                startTime = dateFormat.parse(singgleClass.getStartTime());
                endTime = dateFormat.parse(singgleClass.getEndTime());
            }catch (ParseException ex){
                Log.e(TAG, "createClass ParseException: " + ex.getMessage());
            }
            if(startTime == null || endTime == null){
                return;
            }

            String time = newFormat.format(startTime) + " - " + newFormat.format(endTime);
            String weekdays = singgleClass.getWeekdays();
            String daysCopy = "MTWThF";
            String days = "";
            for(int i = 0; i < daysCopy.length(); i++){
                String letter = daysCopy.charAt(i) + "";
                if(i +1 < daysCopy.length() && daysCopy.charAt(i +1) == 'h'){
                    letter += 'h';
                    i++;
                }
                if(weekdays.contains(letter)){
                    days += "<font color=#000000>" + letter + "</font>";
                }else{
                    days += "<font color=#d3d3d3>" + letter + "</font>";
                }
            }


            LinearLayout column2Layout = new LinearLayout(context);
            column2Layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            column2Layout.setOrientation(LinearLayout.VERTICAL);
            column2Layout.setPadding((int)res.getDimension(R.dimen.term_side_padding), 0, 0, 0);


            TextView timeTV = new TextView(context);
            timeTV.setText(time);
            timeTV.setLayoutParams(tvLP);
            timeTV.setTextSize(15);

            TextView daysTV = new TextView(context);
            daysTV.setText(Html.fromHtml(days));
            daysTV.setLayoutParams(tvLP);
            daysTV.setTextSize(15);

            TextView locationTV = new TextView(context);
            locationTV.setText(location);
            locationTV.setLayoutParams(tvLP);
            locationTV.setTextSize(15);

            column2Layout.addView(timeTV);
            column2Layout.addView(daysTV);
            column2Layout.addView(locationTV);

            horizontalLinearLayout.addView(column2Layout);

            mContainer.addView(horizontalLinearLayout);
        }
    }

    private void createTerm(String term){
        String year = "20" + term.substring(1,3);
        String season = term.substring(3);
        switch (season){
            case "1":
                season = "Winter";
                break;
            case "5":
                season = "Spring";
                break;
            case "9":
                season = "Fall";
                break;
        }

        Context context = getContext();
        Resources res = context.getResources();
        TextView termHeader = new TextView(getContext());
        termHeader.setText(season + " " + year);
        termHeader.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        termHeader.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray));
        termHeader.setTextSize(14);
        termHeader.setPadding((int)res.getDimension(R.dimen.term_side_padding), (int)res.getDimension(R.dimen.term_top_bottom_padding),
                (int)res.getDimension(R.dimen.term_side_padding), (int)res.getDimension(R.dimen.term_top_bottom_padding));
            mContainer.addView(termHeader);
    }
}
