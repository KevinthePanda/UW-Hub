package com.projects.kquicho.uwhub;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


/**
 * Created by Kevin Quicho on 3/16/2016.
 */
public class CourseTabActivity extends AppCompatActivity {
    public static final String TAG = "courseTabFragment";
    public static final String SUBJECT = "subject";
    public static final String CATALOG_NUMBER = "catalogNumber";
    public static final String SUBTITLE = "subtitle";
    public static final String POSITION = "position";
    private static String mSubject;
    private static String mCatalogNumber;
    private String mSubtitle;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private int mCurrentPosition = 0;


    @Override
    public void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_tabs);

        if(savedInstanceState == null) {
            Intent intent = getIntent();
            mSubject = intent.getStringExtra(SUBJECT);
            mCatalogNumber = intent.getStringExtra(CATALOG_NUMBER);
            mSubtitle = intent.getStringExtra(SUBTITLE);
        }else{
            mSubject = savedInstanceState.getString(SUBJECT);
            mCatalogNumber = savedInstanceState.getString(CATALOG_NUMBER);
            mSubtitle = savedInstanceState.getString(SUBTITLE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mSubject + " " + mCatalogNumber);
            actionBar.setSubtitle(mSubtitle);
        }


        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager = viewPager;
        mTabLayout = tabLayout;
        if(tabLayout!= null) {
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
            tabLayout.post(new Runnable() {
                @Override
                public void run() {
                    tabLayout.setupWithViewPager(viewPager);
                    if(savedInstanceState != null)
                        mViewPager.setCurrentItem(savedInstanceState.getInt(POSITION));
                }
            });
        }
        if(viewPager !=null) {
            viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        }



    }

    @Override
    protected void onStart(){
        super.onStart();
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(mCurrentPosition);
            }
        });
    }

    @Override
    protected void onStop(){
        mCurrentPosition = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(0);
        super.onStop();
    }
    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(savedInstanceState.getInt(POSITION));
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SUBJECT, mSubject);
        outState.putString(CATALOG_NUMBER, mCatalogNumber);
        outState.putString(SUBTITLE, mSubtitle);
        outState.putInt(POSITION, mTabLayout.getSelectedTabPosition());
        super.onSaveInstanceState(outState);
    }


    public static class PagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String mTabTitles[] = new String[] { "Details", "Schedule" };

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
            return mTabTitles[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }

}
