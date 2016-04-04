package com.projects.kquicho.uwatm8;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.projects.kquicho.uw_api_client.Building.BuildingParser;
import com.projects.kquicho.uw_api_client.Building.UWBuilding;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Terms.TermsParser;

import java.util.Calendar;

/**
 * Created by Kevin Quicho on 3/16/2016.
 */
public class CourseScheduleFragment extends Fragment implements JSONDownloader.onDownloadListener,
        RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener,
        CourseScheduleAdapter.onButtonClickListener,
        AsyncHandler.AsyncCRUDListener{

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";
    public static final String TAG = "courseScheduleFragment";
    public static final String SUBJECT_TAG = "subject";
    public static final String CATALOG_NUMBER_TAG = "catalogNumber";

    private CourseScheduleData mData;
    private CourseScheduleAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView.Adapter mWrappedAdapter;


    private String mCurrentTerm = "1165";
    private String mSubject;
    private String mCatalogNumber;
    private TermsParser mTermsParser = new TermsParser();
    private String mCourseScheduleURL;
    private BuildingParser mBuildingParser = new BuildingParser();
    private String mBuildingURL;
    private Long mLastEventID = null;
    private AsyncHandler mHandler;
    private CourseDBHelper mDBHelper;
    private View mEmptyView;
    private View mProgressBar;

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

        mHandler = new AsyncHandler(getActivity().getContentResolver(), this);
        mDBHelper = CourseDBHelper.getInstance(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_schedule, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.pbLoading);
        mEmptyView = view.findViewById(R.id.empty_view);
        mRecyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        mRecyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);
        mRecyclerViewExpandableItemManager.setOnGroupExpandListener(this);
        mRecyclerViewExpandableItemManager.setOnGroupCollapseListener(this);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.setHasFixedSize(false);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);


        mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
        mCourseScheduleURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint("1165",mSubject, mCatalogNumber));

        mProgressBar.setVisibility(View.VISIBLE);
        JSONDownloader downloader = new JSONDownloader(mCourseScheduleURL);
        downloader.setOnDownloadListener(this);
        downloader.start();

    }


    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    @Override
    public void onDownloadComplete(APIResult  apiResult) {
        if(apiResult.getUrl().equals(mCourseScheduleURL)) {
            mTermsParser.setAPIResult(apiResult);
            mTermsParser.parseJSON(getActivity().getApplicationContext());
            mData = new CourseScheduleData(mTermsParser.getCourseSchedule());

            android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

            final CourseScheduleAdapter.onButtonClickListener onButtonClickListener = this;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    if(mData == null || mData.getGroupCount() == 0){
                        mEmptyView.setVisibility(View.VISIBLE);
                    }else {
                        mEmptyView.setVisibility(View.GONE);
                        mAdapter = new CourseScheduleAdapter(mData, getContext(),
                                onButtonClickListener);
                        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);      // wrap for expanding
                        mRecyclerView.setAdapter(mWrappedAdapter);
                    }
                }
            };
            handler.post(runnable);

        }else{
            mBuildingParser.setAPIResult(apiResult);
            mBuildingParser.parseJSON();

            UWBuilding building = mBuildingParser.getBuildingCodeBuilding();

            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + String.valueOf(building.getLatitude()) + ","
                    + String.valueOf(building.getLongitude()) + "(" + building.getBuildingCode() + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
        Log.i(TAG, "completed ");

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save current state to support screen rotation, etc...
        if (mRecyclerViewExpandableItemManager != null) {
            outState.putParcelable(
                    SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                    mRecyclerViewExpandableItemManager.getSavedState());
        }
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewExpandableItemManager != null) {
            mRecyclerViewExpandableItemManager.release();
            mRecyclerViewExpandableItemManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mLayoutManager = null;

        super.onDestroyView();
    }

    @Override
    public void onGroupCollapse(int groupPosition, boolean fromUser) {
    }

    @Override
    public void onGroupExpand(int groupPosition, boolean fromUser) {
        if (fromUser) {
            adjustScrollPositionOnGroupExpanded(groupPosition);
        }
    }

    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int childItemHeight = 43;
        int topMargin = (int) (getActivity().getResources().getDisplayMetrics().density * 16); // top-spacing: 16dp
        int bottomMargin = topMargin; // bottom-spacing: 16dp

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, topMargin, bottomMargin);
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onButtonClick(int groupPosition, int childPosition, int type) {
        CourseSectionData sectionData = (CourseSectionData)mData.getGroupItem(groupPosition);
        switch (type){

            case CourseScheduleAdapter.ADD_EVENT:
                String weekdays =  sectionData.getWeekdays();
                String recurDays = "";
                for(int i = 0; i < weekdays.length(); i++){
                    char c = weekdays.charAt(i);
                    String day = "";
                    if(c == 'T' && weekdays.charAt(i +1) == 'h'){
                        day = "TH";
                        i++;
                    }else{
                        switch (c){
                            case 'M':
                                day = "MO";
                                break;
                            case 'T':
                                day = "TU";
                                break;
                            case 'W':
                                day = "WE";
                                break;
                            case'F':
                                day = "FR";
                                break;
                        }
                    }

                    if(recurDays.equals("")){
                        recurDays = day;
                    }else{
                        recurDays += "," + day;
                    }
                }



                String startTimeS = sectionData.getStartTime();
                String endTimeS = sectionData.getEndTime();
                String[] hoursMinsStart = startTimeS.split(":");
                String[] hoursMinsEnd = endTimeS.split(":");

                int hourStart = Integer.valueOf(hoursMinsStart[0]);
                int minStart = Integer.valueOf(hoursMinsStart[1]);
                int hourEnd = Integer.valueOf(hoursMinsEnd[0]);
                int minEnd = Integer.valueOf(hoursMinsEnd[1]);

                int duration = ((hourEnd * 60) + minEnd)  - ((hourStart * 60) + minStart);
                int hDuration = (duration / 60);
                int mDuration = (duration % 60);



                Calendar beginTime = Calendar.getInstance();
                beginTime.set(2016, 4, 2, hourStart, minStart);

                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
                values.put(CalendarContract.Events.TITLE,  mSubject + " " + mCatalogNumber + " - " + sectionData.getSection());
                values.put(CalendarContract.Events.CALENDAR_ID, 1);
                values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;UNTIL=20160726T170000Z;WKST=SU;BYDAY=" + recurDays);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Toronto");
                values.put(CalendarContract.Events.EVENT_LOCATION, sectionData.getBuilding() + " - " + sectionData.getRoom());
                values.put(CalendarContract.Events.DURATION, "PT" + String.valueOf(hDuration) +
                        "H" + String.valueOf(mDuration) + "M0S");
                Cookie cookie = new Cookie(sectionData.getSection()+ " " + mSubject + mCatalogNumber + mCurrentTerm,
                        groupPosition, childPosition);
                mHandler.startInsert(0,cookie , CalendarContract.Events.CONTENT_URI, values);

                break;
            case CourseScheduleAdapter.DIRECTION:
                mBuildingParser.setParseType(BuildingParser.ParseType.BUILDING_CODE.ordinal());
                mBuildingURL = UWOpenDataAPI.buildURL(mBuildingParser.getEndPoint(((CourseSectionData)mData.getGroupItem(groupPosition)).getBuilding()));

                JSONDownloader downloader = new JSONDownloader(mBuildingURL);
                downloader.setOnDownloadListener(this);
                downloader.start();
                break;
            case CourseScheduleAdapter.VIEW_EVENT:
                // A date-time specified in milliseconds since the epoch.

                Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.valueOf(sectionData.getEventID()));
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(uri);
                startActivity(intent);
                break;
            case CourseScheduleAdapter.ADD_WATCH:
                CourseWatchDBModel data = new CourseWatchDBModel(sectionData.getSection(), mCurrentTerm,
                        mSubject, mCatalogNumber);
                data.setID(mDBHelper.addCourseWatch(data));
                ((CourseSectionFooterData)mData.getChildItem(groupPosition, childPosition)).setBeingWatched(true);
                mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition);

                Intent alarmIntent = new Intent(getActivity().getApplicationContext(), CourseWatchAlarmReceiver.class);
                alarmIntent.putExtra(CourseWatchAlarmReceiver.COURSE_WATCH_DB_MODEL, data);

                final PendingIntent pIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                        data.getID(),alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                //set the alarm an hour before the start time
                alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 300000,
                        AlarmManager.INTERVAL_HOUR, pIntent);
                //http://www.fileformat.info/tip/java/date2millis.htm
                Log.d(TAG, "Setting alarm for " + data.getID() + " " + data.getCourseID());

                break;
            case CourseScheduleAdapter.REMOVE_WATCH:
                int id = mDBHelper.deleteCourseWatch(sectionData.getSection() + " " + mSubject + mCatalogNumber + mCurrentTerm);
                ((CourseSectionFooterData)mData.getChildItem(groupPosition, childPosition)).setBeingWatched(false);
                mRecyclerViewExpandableItemManager.notifyChildItemChanged(groupPosition, childPosition);

                Intent deleteAlarmIntent = new Intent(getActivity().getApplicationContext(), CourseWatchAlarmReceiver.class);
                final PendingIntent deletePIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                        id, deleteAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager deleteAlarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                deleteAlarm.cancel(deletePIntent);

                break;
        }

    }

    @Override
    public void onInsertComplete(int token, final Object cookie, Uri uri) {
        switch (token){
            case 0:
                final Cookie cookieObject = (Cookie)cookie;
                final String[] cookieArr = cookieObject.mText.split(" ");
                final String section = cookieArr[0] + " " + cookieArr[1];
                mLastEventID = Long.parseLong(uri.getLastPathSegment());
                ((CourseSectionFooterData)mData.getChildItem(cookieObject.mGroupPos, cookieObject.mChildPos)).setEventID(String.valueOf(mLastEventID));
                ((CourseSectionData)mData.getGroupItem(cookieObject.mGroupPos)).setEventID(String.valueOf(mLastEventID));
                mRecyclerViewExpandableItemManager.notifyChildItemChanged(cookieObject.mGroupPos, cookieObject.mChildPos);
                mDBHelper.addGoogleCalendarEvent(cookieObject.mText, String.valueOf(mLastEventID));

                Snackbar.make(mRecyclerView, section +" added to Google Calendar", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, mLastEventID);
                                Cookie deleteCookie = new Cookie(section, cookieObject.mGroupPos, cookieObject.mChildPos);
                                mHandler.startDelete(0, deleteCookie, deleteUri, null, null);
                            }
                        })
                        .show();

                Log.i(TAG, cookie + " Event created");
                break;
        }
    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int result) {
        switch (token){
            case 0:
                Cookie cookieObject = (Cookie)cookie;
                ((CourseSectionFooterData)mData.getChildItem(cookieObject.mGroupPos, cookieObject.mChildPos)).setEventID(null);
                ((CourseSectionData)mData.getGroupItem(cookieObject.mGroupPos)).setEventID(null);
                mRecyclerViewExpandableItemManager.notifyChildItemChanged(cookieObject.mGroupPos, cookieObject.mChildPos);

                mDBHelper.deleteCourseEvent(String.valueOf(mLastEventID));
                Snackbar.make(mRecyclerView, cookieObject.mText + " removed from Google Calendar", Snackbar.LENGTH_LONG)
                        .show();

                Log.i(TAG, cookie + " Event removed");
                break;
        }
    }

    public class Cookie{
        String mText;
        int mGroupPos;
        int mChildPos;

        public Cookie(String text, int groupPos, int childPos){
            mText = text;
            mGroupPos = groupPos;
            mChildPos = childPos;
        }
    }

}
