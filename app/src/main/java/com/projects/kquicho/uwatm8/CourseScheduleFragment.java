package com.projects.kquicho.uwatm8;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.NinePatchDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Course.CourseSchedule;
import com.projects.kquicho.uw_api_client.Terms.TermsParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by Kevin Quicho on 3/16/2016.
 */
public class CourseScheduleFragment extends Fragment implements JSONDownloader.onDownloadListener,
        RecyclerViewExpandableItemManager.OnGroupCollapseListener,
        RecyclerViewExpandableItemManager.OnGroupExpandListener,
        CourseScheduleAdapter.onButtonClickListener{

    private GoogleAccountCredential mCredential;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR  };

    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";
    public static final String TAG = "courseScheduleFragment";
    public static final String SUBJECT_TAG = "subject";
    public static final String CATALOG_NUMBER_TAG = "catalogNumber";
    public static final String TITLE_TAG = "title";

    private CourseScheduleData mData;
    private CourseScheduleAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewExpandableItemManager mRecyclerViewExpandableItemManager;
    private RecyclerView.Adapter mWrappedAdapter;



    private String mSubject;
    private String mCatalogNumber;
    private String mTitle;
    private TermsParser mTermsParser = new TermsParser();
    private String mTermListUrl;
    private Map<String, ArrayList<CourseSchedule>> mSchedules;
    private String mCurrentScheduleUrl;
    private String mNextScheduleUrl;
    private String mCurrentTerm;
    private String mNextTerm;
    private LinearLayout mContainer;

    public static CourseScheduleFragment newInstance(String subject, String catalogNumber, String title) {

        Bundle args = new Bundle();
        args.putString(SUBJECT_TAG, subject);
        args.putString(CATALOG_NUMBER_TAG, catalogNumber);
        args.putString(TITLE_TAG, title);
        CourseScheduleFragment fragment = new CourseScheduleFragment();
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
        mSchedules = new TreeMap<>();

        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(getActivity().getApplicationContext(),Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_schedule, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
          //  mOutputText.setText("Google Play Services required: " +
           //        "after installing, close and relaunch this app.");
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //mOutputText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
              //  new MakeRequestTask(mCredential).execute();
            } else {
               // mOutputText.setText("No network connection available.");
            }
        }
    }

    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }



    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                getActivity(),
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

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


        mTermsParser.setParseType(TermsParser.ParseType.CATALOG_SCHEDULE.ordinal());
        mTermListUrl = UWOpenDataAPI.buildURL(mTermsParser.getEndPoint("1165",mSubject, mCatalogNumber));
        mContainer = (LinearLayout)view.findViewById(R.id.container);

        JSONDownloader downloader = new JSONDownloader(mTermListUrl);
        downloader.setOnDownloadListener(this);
        downloader.start();

    }


    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    @Override
    public void onDownloadComplete(APIResult  apiResult) {
        mTermsParser.setAPIResult(apiResult);
        mTermsParser.parseJSON();
        mData = new CourseScheduleData(mTermsParser.getCourseSchedule());

        android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

        final CourseScheduleAdapter.onButtonClickListener onButtonClickListener = this;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAdapter = new CourseScheduleAdapter(mData, ContextCompat.getDrawable(getContext(), R.drawable.enrollment_status_open),
                        ContextCompat.getDrawable(getContext(), R.drawable.enrollment_status_full), onButtonClickListener);
                mWrappedAdapter = mRecyclerViewExpandableItemManager.createWrappedAdapter(mAdapter);      // wrap for expanding
                mRecyclerView.setAdapter(mWrappedAdapter);
            }
        };
        handler.post(runnable);

        Log.i(TAG, "completed ");

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
    public void onGroupExpand(int groupPosition, boolean fromUser) {
        if (fromUser) {
            adjustScrollPositionOnGroupExpanded(groupPosition);
        }
    }

    private void adjustScrollPositionOnGroupExpanded(int groupPosition) {
        int childItemHeight = 43;
        int topMargin = (int) (getActivity().getResources().getDisplayMetrics().density * 16); // top-spacing: 16dp
        int bottomMargin = topMargin; // bottom-spacing: 16dp

        mRecyclerViewExpandableItemManager.scrollToGroup(groupPosition, childItemHeight, topMargin, bottomMargin);
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onButtonClick(int pos) {
        CourseSectionData sectionData = (CourseSectionData)mData.getGroupItem(pos);
        Log.i("test", "onButtonClick");
        new MakeRequestTask(mCredential).execute(sectionData.getSection(), sectionData.getBuilding() + " - " + sectionData.getRoom(),
                sectionData.getStartTime(), sectionData.getEndTime(), sectionData.getDate(), sectionData.getWeekdays());

    }


    private class MakeRequestTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private String mLastEventId = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("UWatM8")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected String doInBackground(String... params) {
            try {
               //return getDataFromApi();
                createEvent(params[0], params[1], params[2], params[3], params[4], params[5]);
                return params[0];
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
            return eventStrings;
        }

        private void createEvent(String section, String location, String startTime, String endTime, String date, String weedays) throws IOException{
            UUID id = UUID.randomUUID();
            mLastEventId = id.toString().toLowerCase().replace("-", "");
            Event event = new Event()
                    .setSummary(mSubject + " " + mCatalogNumber + " - " + section)
                    .setLocation(location)
                    .setId(mLastEventId);

            Log.i("test", mLastEventId + "");
            if(date.equals("null")){
                date = "05/02/2016";
            }else{
                date += "/2016";
            }


            int dow = 0;
            switch (weedays.charAt(0)){
                case 'M':
                    dow = 0;
                    break;
                case 'T':
                    if(weedays.charAt(1) != 'h'){
                        dow = 1;
                    }else{
                        dow = 3;
                    }
                    break;
                case 'W':
                    dow = 2;
                    break;
                case 'F':
                    dow = 4;
                    break;

            }
            String startS = getFormattedDate(date, dow, startTime);
            String endS = getFormattedDate(date, dow, endTime);

            DateTime startDateTime = new DateTime(startS + "-04:00");
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Toronto");
            event.setStart(start);

            DateTime endDateTime = new DateTime(endS + "-04:00");
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Toronto");
            event.setEnd(end);

            String recurDays = "";
            for(int i = 0; i < weedays.length(); i++){
                char c = weedays.charAt(i);
                String day = "";
                if(c == 'T' && weedays.charAt(i +1) == 'h'){
                    day = "TH";
                    i++;
                }else{
                    switch (c){
                        case 'M':
                            day = "MO";
                            break;
                        case 'T':
                            day = "TU";
                            break;
                        case 'W':
                            day = "WE";
                            break;
                        case'F':
                            day = "FR";
                            break;
                    }
                }

                if(recurDays.equals("")){
                    recurDays = day;
                }else{
                    recurDays += "," + day;
                }
            }
            Log.i("test", recurDays);

            String[] recurrence = new String[] {"RRULE:FREQ=WEEKLY;UNTIL=20160726T170000Z;BYDAY=" + recurDays};
            event.setRecurrence(Arrays.asList(recurrence));

            EventReminder[] reminderOverrides = new EventReminder[] {
                    new EventReminder().setMethod("email").setMinutes(24 * 60),
                    new EventReminder().setMethod("popup").setMinutes(10),
            };
            Event.Reminders reminders = new Event.Reminders()
                    .setUseDefault(false)
                    .setOverrides(Arrays.asList(reminderOverrides));
            event.setReminders(reminders);

            String calendarId = mCredential.getSelectedAccountName();
            event = mService.events().insert(calendarId, event).execute();
            System.out.printf("Event created: %s\n", event.getHtmlLink());

        }

        private  String getFormattedDate(String date, int dow, String time) {
            String dtStart = date;
            Calendar calendar = new GregorianCalendar();
            calendar.clear();
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy"); // This should be MMM and not MM according to the date format 08-aug-2013
            try {
                Date dateObject = format.parse(dtStart);
                calendar.setTime(dateObject);
                String[] hoursMins = time.split(":");
                int hours = Integer.valueOf(hoursMins[0]);
                int minutes = Integer.valueOf(hoursMins[1]);
                calendar.set(Calendar.HOUR_OF_DAY, hours);
                calendar.set(Calendar.MINUTE, minutes);
                calendar.set(Calendar.SECOND, 0);
                calendar.add(Calendar.DAY_OF_MONTH, + dow);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return outputFormat.format(calendar.getTime());
        }

        @Override
        protected void onPreExecute() {
            //mOutputText.setText("");
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(final String output) {
            Log.i("test", "onPostExecute");
            Snackbar.make(mRecyclerView, output +" added to Google Calendar", Snackbar.LENGTH_LONG)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            undoCreateEvent(output);
                        }
                    })
                    .show();

        }

        private void undoCreateEvent(final String section){
            new AsyncTask<String, Void, String>(){

                @Override
                protected String doInBackground(String... params) {
                    try {
                        mService.events().delete(mCredential.getSelectedAccountName(), mLastEventId).execute();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String output){
                    Snackbar.make(mRecyclerView, section + " removed from Google Calendar", Snackbar.LENGTH_LONG).show();
                }
            }.execute(mLastEventId);
        }
        @Override
        protected void onCancelled() {
            Log.i("test", mLastError.getLocalizedMessage());

            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CourseScheduleFragment.REQUEST_AUTHORIZATION);
                } else {
                }
            } else {
            }
        }
    }
}
