package com.projects.kquicho.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.projects.kquicho.database.CourseDBHelper;
import com.projects.kquicho.models.CourseScheduleData;
import com.projects.kquicho.models.CourseSectionData;
import com.projects.kquicho.models.CourseSectionFooterData;
import com.projects.kquicho.network.Building.BuildingParser;
import com.projects.kquicho.network.Building.UWBuilding;
import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.JSONDownloader;
import com.projects.kquicho.network.Core.UWOpenDataAPI;
import com.projects.kquicho.network.Terms.TermsParser;
import com.projects.kquicho.utils.AsyncHandler;
import com.projects.kquicho.utils.CourseWatchAlarmReceiver;
import com.projects.kquicho.models.CourseWatchDBModel;
import com.projects.kquicho.uwhub.R;
import com.projects.kquicho.views.adapters.CourseScheduleAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    public static final String SUBJECT = "subject";
    public static final String CATALOG_NUMBER = "catalogNumber";
    private final int WINTER = 1;
    private final int SPRING = 5;
    private final int FALL = 9;
    private final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;
    private final int PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;

    private final String DATA = "data";
    private final String IS_CURRENT_TERM_SELECTED = "isCurrentTermSelected";
    private final String CURRENT_TERM = "currentTerm";
    private final String NEXT_TERM = "nextTerm";
    private final String COURSE_SECTION_DATA = "courseSectionData";
    private final String GROUP_POS = "groupPos";
    private final String CHILD_POS = "childPos";

    private CourseScheduleData mData;
    private CourseScheduleAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView.Adapter mWrappedAdapter;


    private String mTerm;
    private String mCurrentTerm;
    private String mNextTerm;
    private String mSubject;
    private String mCatalogNumber;
    private TermsParser mTermsParser = new TermsParser();
    private String mCourseScheduleURL;
    private String mTermListURL;
    private BuildingParser mBuildingParser = new BuildingParser();
    private String mBuildingURL;
    private Long mLastEventID = null;
    private AsyncHandler mHandler;
    private CourseDBHelper mDBHelper;
    private View mEmptyView;
    private View mProgressBar;
    private RadioButton mCurrentTermRB;
    private RadioButton mNextTermRB;
    private View mContainer;
    private CourseSectionData mCourseSectionData;
    private Integer mGroupPos;
    private Integer mChildPos;

    public static CourseScheduleFragment newInstance(String subject, String catalogNumber) {

        Bundle args = new Bundle();
        args.putString(SUBJECT, subject);
        args.putString(CATALOG_NUMBER, catalogNumber);
        CourseScheduleFragment fragment = new CourseScheduleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSubject = args.getString(SUBJECT);
        mCatalogNumber = args.getString(CATALOG_NUMBER);
        mHandler = new AsyncHandler(getActivity().getContentResolver(), this);

        mDBHelper = CourseDBHelper.getInstance(getActivity().getApplicationContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_schedule, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        mCurrentTermRB = (RadioButton)view.findViewById(R.id.current_term);
        mNextTermRB = (RadioButton)view.findViewById(R.id.next_term);
        mProgressBar = view.findViewById(R.id.pbLoading);
        mEmptyView = view.findViewById(R.id.empty_view);
        mRecyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mContainer = view;

        if(savedInstanceState != null){
            Log.d(TAG, "savedInstanceState != null");
            mData = savedInstanceState.getParcelable(DATA);
            boolean isCurrentSelected = savedInstanceState.getBoolean(IS_CURRENT_TERM_SELECTED);
            mCurrentTermRB.setSelected(isCurrentSelected);
            mCurrentTerm = savedInstanceState.getString(CURRENT_TERM);
            mNextTerm = savedInstanceState.getString(NEXT_TERM);
            mTerm = mNextTerm;
            if(isCurrentSelected){
                mTerm = mCurrentTerm;
            }
            mCourseSectionData = savedInstanceState.getParcelable(COURSE_SECTION_DATA);
            mGroupPos = savedInstanceState.getInt(GROUP_POS);
            mChildPos = savedInstanceState.getInt(CHILD_POS);
        }

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

        if(mData == null) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                        PERMISSIONS_REQUEST_GET_ACCOUNTS);

            }else {
                mTermsParser.setParseType(TermsParser.ParseType.TERM_LIST.ordinal());
                mTermListURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint());
                JSONDownloader downloader = new JSONDownloader(getActivity(), mTermListURL);
                downloader.setOnDownloadListener(this);
                downloader.start();
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }else{
            mCurrentTermRB.setText(getTermTitle(mCurrentTerm));
            mNextTermRB.setText(getTermTitle(mNextTerm));
            final JSONDownloader.onDownloadListener onDownloadListener = this;

            android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCurrentTermRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mTerm = mCurrentTerm;
                                mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
                                mCourseScheduleURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(mCurrentTerm, mSubject, mCatalogNumber));

                                mProgressBar.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                                mRecyclerViewExpandableItemManager.collapseAll();
                                mEmptyView.setVisibility(View.GONE);
                                JSONDownloader downloader = new JSONDownloader(getActivity(),mCourseScheduleURL);
                                downloader.setOnDownloadListener(onDownloadListener);
                                downloader.start();
                            }
                        }
                    });

                    mNextTermRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                mTerm = mNextTerm;
                                mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
                                mCourseScheduleURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(mNextTerm, mSubject, mCatalogNumber));

                                mProgressBar.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                                mRecyclerViewExpandableItemManager.collapseAll();
                                mEmptyView.setVisibility(View.GONE);
                                JSONDownloader downloader = new JSONDownloader(getActivity(),mCourseScheduleURL);
                                downloader.setOnDownloadListener(onDownloadListener);
                                downloader.start();
                            }
                        }
                    });
                }
            });
            mProgressBar.setVisibility(View.GONE);
            if (mData == null || mData.getGroupCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mAdapter = new CourseScheduleAdapter(mData, getActivity(),
                        this);
                mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);      // wrap for expanding
                mRecyclerView.setAdapter(mWrappedAdapter);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                View perm_msg = mContainer.findViewById(R.id.permission_msg);
                View perm_btn = mContainer.findViewById(R.id.permission_btn);
                View term_toggle = mContainer.findViewById(R.id.term_toggle);
                View coord_layout = mContainer.findViewById(R.id.group_view);

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mTermsParser.setParseType(TermsParser.ParseType.TERM_LIST.ordinal());
                    mTermListURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint());
                    JSONDownloader downloader = new JSONDownloader(getActivity(),mTermListURL);
                    downloader.setOnDownloadListener(this);
                    downloader.start();
                    mProgressBar.setVisibility(View.VISIBLE);
                    perm_msg.setVisibility(View.GONE);
                    perm_btn.setVisibility(View.GONE);
                    term_toggle.setVisibility(View.VISIBLE);
                    coord_layout.setVisibility(View.VISIBLE);
                } else {
                    perm_msg.setVisibility(View.VISIBLE);
                    perm_btn.setVisibility(View.VISIBLE);
                    term_toggle.setVisibility(View.GONE);
                    coord_layout.setVisibility(View.GONE);
                    perm_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS},
                                    PERMISSIONS_REQUEST_GET_ACCOUNTS);
                        }
                    });
                }
                return;
            }
            case PERMISSIONS_REQUEST_WRITE_CALENDAR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!mCourseSectionData.getSection().split(" ")[0].equals("TST")) {
                        createLECorTUTEvent(mCourseSectionData, mGroupPos, mChildPos);
                    }else{
                        createTSTEvent(mCourseSectionData, mGroupPos, mChildPos);
                    }
                } else {
                    Snackbar.make(mContainer.findViewById(R.id.group_view),
                            "Requires \"Write Calendar\" Permission", Snackbar.LENGTH_LONG).show();

                }
                mCourseSectionData = null;
                mGroupPos = null;
                mChildPos = null;
                return;


        }
    }


    @Override
    public void onDownloadFail(String givenURL, int index, boolean noConnection) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    private String getTermTitle(String id){
        String season = "";
        switch (Integer.valueOf(id.substring(3))){
            case WINTER:
                season = "Winter";
                break;
            case SPRING:
                season = "Spring";
                break;
            case FALL:
                season = "Fall";
        }
        return  season + " " + "20" + id.substring(1,3);
    }


    @Override
    public void onDownloadComplete(APIResult  apiResult) {
        Log.i(TAG, "onDownloadComplete");
        final Activity activity = getActivity();
        if(activity == null){
            return;
        }

        if(apiResult.getUrl().equals(mTermListURL)){
            Log.i(TAG, "mTermListURL");
            mTermsParser.setAPIResult(apiResult);
            mTermsParser.parseJSON();
            final String currentTerm = mTermsParser.getCurrentTerm();
            final String nextTerm = mTermsParser.getNextTerm();
            mCurrentTerm = currentTerm;
            mNextTerm = nextTerm;
            mTerm = currentTerm;
            final String currentTermTitle  = getTermTitle(currentTerm);
            final String nextTermTitle  = getTermTitle(nextTerm);

            android.os.Handler handler = new android.os.Handler(activity.getMainLooper());
            final JSONDownloader.onDownloadListener onDownloadListener = this;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCurrentTermRB.setText(currentTermTitle);
                    mNextTermRB.setText(nextTermTitle);

                    mCurrentTermRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                mTerm = currentTerm;
                                mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
                                mCourseScheduleURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(currentTerm, mSubject, mCatalogNumber));

                                mProgressBar.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                                mRecyclerViewExpandableItemManager.collapseAll();
                                mEmptyView.setVisibility(View.GONE);
                                JSONDownloader downloader = new JSONDownloader(getActivity(),mCourseScheduleURL);
                                downloader.setOnDownloadListener(onDownloadListener);
                                downloader.start();
                            }
                        }
                    });

                    mNextTermRB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked) {
                                mTerm = nextTerm;
                                mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
                                mCourseScheduleURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(nextTerm, mSubject, mCatalogNumber));

                                mProgressBar.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                                mRecyclerViewExpandableItemManager.collapseAll();
                                mEmptyView.setVisibility(View.GONE);
                                JSONDownloader downloader = new JSONDownloader(getActivity(),mCourseScheduleURL);
                                downloader.setOnDownloadListener(onDownloadListener);
                                downloader.start();
                            }
                        }
                    });

                    mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
                    mCourseScheduleURL = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint(currentTerm,mSubject, mCatalogNumber));

                    mProgressBar.setVisibility(View.VISIBLE);
                    JSONDownloader downloader = new JSONDownloader(getActivity(),mCourseScheduleURL);
                    downloader.setOnDownloadListener(onDownloadListener);
                    downloader.start();
                }
            });

        }
        else if(apiResult.getUrl().equals(mCourseScheduleURL)) {
            Log.i(TAG, "mCourseScheduleURL");
            mTermsParser.setAPIResult(apiResult);
            mTermsParser.parseJSON(activity.getApplicationContext());

            android.os.Handler handler = new android.os.Handler(activity.getMainLooper());
            Runnable runnable;
            if(mAdapter == null) {
                mData = new CourseScheduleData(mTermsParser.getCourseSchedule());
                final CourseScheduleAdapter.onButtonClickListener onButtonClickListener = this;
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        if (mData == null || mData.getGroupCount() == 0) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        } else {
                            mEmptyView.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mAdapter = new CourseScheduleAdapter(mData, activity,
                                    onButtonClickListener);
                            mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);      // wrap for expanding
                            mRecyclerView.setAdapter(mWrappedAdapter);
                        }
                    }
                };
            }else{
                mData.changeData(mTermsParser.getCourseSchedule());
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                        if (mData == null || mData.getGroupCount() == 0) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        } else {
                            mEmptyView.setVisibility(View.GONE);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(getActivity() == null){
                                        return;
                                    }
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                }
                            }, 150);
                        }
                    }
                };
            }
            handler.post(runnable);

        }else{
            Log.i(TAG, "mBuildingParser");
            mBuildingParser.setAPIResult(apiResult);
            mBuildingParser.parseJSON();

            UWBuilding building = mBuildingParser.getBuildingCodeBuilding();
            if (building != null) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + String.valueOf(building.getLatitude()) + ","
                        + String.valueOf(building.getLongitude()) + "(" + building.getBuildingCode() + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }else{
                Snackbar.make(mRecyclerView, "Section has no location to search", Snackbar.LENGTH_LONG).show();
            }
        }
        Log.i(TAG, "Completed ");

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSavedInstanceState");

        // save current state to support screen rotation, etc...
        if (mRecyclerViewExpandableItemManager != null) {
            outState.putParcelable(
                    SAVED_STATE_EXPANDABLE_ITEM_MANAGER,
                    mRecyclerViewExpandableItemManager.getSavedState());
        }

        outState.putParcelable(DATA, mData);
        outState.putBoolean(IS_CURRENT_TERM_SELECTED, mCurrentTermRB.isSelected());
        outState.putString(CURRENT_TERM, mCurrentTerm);
        outState.putString(NEXT_TERM, mNextTerm);
        outState.putParcelable(COURSE_SECTION_DATA, mCourseSectionData);
        outState.putInt(GROUP_POS, mGroupPos == null ? 0 : mGroupPos);
        outState.putInt(CHILD_POS, mChildPos == null ? 0 : mChildPos);
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
    public void onGroupExpand(final int groupPosition, boolean fromUser) {
        if (fromUser) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adjustScrollPositionOnGroupExpanded(groupPosition);
                }
            }, 250);
        }
    }

    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int childItemHeight = (int) (getActivity().getResources().getDisplayMetrics().density * 25);
        int topMargin = (int) (getActivity().getResources().getDisplayMetrics().density * 2);
        int bottomMargin = (int) (getActivity().getResources().getDisplayMetrics().density * (8 + 45));

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, topMargin, bottomMargin);
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    private void createLECorTUTEvent(CourseSectionData sectionData, int groupPosition, int childPosition){
        String weekdays =  sectionData.getWeekdays();
        String recurDays = "";
        int firstOccurence = 0;
        for(int i = 0; i < weekdays.length(); i++){
            char c = weekdays.charAt(i);
            String day = "";
            int calendarDay = 0;
            if(c == 'T' && weekdays.length() > i + 1 && weekdays.charAt(i +1) == 'h'){
                day = "TH";
                i++;
                calendarDay = Calendar.THURSDAY;
            }else{
                switch (c){
                    case 'M':
                        day = "MO";
                        calendarDay = Calendar.MONDAY;
                        break;
                    case 'T':
                        day = "TU";
                        calendarDay = Calendar.TUESDAY;
                        break;
                    case 'W':
                        day = "WE";
                        calendarDay = Calendar.WEDNESDAY;
                        break;
                    case'F':
                        day = "FR";
                        calendarDay = Calendar.FRIDAY;
                        break;
                }
            }

            if(recurDays.equals("")){
                recurDays = day;
                firstOccurence = calendarDay;
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
        Calendar endTime = Calendar.getInstance();
        beginTime.set(Calendar.DAY_OF_WEEK, firstOccurence);
        int year =  Integer.valueOf("20" + mTerm.substring(1, 3));
        int month = Integer.valueOf(mTerm.substring(3)) - 1;
        switch (month){
            case 0:
                beginTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                endTime.set(Calendar.MONTH, 3);
                endTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                endTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                break;
            case 4:
                beginTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                endTime.set(Calendar.MONTH, 6);
                endTime.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                endTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, -1);

                break;
            case 8:
                beginTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 2);

                endTime.set(Calendar.MONTH, 11);
                endTime.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                endTime.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

                break;
        }
        beginTime.set(Calendar.YEAR, year);
        beginTime.set(Calendar.MONTH, month);
        beginTime.set(Calendar.HOUR_OF_DAY, hourStart);
        beginTime.set(Calendar.MINUTE, minStart);

        endTime.set(Calendar.YEAR, year);
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CANADA);
        String endDate = dateFormat.format(endTime.getTime());

        final ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE,  mSubject + " " + mCatalogNumber + " - " + sectionData.getSection());
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;UNTIL=" + endDate.toString() +"T170000Z;WKST=SU;BYDAY=" + recurDays);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Toronto");
        values.put(CalendarContract.Events.EVENT_LOCATION, sectionData.getBuilding() + " - " + sectionData.getRoom());
        values.put(CalendarContract.Events.DURATION, "PT" + String.valueOf(hDuration) +
                "H" + String.valueOf(mDuration) + "M0S");
        final Cookie cookie = new Cookie(sectionData.getSection()+ " " + mSubject + mCatalogNumber + mTerm,
                groupPosition, childPosition);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Google Calendar Event");
        alertDialog.setMessage("Are you sure you want to add this event to your Primary Google Calendar?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.startInsert(0,cookie , CalendarContract.Events.CONTENT_URI, values);
                        dialog.dismiss();

                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private void createTSTEvent(CourseSectionData sectionData, int groupPosition, int childPosition){
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


        int year =  Integer.valueOf("20" + mTerm.substring(1, 3));
        DateFormat dateFormat = new SimpleDateFormat("MM/dd yyyy HH:mm", Locale.CANADA);
        Date date = null;
        try {
            date = dateFormat.parse(sectionData.getDate() + " " + year + " " + sectionData.getStartTime());
        }catch (ParseException ex){
            Log.e(TAG, ex.getMessage());
            return;
        }
        final ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, date.getTime());
        values.put(CalendarContract.Events.TITLE,  mSubject + " " + mCatalogNumber + " - " + sectionData.getSection());
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Toronto");
        String building =  sectionData.getBuilding();
        String room =  sectionData.getRoom();
        if(building != null && !building.equals("null") && room != null && !room.equals("null")) {
            values.put(CalendarContract.Events.EVENT_LOCATION, sectionData.getBuilding() + " - " + sectionData.getRoom());
        }
        values.put(CalendarContract.Events.DURATION, "PT" + String.valueOf(hDuration) +
                "H" + String.valueOf(mDuration) + "M0S");
        final Cookie cookie = new Cookie(sectionData.getSection()+ " " + mSubject + mCatalogNumber + mTerm,
                groupPosition, childPosition);

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Google Calendar Event");
        alertDialog.setMessage("Are you sure you want to add this event to your Primary Google Calendar?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.startInsert(0,cookie , CalendarContract.Events.CONTENT_URI, values);
                        dialog.dismiss();

                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();


    }

    @Override
    public void onButtonClick(int groupPosition, int childPosition, int type) {
        CourseSectionData sectionData = (CourseSectionData)mData.getGroupItem(groupPosition);
        switch (type){

            case CourseScheduleAdapter.ADD_EVENT:
                if(sectionData.getCampus().contains("ONLINE")){
                    Snackbar.make(mRecyclerView, "Cannot add online sections to calendar", Snackbar.LENGTH_LONG).show();
                    return;
                }else if(sectionData.getWeekdays().equals("TBA") || !sectionData.getStartTime().contains(":")
                || !sectionData.getEndTime().contains(":") || sectionData.getSection() == null ||
                        sectionData.getWeekdays().contains("(")){
                    Snackbar.make(mRecyclerView, "Section has incompatible data, cannot create an event ", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    mCourseSectionData = sectionData;
                    mGroupPos = groupPosition;
                    mChildPos = childPosition;
                    requestPermissions(new String[]{Manifest.permission.WRITE_CALENDAR}, PERMISSIONS_REQUEST_WRITE_CALENDAR);
                }else{
                    if(!sectionData.getSection().split(" ")[0].equals("TST")) {
                        createLECorTUTEvent(sectionData, groupPosition, childPosition);
                    }else{
                        createTSTEvent(sectionData, groupPosition, childPosition);
                    }
                }
                break;
            case CourseScheduleAdapter.DIRECTION:
                mBuildingParser.setParseType(BuildingParser.ParseType.BUILDING_CODE.ordinal());
                mBuildingURL = UWOpenDataAPI.buildURL(mBuildingParser.getEndPoint(sectionData.getBuilding()));

            JSONDownloader downloader = new JSONDownloader(getActivity(),mBuildingURL);
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
                if(sectionData.getEnrollmentTotal() < sectionData.getEnrollmentCapacity()){
                    Snackbar.make(mRecyclerView, "Can't watch sections without full enrollment", Snackbar.LENGTH_LONG).show();
                    return;
                }
                CourseWatchDBModel data = new CourseWatchDBModel(sectionData.getSection(), mTerm,
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
                Snackbar.make(mRecyclerView, "You'll recieve a notification when the section is no longer full", Snackbar.LENGTH_LONG).show();

                break;
            case CourseScheduleAdapter.REMOVE_WATCH:
                int id = mDBHelper.deleteCourseWatch(sectionData.getSection() + " " + mSubject + mCatalogNumber + mTerm);
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
        Log.d(TAG, "onInsertComplete");
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
