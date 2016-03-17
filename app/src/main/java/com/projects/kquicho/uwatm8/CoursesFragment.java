package com.projects.kquicho.uwatm8;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
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
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Course.Course;
import com.projects.kquicho.uw_api_client.Course.CourseParser;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 3/14/2016.
 */
public class CoursesFragment extends Fragment implements JSONDownloader.onDownloadListener,
        CoursesAdapter.onCourseClickListener{
    public static final String TAG = "CoursesFragment";
    private static final String SUBJECT = "subject";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CourseParser mCoursesParser = new CourseParser();
    private String mUrl;
    private String mSubject;
    private ArrayList<Course> mData;
    private CoursesAdapter mAdapter;

    public static CoursesFragment newInstance(String subject) {
        Bundle args = new Bundle();
        args.putString(SUBJECT, subject);
        CoursesFragment fragment = new CoursesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mSubject = getArguments().getString(SUBJECT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, parent, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

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

        mCoursesParser.setParseType(CourseParser.ParseType.COURSES.ordinal());
        mUrl = UWOpenDataAPI.buildURL(String.format(mCoursesParser.getEndPoint(), mSubject));

        JSONDownloader downloader = new JSONDownloader(mUrl);
        downloader.setOnDownloadListener(this);
        downloader.start();

    }


    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        mCoursesParser.setAPIResult(apiResult);
        mCoursesParser.parseJSON();
        mData = mCoursesParser.getCourses();

        android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

        final CoursesAdapter.onCourseClickListener courseClickListener = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAdapter = new CoursesAdapter(mData, courseClickListener);
                mRecyclerView.setAdapter(mAdapter);
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
        android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        CourseTabFragment fragment = CourseTabFragment.newInstance(mSubject, catalogNumber, title);
        ft
                .add(R.id.fragment_container, fragment)
                .hide(this)
                .addToBackStack(CourseTabFragment.TAG)
                .commit();
    }


}
