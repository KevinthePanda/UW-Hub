package com.projects.kquicho.uwatm8;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.projects.kquicho.uw_api_client.Resources.InfoSession;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 4/5/2016.
 */
public class InfoSessionActivity extends AppCompatActivity{
    public static final String INFO_SESSION = "infoSession";
    public static final String IS_ALARM_SET = "isAlarmSet";
    public static final String HEADER_COLOUR = "isAlarmSet";
    private boolean mIsAlertSet;
    private boolean mIsAlertSetOriginal;
    private String mUWLink;
    private String mEmployerLink;
    private LinearLayout mAudienceContainer;
    private boolean mIsCancelled = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_session);



        Intent intent = getIntent();
        final InfoSession infoSession = intent.getParcelableExtra(INFO_SESSION);
        mIsAlertSet = intent.getBooleanExtra(IS_ALARM_SET, false);
        mIsAlertSetOriginal = mIsAlertSet;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle(infoSession.getEmployer());

        if(infoSession.isCancelled()){
            mIsCancelled = true;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            collapsingToolbar.setBackgroundColor(intent.getIntExtra(HEADER_COLOUR,
                    ContextCompat.getColor(this, R.color.theme_primary)));
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoSession.getBuildingMapUrl();
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + String.valueOf(infoSession.getLatitude()) + ","
                        + String.valueOf(infoSession.getLongitude()) + "(" + infoSession.getBuildingCode()
                        + " - " + infoSession.getBuildingRoom().replace("&", " and ") + ")");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }

            }
        });


        ((TextView)findViewById(R.id.date)).setText(infoSession.getDate());
        ((TextView)findViewById(R.id.time)).setText(infoSession.getDisplay_time_range());
        ((TextView)findViewById(R.id.location)).setText(infoSession.getBuildingCode() + " - "
                + infoSession.getBuildingRoom());

        ((ExpandableTextView)findViewById(R.id.expandable_description)).setText(infoSession.getDescription());

        String audience = infoSession.getAudience();
        audience = audience.substring(1, audience.length() -1);
        audience = audience.replace("\"","");
        String[] audienceList = audience.split(",");
        mAudienceContainer = (LinearLayout)findViewById(R.id.audience_container);
        createAudienceViews(audienceList);

        mUWLink = infoSession.getLink();
        mEmployerLink = infoSession.getWebsite();
    }

    private void createAudienceViews(String[] audienceList){
        Log.i("createAudienceViews", audienceList[0]);
        String prevDepartment = "";
        ArrayList<String> programs = new ArrayList<>();
        for(String audience : audienceList){
            String workingArr[] = audience.split(" ");
            String department = workingArr[0];
            if(workingArr.length < 2){
                continue;
            }
            String program = workingArr[2];
            for(int i = 3; i < workingArr.length; i++){
                program += " " + workingArr[i];
            }

            if(prevDepartment.equals("") || department.equals(prevDepartment)){
                programs.add(program);
            }else {
                createDepartmentView(prevDepartment, programs);
                programs.clear();
                programs.add(program);
            }

            prevDepartment = department;
        }

        if(programs.size() != 0){
            createDepartmentView(prevDepartment, programs);
        }
    }

    private void createDepartmentView(String department, ArrayList<String> programs){
        Log.i("Department", department);
        final LinearLayout expandableContainer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.expandable_department, null);
        ((TextView)expandableContainer.findViewById(R.id.department)).setText(department);
        String programList = "";
        for(String program : programs){
            Log.i("Program", program);
            if(!programList.equals("")){
                programList += "\n";
            }
            programList += program;
        }
        ((TextView)expandableContainer.findViewById(R.id.programs)).setText(programList);

        expandableContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mAudienceContainer.addView(expandableContainer);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_session_menu, menu);
        final View container  = findViewById(R.id.container);
        final MenuItem save = menu.findItem(R.id.save);
        final MenuItem uw = menu.findItem(R.id.uw);
        final MenuItem employer = menu.findItem(R.id.employer);
        final Drawable ic_star = ContextCompat.getDrawable(this, R.drawable.ic_star);
        final Drawable ic_starOutline = ContextCompat.getDrawable(this, R.drawable.ic_star_outline);
        final Drawable ic_uw = ContextCompat.getDrawable(this, R.drawable.ic_school);
        final Drawable ic_employer = ContextCompat.getDrawable(this, R.drawable.ic_web);

        if(mIsCancelled){
           save.setVisible(false);
        }else{
            if(mIsAlertSet) {
                save.setIcon(ic_star);
            }else{
                save.setIcon(ic_starOutline);
            }
            ic_star.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            ic_starOutline.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mIsAlertSet) {
                        item.setIcon(ic_starOutline);
                    } else {
                        item.setIcon(ic_star);
                    }
                    mIsAlertSet = !mIsAlertSet;
                    Intent data = new Intent();
                    data.putExtra(InfoSessionsFragment.SHOULD_TOGGLE, mIsAlertSet != mIsAlertSetOriginal);
                    setResult(RESULT_OK, data);
                    return true;
                }
            });
        }

        ic_uw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        ic_employer.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        uw.setIcon(ic_uw);
        employer.setIcon(ic_employer);


        uw.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mUWLink));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Snackbar.make(container, "Website appears to be down or no longer valid", Snackbar.LENGTH_SHORT)
                            .show();
                }
                return true;
            }
        });


        employer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mEmployerLink));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Snackbar.make(container, "Website appears to be down or no longer valid", Snackbar.LENGTH_SHORT)
                            .show();
                }
                return true;
            }
        });



        return true;
    }
}
