package com.projects.kquicho.uwatm8;

import android.Manifest;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";
    final private int PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;
    final private int PERMISSIONS_REQUEST_WRITE_CALENDAR = 1;
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

        mNavDrawer.getMenu().performIdentifierAction(R.id.nav_home, 0);
    }


    public void animateMenuArrowDrawable(boolean menuToArrow){
        mMenuArrowDrawable.animateDrawable(menuToArrow);
    }

    public void setMenuArrowDrawable(boolean menuToArrow){
        float progress = menuToArrow ? 1: 0;
        mMenuArrowDrawable.setProgress(progress);
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
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);;

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
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

            // Highlight the selected item, update the title, and close the drawer
            menuItem.setChecked(true);
            getSupportActionBar().setTitle(menuItem.getTitle());
            getSupportActionBar().setSubtitle(null);
            Log.i("test", menuItem.getTitle() + "");
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        Fragment currentFragment = null;
        if(count > 0) {
            String fragmentTag = fragmentManager.getBackStackEntryAt(count - 1).getName();
            currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        }

        //nothing else in back stack || nothing in back stack is instance of our interface
        if (currentFragment == null || !(currentFragment instanceof FragmentOnBackClickInterface)) {
            Log.i("test", "Wtf");
            super.onBackPressed();
        } else {
            ((FragmentOnBackClickInterface) currentFragment).onFragmentBackPressed();
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {


                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        PERMISSIONS_REQUEST_GET_ACCOUNTS);

        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {


            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_CALENDAR},
                    PERMISSIONS_REQUEST_WRITE_CALENDAR);

        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case PERMISSIONS_REQUEST_WRITE_CALENDAR:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;


        }
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

}
