package com.projects.kquicho.uwatm8;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by Kevin Quicho on 3/16/2016.
 */
public class CourseTabFragment extends Fragment implements MainActivity.FragmentOnBackClickInterface{
    public static final String TAG = "courseTabFragment";
    public static final String SUBJECT_TAG = "subject";
    public static final String CATALOG_NUMBER_TAG = "catalogNumber";
    public static final String TITLE_TAG = "title";
    public static final String CALLING_FRAGMENT_TITLE = "callingFragmentTitle";
    private String mSubject;
    private String mCatalogNumber;
    private String mTitle;
    private String mCallingFragmentTitle = null;


    public static CourseTabFragment newInstance(String subject, String catalogNumber, String title, String callingFragmentTitle) {

        Bundle args = new Bundle();
        args.putString(SUBJECT_TAG, subject);
        args.putString(CATALOG_NUMBER_TAG, catalogNumber);
        args.putString(TITLE_TAG, title);
        args.putString(CALLING_FRAGMENT_TITLE, callingFragmentTitle);
        CourseTabFragment fragment = new CourseTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSubject = args.getString(SUBJECT_TAG);
        mCatalogNumber = args.getString(CATALOG_NUMBER_TAG);
        mTitle = args.getString(TITLE_TAG);
        mCallingFragmentTitle = args.getString(CALLING_FRAGMENT_TITLE);

        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(mSubject + " " + mCatalogNumber);
            actionBar.setSubtitle(mTitle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_course_tabs, container, false);

        final TabLayout tabLayout = (TabLayout) inflatedView.findViewById(R.id.tabLayout);
        final ViewPager viewPager = (ViewPager) inflatedView.findViewById(R.id.viewpager);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        viewPager.setAdapter(new PagerAdapter
                (getFragmentManager()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if(mCallingFragmentTitle.equals(GroupSubjectFragment.TITLE)){
            ((MainActivity) getActivity()).setMenuArrowDrawable(true);

        }
        return inflatedView;
    }

    @Override
    public void onFragmentBackPressed() {
        Log.i(TAG, "onFragmentBackPressed");
        MainActivity activity = (MainActivity)getActivity();
        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null){
            actionBar.setSubtitle(null);
            actionBar.setTitle(mCallingFragmentTitle);
        }


        if(mCallingFragmentTitle.equals(GroupSubjectFragment.TITLE)){
            activity.animateMenuArrowDrawable(false);
        }
        activity.getSupportFragmentManager().popBackStackImmediate();
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] { "Details", "Schedule" };

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return CourseDetailsFragment.newInstance(mSubject, mCatalogNumber);
                case 1:
                    return  CourseScheduleFragment.newInstance(mSubject, mCatalogNumber);
                default:
                    return null;
            }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }

}
