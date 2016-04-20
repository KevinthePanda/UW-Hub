package com.projects.kquicho.uwatm8;

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
    public static final String SUBJECT_TAG = "subject";
    public static final String CATALOG_NUMBER_TAG = "catalogNumber";
    public static final String SUBTITLE_TAG = "subtitle";
    private static String mSubject;
    private static String mCatalogNumber;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_tabs);

        Intent intent = getIntent();
        mSubject = intent.getStringExtra(SUBJECT_TAG);
        mCatalogNumber = intent.getStringExtra(CATALOG_NUMBER_TAG);
        String subtitle = intent.getStringExtra(SUBTITLE_TAG);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mSubject + " " + mCatalogNumber);
            actionBar.setSubtitle(subtitle);
        }


        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
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
