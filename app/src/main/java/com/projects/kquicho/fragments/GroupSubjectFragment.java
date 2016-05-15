package com.projects.kquicho.fragments;



import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.projects.kquicho.activities.CourseTabActivity;
import com.projects.kquicho.models.GroupSubjectData;
import com.projects.kquicho.network.Codes.CodesParser;
import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.JSONDownloader;
import com.projects.kquicho.network.Core.UWOpenDataAPI;
import com.projects.kquicho.network.Course.Course;
import com.projects.kquicho.network.Course.CourseParser;
import com.projects.kquicho.activities.MainActivity;
import com.projects.kquicho.uwhub.R;
import com.projects.kquicho.views.adapters.SearchCourseResultsAdapter;
import com.projects.kquicho.views.adapters.GroupSubjectAdapter;

import java.util.ArrayList;
import java.util.List;


public class GroupSubjectFragment extends Fragment implements JSONDownloader.onDownloadListener,
        RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener,
        GroupSubjectAdapter.onSubjectClickListener,
        MainActivity.FragmentOnBackClickInterface {

public static final String TAG = "GroupSubjectFragment";
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";
    public static final String TITLE = "Courses";
    private final String DATA = "data";
    private final String IS_HIDDEN = "hidden";
    private GroupSubjectData mData;
    private GroupSubjectAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private CodesParser mCodesParser = new CodesParser();
    private String mCodeUrl;
    private CourseParser mCoursesParser = new CourseParser();
    private String mCourseUrl;
    private String mPreviousSearchSubject = "";
    private SearchCourseResultsAdapter mSearchViewAdapter;
    private ArrayList<Course> mPreviousCourses;
    private View mDimOverlay;
    private SearchView mSearchView;
    private FloatingActionButton mFab;
    private View mProgressBar;

    public GroupSubjectFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_subject, parent, false);

        if(savedInstanceState != null){
            mData = savedInstanceState.getParcelable(DATA);
            if(savedInstanceState.getBoolean(IS_HIDDEN)){
                getActivity().getSupportFragmentManager().beginTransaction().hide(this).commit();
            }
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        final Window window = getActivity().getWindow();

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView = searchView;
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getString(R.string.search_courses));

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                MainActivity activity = (MainActivity) getActivity();
                if (!queryTextFocused) {
                    Log.i(TAG, "onFocusChange - searchView lost focus");

                    activity.animateMenuArrowDrawable(false);
                    activity.unlockNavDrawer();

                    searchItem.collapseActionView();
                    searchView.setIconified(true);
                    searchView.setQuery(null, false);

                    if (actionBar != null) {
                        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.theme_primary));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.theme_primary_dark));
                    }
                    mDimOverlay.setVisibility(View.GONE);
                    mFab.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "onFocusChange - searchView gained focus");

                    activity.animateMenuArrowDrawable(true);
                    activity.lockNavDrawer();

                    if (actionBar != null) {
                        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.search_view));
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.setStatusBarColor(Color.BLACK);
                    }

                    mDimOverlay.setVisibility(View.VISIBLE);
                    mFab.setVisibility(View.GONE);
                }
            }
        });


        final Fragment thisFragment = this;
        final JSONDownloader.onDownloadListener onDownloadListener = this;


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setOnQueryTextFocusChangeListener(null);
                ((MainActivity)getActivity()).unlockNavDrawer();

                ArrayList<Course> newCourseList = new ArrayList<>();
                if (mSearchViewAdapter == null) {
                    return false;
                }
                for (int i = 0; i < mSearchViewAdapter.getCount(); i++) {
                    Cursor cursor = (Cursor) mSearchViewAdapter.getItem(i);
                    Course newCourse = new Course();
                    newCourse.setSubject(mPreviousSearchSubject);
                    newCourse.setCatalogNumber(cursor.getString(1).split(" ")[1]);
                    newCourse.setTitle(cursor.getString(2));
                    newCourseList.add(newCourse);
                    cursor.close();
                }

                mDimOverlay.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.theme_primary_dark));
                }
                actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.theme_primary));

                android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                CatalogNumberFragment fragment = CatalogNumberFragment.newInstance(mPreviousSearchSubject, searchView.getQuery().toString(), newCourseList, TITLE);

                ft
                        .add(R.id.fragment_container, fragment, CatalogNumberFragment.TAG)
                        .hide(thisFragment)
                        .addToBackStack(CatalogNumberFragment.TAG)
                        .commit();

                mPreviousSearchSubject = null;
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // perform query here
                if (newText.length() < 2) {
                    return true;
                }
                String subject = "";
                String number = "";
                for (char c : newText.toCharArray()) {
                    if (Character.isLetter(c)) {
                        subject += c;
                    } else if (Character.isDigit(c)) {
                        number += c;
                    }
                }
                subject = subject.toUpperCase();

                if (subject.length() >= 2 && !subject.equals(mPreviousSearchSubject)) {
                    mCoursesParser.setParseType(CourseParser.ParseType.COURSES.ordinal());
                    mCourseUrl = UWOpenDataAPI.buildURL(String.format(mCoursesParser.getEndPoint(), subject));

                    JSONDownloader downloader = new JSONDownloader(getActivity(),mCourseUrl);
                    downloader.setOnDownloadListener(onDownloadListener);
                    downloader.start();
                    mPreviousSearchSubject = subject;
                    mSearchViewAdapter.changeCursor(null);
                } else if (subject.equals(mPreviousSearchSubject) && mPreviousCourses != null) {
                    MatrixCursor matrixCursor = convertToCursor(mPreviousCourses, number);
                    mSearchViewAdapter.changeCursor(matrixCursor);
                } else {
                    mSearchViewAdapter.changeCursor(null);
                }
                return true;
            }
        });

        mSearchViewAdapter = new SearchCourseResultsAdapter(this.getActivity(), R.layout.catalog_number_row, null, columns, null, -1000,
                new SearchCourseResultsAdapter.onSuggestionClickListener() {
                    @Override
                    public void onSuggestionClick(String catalogNumber, String title){
                        searchView.clearFocus();
                        mDimOverlay.setVisibility(View.GONE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.theme_primary_dark));
                        }

                        if(actionBar != null) {
                            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.theme_primary));
                        }

                        Intent intent = new Intent(getActivity(), CourseTabActivity.class);
                        intent.putExtra(CourseTabActivity.CATALOG_NUMBER, catalogNumber);
                        intent.putExtra(CourseTabActivity.SUBJECT, mPreviousSearchSubject);
                        intent.putExtra(CourseTabActivity.SUBTITLE, title);
                        startActivity(intent);
                    }
                });
        searchView.setSuggestionsAdapter(mSearchViewAdapter);

        Log.i("suggestion",searchView.getSuggestionsAdapter() + "");
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.pbLoading);
        mDimOverlay = view.findViewById(R.id.dim_overlay);
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
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        mRecyclerViewExpandableItemManager.attachRecyclerView(mRecyclerView);

        mFab = (FloatingActionButton)view.findViewById(R.id.fab);


        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerViewExpandableItemManager.collapseAll();
            }
        });

        if(mData == null) {
            mCodesParser.setParseType(CodesParser.ParseType.GROUPS_WITH_SUBJECTS.ordinal());
            mCodeUrl = UWOpenDataAPI.buildURL(mCodesParser.getEndPoint());

            mProgressBar.setVisibility(View.VISIBLE);
            JSONDownloader downloader = new JSONDownloader(getActivity(),mCodeUrl);
            downloader.setOnDownloadListener(this);
            downloader.start();
        }else{
            mProgressBar.setVisibility(View.GONE);
            mFab.setVisibility(View.VISIBLE);
            mAdapter = new GroupSubjectAdapter(mData, this);
            mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);      // wrap for expanding
            mRecyclerView.setAdapter(mWrappedAdapter);
        }


        mDimOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.clearFocus();
            }
        });
    }


    @Override
    public void onDownloadFail(String givenURL, int index, boolean noNetwork) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }
    @Override
    public void onDownloadComplete(APIResult apiResult) {
        Activity activity = getActivity();
        if(activity == null){
            return;
        }
        if(apiResult.getUrl().equals(mCodeUrl)){
            mCodesParser.setAPIResult(apiResult);
            mCodesParser.parseJSON();
            if(mCodesParser.getParseType() == CodesParser.ParseType.GROUPS_WITH_SUBJECTS){
                mCodesParser.setParseType(CodesParser.ParseType.SUBJECTS_WITH_GROUPS.ordinal());
                mCodeUrl = UWOpenDataAPI.buildURL(mCodesParser.getEndPoint());

                JSONDownloader downloader = new JSONDownloader(getActivity(),mCodeUrl);
                downloader.setOnDownloadListener(this);
                downloader.start();
            }else{
                mData = new GroupSubjectData(mCodesParser.getSubjectsWithGroups());

                android.os.Handler handler = new android.os.Handler(activity.getMainLooper());

                final GroupSubjectAdapter.onSubjectClickListener subjectClickListener = this;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mFab.setVisibility(View.VISIBLE);
                        mAdapter = new GroupSubjectAdapter(mData, subjectClickListener);
                        mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);      // wrap for expanding
                        mRecyclerView.setAdapter(mWrappedAdapter);
                    }
                };
                handler.post(runnable);
                Log.i(TAG, "complete");
            }
        }else{
            mCoursesParser.setAPIResult(apiResult);
            mCoursesParser.parseJSON();
            mPreviousCourses = mCoursesParser.getCourses();
            if(mPreviousCourses != null && mPreviousCourses.size() > 0){
                final MatrixCursor matrixCursor = convertToCursor(mPreviousCourses);
                android.os.Handler handler = new android.os.Handler(activity.getMainLooper());
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mSearchViewAdapter.changeCursor(matrixCursor);

                    }
                };
                handler.post(runnable);
            }

            Log.i(TAG, "complete");
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DATA, mData);
        outState.putBoolean(IS_HIDDEN, isHidden());

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
    public void onGroupExpand(final int groupPosition, boolean fromUser) {
        if (fromUser) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    adjustScrollPositionOnGroupExpanded(groupPosition);
                }
            }, 300);
        }
    }

    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int childItemHeight = (int) (getActivity().getResources().getDisplayMetrics().density * 55);
        int topMargin = (int) (getActivity().getResources().getDisplayMetrics().density * 16); // top-spacing: 16dp
        int bottomMargin = topMargin; // bottom-spacing: 16dp

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, 0, 0);
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
    public static String[] columns = new String[]{"_id","catalogNumber", "title"};

    private MatrixCursor convertToCursor(List<Course> courseResults) {
        MatrixCursor cursor = new MatrixCursor(columns);
        int i = 0;
        for (Course courseResult : courseResults) {
            String[] temp = new String[3];
            i = i + 1;
            temp[0] = Integer.toString(i);
            temp[1] = mPreviousSearchSubject + " " + courseResult.getCatalogNumber();
            temp[2] = courseResult.getTitle();
            cursor.addRow(temp);
        }
        return cursor;
    }
    private MatrixCursor convertToCursor(List<Course> courseResults, String number) {
        MatrixCursor cursor = new MatrixCursor(columns);
        int i = 0;
        for (Course courseResult : courseResults) {
            String catalogNumber = courseResult.getCatalogNumber();
            int j = 0;
            boolean shouldAdd = catalogNumber.length() >= number.length();
            if(shouldAdd) {
                for (char c : number.toCharArray()) {
                    if (c != catalogNumber.charAt(j)){
                        shouldAdd = false;
                        break;
                    }
                    j++;
                }
                if(shouldAdd) {
                    String[] temp = new String[3];
                    i = i + 1;
                    temp[0] = Integer.toString(i);
                    temp[1] = mPreviousSearchSubject + " " + catalogNumber;
                    temp[2] = courseResult.getTitle();
                    cursor.addRow(temp);
                }
            }
        }
        return cursor;
    }


    @Override
    public void onSubjectClick (String subject) {
        android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        CatalogNumberFragment fragment = CatalogNumberFragment.newInstance(subject, TITLE);

        ft
                .add(R.id.fragment_container, fragment,CatalogNumberFragment.TAG)
                .hide(this)
                .addToBackStack(CatalogNumberFragment.TAG)
                .commit();
    }

    @Override
    public void onFragmentBackPressed() {
        Log.i(TAG, "onFragmentBackPressed");
        if(!mSearchView.isIconified()   ) {
            mSearchView.setQuery(null, false);
            mSearchView.setIconified(true);
            return;
        }
        ((MainActivity)getActivity()).navigateToHome();
    }
}
