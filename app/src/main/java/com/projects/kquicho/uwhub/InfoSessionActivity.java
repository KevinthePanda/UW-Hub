package com.projects.kquicho.uwhub;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Resources.InfoSession;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 4/5/2016.
 */
public class InfoSessionActivity extends AppCompatActivity implements JSONDownloader.onDownloadListener{
    public static final String TAG = "InfoSessionActivity";
    public static final String INFO_SESSION = "infoSession";
    public static final String IS_ALARM_SET = "isAlarmSet";
    public static final String IS_ALARM_SET_ORIGINAL = "isAlarmSetOriginal";
    public static final String INFO_SESSION_ID = "infoSessionId";
    private boolean mIsAlertSet = true;
    private Boolean mIsAlertSetOriginal;
    private InfoSession mInfoSession;
    private String mUWLink;
    private String mEmployerLink;
    private LinearLayout mAudienceContainer;
    private boolean mIsCancelled = false;
    private ResourcesParser mParser = new ResourcesParser();
    private int mId = -1;
    private ProgressBar mProgressBar;
    private View mContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_session);

        mProgressBar = (ProgressBar)findViewById(R.id.pbLoading);
        mContainer = findViewById(R.id.container);
        mProgressBar.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);

        if(savedInstanceState == null) {
            Intent intent = getIntent();
            mId = intent.getIntExtra(INFO_SESSION_ID, -1);
            if (mId == -1) {
                mInfoSession = intent.getParcelableExtra(INFO_SESSION);
                init(mInfoSession, intent.getBooleanExtra(IS_ALARM_SET, false));
            } else {
                mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
                String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());
                JSONDownloader downloader = new JSONDownloader(url);
                downloader.setOnDownloadListener(this);
                downloader.start();
            }
        }else{
            mInfoSession = savedInstanceState.getParcelable(INFO_SESSION);
            mIsAlertSet = savedInstanceState.getBoolean(IS_ALARM_SET);
            mIsAlertSetOriginal = savedInstanceState.getBoolean(IS_ALARM_SET_ORIGINAL);

            Intent data = new Intent();
            data.putExtra(InfoSessionsFragment.SHOULD_TOGGLE, mIsAlertSet != mIsAlertSetOriginal);
            setResult(RESULT_OK, data);

            init(mInfoSession, mIsAlertSet);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_session_menu, menu);
        final View container  = findViewById(R.id.container);
        final MenuItem save = menu.findItem(R.id.save);
        final MenuItem uw = menu.findItem(R.id.uw);
        final MenuItem employer = menu.findItem(R.id.employer);
        final Drawable ic_starOutline = getResizedDrawable(save.getIcon());
        final Drawable ic_star = getResizedDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star));
        final Drawable ic_uw = getResizedDrawable(uw.getIcon());
        final Drawable ic_employer = getResizedDrawable(employer.getIcon());

        if(mIsCancelled){
            save.setVisible(false);
        }else{
            if(mIsAlertSet) {
                save.setIcon(ic_star);
            }else{
                save.setIcon(ic_starOutline);
            }
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

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(INFO_SESSION, mInfoSession);
        savedInstanceState.putBoolean(IS_ALARM_SET, mIsAlertSet);
        savedInstanceState.putBoolean(IS_ALARM_SET_ORIGINAL, mIsAlertSetOriginal);
        super.onSaveInstanceState(savedInstanceState);
    }


    private void init(final InfoSession infoSession, boolean isAlertSet){
        mIsAlertSet = isAlertSet;
        if(mIsAlertSetOriginal == null) {
            mIsAlertSetOriginal = mIsAlertSet;
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle(infoSession.getEmployer());

        if(infoSession.isCancelled()){
            mIsCancelled = true;
            findViewById(R.id.session_container).setVisibility(View.GONE);
            findViewById(R.id.description_card_view).setVisibility(View.GONE);
            findViewById(R.id.audience_card_view).setVisibility(View.GONE);
            findViewById(R.id.cancelled).setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            collapsingToolbar.setBackgroundColor(generator.getColor(infoSession.getEmployer()));
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }




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
        mProgressBar.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
    }


    private void createAudienceViews(String[] audienceList){
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
        final LinearLayout expandableContainer = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.expandable_department, null);
        ((TextView)expandableContainer.findViewById(R.id.department)).setText(department);
        String programList = "";
        for(String program : programs){
            if(!programList.equals("")){
                programList += "\n";
            }
            programList += program;
        }
        ((TextView)expandableContainer.findViewById(R.id.programs)).setText(programList);

        expandableContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mAudienceContainer.addView(expandableContainer);

    }

    private Drawable getResizedDrawable(Drawable drawable){
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        return new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap,
                (int)(getResources().getDisplayMetrics().density * 24),
                (int)(getResources().getDisplayMetrics().density * 24), true));
    }


    @Override
    public void onDownloadComplete(@NonNull APIResult apiResult) {
        mParser.setAPIResult(apiResult);
        final InfoSession infoSession = mParser.getSingleInfoSession(mId);
        android.os.Handler handler = new android.os.Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(infoSession != null) {
                    init(infoSession, true);
                }else{
                    ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle("Could not load");
                }
            }
        });


    }

    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }
}
