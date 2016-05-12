package com.projects.kquicho.uwhub;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    final public static String BASE_FRAGMENT = "baseFragment";
    final public String TITLE = "title";
    final public String SUBTITLE = "subtitle";
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavDrawer;
    private MenuArrowDrawable mMenuArrowDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Our drawer layout root
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mMenuArrowDrawable = new MenuArrowDrawable(this);
        mToolbar.setNavigationIcon(mMenuArrowDrawable);

        //Find our drawer view
        mNavDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(mNavDrawer);

        if(savedInstanceState == null) {
            mNavDrawer.getMenu().performIdentifierAction(R.id.nav_home, 0);
        }else{
            android.support.v7.app.ActionBar actionBar =  getSupportActionBar();
            if(actionBar != null){
                actionBar.setTitle(savedInstanceState.getString(TITLE));
                actionBar.setSubtitle(savedInstanceState.getString(SUBTITLE));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        android.support.v7.app.ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null) {
            savedInstanceState.putString(TITLE, actionBar.getTitle() != null ?
                    actionBar.getTitle().toString() : null);
            savedInstanceState.putString(SUBTITLE, actionBar.getSubtitle() != null ?
                    actionBar.getSubtitle().toString(): null);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void animateMenuArrowDrawable(boolean menuToArrow){
        mMenuArrowDrawable.animateDrawable(menuToArrow);
    }

    public void setMenuArrowDrawable(boolean menuToArrow){
        float progress = menuToArrow ? 1: 0;
        mMenuArrowDrawable.setProgress(progress);
    }

    public void lockNavDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    public void unlockNavDrawer(){
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on position

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragmentClass = HomeFragment.class;
                break;
            case R.id.nav_info_sessions:
                fragmentClass = InfoSessionsFragment.class;
                break;
            case R.id.nav_courses_fragment:
                fragmentClass = GroupSubjectFragment.class;
                break;
            default:
                fragmentClass = HomeFragment.class;
        }

        try {
            if(fragment != null && fragmentClass == fragment.getClass()){
                return;
            }
            fragment = (Fragment) fragmentClass.newInstance();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, BASE_FRAGMENT).commit();
            fragmentManager.popBackStackImmediate();
            // Highlight the selected item, update the title, and close the drawer
            menuItem.setChecked(true);
            getSupportActionBar().setTitle(menuItem.getTitle());
            getSupportActionBar().setSubtitle(null);
            mDrawerLayout.closeDrawers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(mMenuArrowDrawable.getPosition() == 0) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }else{
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Log.i(TAG, "onBackPressed");
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Fragment currentFragment;

        if(count > 0) {
            String fragmentTag = fragmentManager.getBackStackEntryAt(count - 1).getName();
            currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }else{
            currentFragment = fragmentManager.findFragmentByTag(BASE_FRAGMENT);
            if(currentFragment instanceof HomeFragment){
                currentFragment = null;
            }
        }

        if (currentFragment == null || !(currentFragment instanceof FragmentOnBackClickInterface)) {
            super.onBackPressed();
        } else {
            ((FragmentOnBackClickInterface) currentFragment).onFragmentBackPressed();
        }

        //nothing else in back stack || nothing in back stack is instance of our interface
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



    public interface FragmentOnBackClickInterface {
        void onFragmentBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void navigateToHome(){
        selectDrawerItem(mNavDrawer.getMenu().getItem(0));
    }

}
