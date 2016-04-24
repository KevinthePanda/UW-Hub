package com.projects.kquicho.uwatm8;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.projects.kquicho.uw_api_client.Codes.Term;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Course.Course;
import com.projects.kquicho.uw_api_client.Course.CourseParser;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class CatalogNumberFragment extends Fragment implements JSONDownloader.onDownloadListener,
        CoursesAdapter.onCourseClickListener,
        MainActivity.FragmentOnBackClickInterface{
    public static final String TAG = "CatalogNumberFragment";
    private static final String SUBJECT = "subject";
    private static final String CALLING_FRAGMENT_TITLE = "callingFragmentTitle";
    private static final String DATA = "data";
    private static final String TITLE = "title";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CourseParser mCoursesParser = new CourseParser();
    private String mUrl;
    private String mSubject;
    private String mTitle = null;
    private ArrayList<Course> mData;
    private CoursesAdapter mAdapter;
    private String mCallingFragmentTitle = null;
    private static boolean mIsFromSearch = false;
    private View mEmptyView;
    private View mProgressBar;

    public static CatalogNumberFragment newInstance(String subject, String callingFragmentTitle) {
        Bundle args = new Bundle();
        args.putString(SUBJECT, subject);
        args.putString(CALLING_FRAGMENT_TITLE, callingFragmentTitle);
        CatalogNumberFragment fragment = new CatalogNumberFragment();
        fragment.setArguments(args);
        mIsFromSearch = false;
        return fragment;
    }

    public static CatalogNumberFragment newInstance(String subject, String title, ArrayList<Course> data, String callingFragmentTitle) {
        Bundle args = new Bundle();
        args.putString(SUBJECT, subject);
        args.putString(TITLE, title);
        args.putString(CALLING_FRAGMENT_TITLE, callingFragmentTitle);
        args.putParcelableArrayList(DATA, data);
        CatalogNumberFragment fragment = new CatalogNumberFragment();
        fragment.setArguments(args);
        mIsFromSearch = true;
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalog_number, parent, false);

        Bundle args = savedInstanceState != null ? savedInstanceState : getArguments();
        mSubject = args.getString(SUBJECT);
        mCallingFragmentTitle = args.getString(CALLING_FRAGMENT_TITLE);
        mTitle = args.getString(TITLE);
        if(args.containsKey(DATA)){
            mData = args.getParcelableArrayList(DATA);
        }
        if(savedInstanceState == null) {
            android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                if (mTitle == null) {
                    actionBar.setTitle(mSubject);
                } else {
                    actionBar.setTitle(mTitle);
                }
            }
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.pbLoading);
        mEmptyView = view.findViewById(R.id.empty_view);
        mRecyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

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
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));

        if(mIsFromSearch) {
            ((MainActivity) getActivity()).setMenuArrowDrawable(true);
        }else{
            ((MainActivity) getActivity()).animateMenuArrowDrawable(true);
        }

        if(mData != null && mData.size() != 0) {
            mEmptyView.setVisibility(View.GONE);
            mAdapter = new CoursesAdapter(mData, this);
            mRecyclerView.setAdapter(mAdapter);
        }else {
            mCoursesParser.setParseType(CourseParser.ParseType.COURSES.ordinal());
            mUrl = UWOpenDataAPI.buildURL(String.format(mCoursesParser.getEndPoint(), mSubject));

            mProgressBar.setVisibility(View.VISIBLE);
            JSONDownloader downloader = new JSONDownloader(mUrl);
            downloader.setOnDownloadListener(this);
            downloader.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        outState.putParcelableArrayList(DATA, mData);
        outState.putString(SUBJECT, mSubject);
        outState.putString(CALLING_FRAGMENT_TITLE, mCallingFragmentTitle);
        if(mTitle != null) {
            outState.putString(TITLE, mTitle);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
        mProgressBar.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDownloadComplete(APIResult apiResult) {
        Activity activity = getActivity();
        if(activity == null) {
            return;
        }
        mCoursesParser.setAPIResult(apiResult);
        mCoursesParser.parseJSON();
        mData = mCoursesParser.getCourses();

        android.os.Handler handler = new android.os.Handler(activity.getMainLooper());

        final CoursesAdapter.onCourseClickListener courseClickListener = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                if(mData == null || mData.size() == 0){
                    mEmptyView.setVisibility(View.VISIBLE);
                }else {
                    mEmptyView.setVisibility(View.GONE);
                    mAdapter = new CoursesAdapter(mData, courseClickListener);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        };
        handler.post(runnable);
        Log.i(TAG, "complete");
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onCourseClick(String catalogNumber, String title) {
        Intent intent = new Intent(getActivity(), CourseTabActivity.class);
        intent.putExtra(CourseTabActivity.CATALOG_NUMBER, catalogNumber);
        intent.putExtra(CourseTabActivity.SUBJECT, mSubject);
        intent.putExtra(CourseTabActivity.SUBTITLE, title);
        startActivity(intent);
    }

    @Override
    public void onFragmentBackPressed() {
        Log.i(TAG, "onFragmentBackPressed");
        MainActivity activity = (MainActivity)getActivity();
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(mCallingFragmentTitle);
        }

        activity.animateMenuArrowDrawable(false);
        activity.getSupportFragmentManager().popBackStackImmediate();
    }


}
