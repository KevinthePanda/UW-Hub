package com.projects.kquicho.uwatm8;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

/**
 * Created by Kevin Quicho on 4/5/2016.
 */
public class InfoSessionActivity extends AppCompatActivity{
    public static final String INFO_SESSION = "infoSession";
    public static final String TIME = "time";
    public static final String IS_ALARM_SET = "isAlarmSet";
    private boolean mIsAlertSet;
    private boolean mIsAlertSetOriginal;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_session);

        Intent intent = getIntent();
        final InfoSession infoSession = intent.getParcelableExtra(INFO_SESSION);
        final long time = intent.getLongExtra(TIME, 0);
        mIsAlertSet = intent.getBooleanExtra(IS_ALARM_SET, false);
        mIsAlertSetOriginal = mIsAlertSet;

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(infoSession.getEmployer());

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

        ((TextView)findViewById(R.id.description)).setText(infoSession.getDescription());

        String audience = infoSession.getAudience();
        audience = audience.substring(1, audience.length() -1);
        audience = audience.replace("\"","");
        String[] audienceArr = audience.split(",");
        audience = "";
        for(String singleAudience : audienceArr){
            if(!audience.equals("")){
                audience = audience + "\n";
            }
            audience = audience + singleAudience;
        }

        ((TextView)findViewById(R.id.audience)).setText(audience);

        findViewById(R.id.employer_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(infoSession.getWebsite()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }else{

                }

            }
        });

        findViewById(R.id.ceca_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(infoSession.getWebsite()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }else{

                }
            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_session_menu, menu);
        final MenuItem save = menu.findItem(R.id.save);
        final Drawable star = ContextCompat.getDrawable(this, R.drawable.ic_star);
        final Drawable starOutline = ContextCompat.getDrawable(this, R.drawable.ic_star_outline);
        star.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        starOutline.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP  );
        if(mIsAlertSet) {
            save.setIcon(star);
        }else{
            save.setIcon(starOutline);
        }

        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(mIsAlertSet) {
                    item.setIcon(starOutline);
                }else{
                    item.setIcon(star);
                }
                mIsAlertSet = !mIsAlertSet;
                Intent data = new Intent();
                data.putExtra(InfoSessionsFragment.SHOULD_TOGGLE, mIsAlertSet != mIsAlertSetOriginal);
                setResult(RESULT_OK, data);
                return true;
            }
        });


        return true;
    }
}
