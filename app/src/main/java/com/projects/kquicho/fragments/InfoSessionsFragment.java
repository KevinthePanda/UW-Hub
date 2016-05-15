package com.projects.kquicho.fragments;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.projects.kquicho.activities.InfoSessionActivity;
import com.projects.kquicho.database.InfoSessionDBHelper;
import com.projects.kquicho.models.InfoSessionDBModel;
import com.projects.kquicho.models.InfoSessionData;
import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.JSONDownloader;
import com.projects.kquicho.network.Core.UWOpenDataAPI;
import com.projects.kquicho.network.Resources.InfoSession;
import com.projects.kquicho.network.Resources.ResourcesParser;
import com.projects.kquicho.utils.InfoSessionAlarmReceiver;
import com.projects.kquicho.activities.MainActivity;
import com.projects.kquicho.uwhub.R;
import com.projects.kquicho.views.adapters.BottomSheetInfoSessionAdapter;
import com.projects.kquicho.views.adapters.InfoSessionAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;

public class InfoSessionsFragment extends Fragment implements JSONDownloader.onDownloadListener,
        InfoSessionAdapter.onInfoSessionClickListener,
        BottomSheetInfoSessionAdapter.onBottomSheetInfoSessionClickListener,
        MainActivity.FragmentOnBackClickInterface {
    public static final String TAG = "InfoSessionsFragment";
    public static final String POSITION = "position";
    private final String DATA = "data";
    private final String IS_SHOWING_ALL = "isShowingAll";
    private final String CURRENT_INFO_SESSION_POSITION = "currentInfoSessionPosition";
    private final int ALERT_CHANGE_REQUEST = 1;
    public final int DIALOG_FRAGMENT = 2;
    public static final String SHOULD_TOGGLE = "shouldToggle";

    private ArrayList<InfoSessionData> mData = new ArrayList<>();
    private ArrayList<InfoSessionData> mOriginalData = new ArrayList<>();
    private ArrayList<Pair<Integer,InfoSession>> mBottomSheetData = new ArrayList<>();
    private InfoSessionAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView mBottomSheetRV;
    private SnappingLinearLayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private ResourcesParser mParser = new ResourcesParser();
    private View mEmptyView;
    private FloatingActionButton mFab;
    private ProgressBar mProgressBar;
    private int mCurrentInfoSessionPosition;
    private BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetInfoSessionAdapter mBottomSheetAdapter;
    private TextView mBottomSheetEmployer;
    private RelativeLayout mBottomSheetViewGroup;
    public InfoSessionsFragment(){
        super();
    }
    private View mDimOverlay;
    private SearchView mSearchView;
    private boolean mSavedShowingAll = true;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_sessions, parent, false);

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelableArrayList(DATA);
            mOriginalData.clear();
            mOriginalData.addAll(mData);
            mSavedShowingAll = savedInstanceState.getBoolean(IS_SHOWING_ALL);
            mCurrentInfoSessionPosition = savedInstanceState.getInt(CURRENT_INFO_SESSION_POSITION);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        final Window window = getActivity().getWindow();

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView = searchView;
        searchView.setQueryHint(getString(R.string.search_employers));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                MainActivity activity = (MainActivity) getActivity();
                if (!queryTextFocused) {
                    Log.i(TAG, "onFocusChange - searchView lost focus");

                    activity.animateMenuArrowDrawable(false);
                    activity.unlockNavDrawer();

                    if (searchView.getQuery().toString().trim().length() == 0) {
                        searchItem.collapseActionView();
                        searchView.setIconified(true);
                    }
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

                    if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.toUpperCase().trim();
                mData.clear();
                if (mOriginalData == null) {
                    return false;
                } else if (query.equals("")) {
                    mData.addAll(mOriginalData);
                } else {
                    for (int i = 0; i < mOriginalData.size(); i++) {
                        InfoSessionData infoSession = mOriginalData.get(i);
                        String employer = infoSession.getInfoSession().getEmployer().toUpperCase();
                        if (employer.contains(query)) {
                            mData.add(infoSession);
                        }
                    }
                }
                searchView.clearFocus();
                mDimOverlay.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.theme_primary_dark));
                }
                actionBar.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.theme_primary));
                mAdapter.notifyDataSetChanged();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toUpperCase().trim();
                mData.clear();
                if (mOriginalData == null) {
                    return false;
                } else if (newText.equals("")) {
                    mData.addAll(mOriginalData);
                } else {
                    for (int i = 0; i < mOriginalData.size(); i++) {
                        InfoSessionData infoSession = mOriginalData.get(i);
                        String employer = infoSession.getInfoSession().getEmployer().toUpperCase();
                        if (employer.contains(newText)) {
                            mData.add(infoSession);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);

    }

        @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = (ProgressBar)view.findViewById(R.id.pbLoading);
        mDimOverlay = view.findViewById(R.id.dim_overlay);
        mEmptyView = view.findViewById(R.id.empty_view);
        mRecyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new SnappingLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        Drawable selected = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star);
        Drawable unselected = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star_outline);
        mAdapter = new InfoSessionAdapter(mData, selected, unselected, InfoSessionDBHelper.getInstance(getContext()),
                getActivity(), this);

        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);

        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);

        if(mData == null || mData.size() == 0) {
            mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
            String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());
            mProgressBar.setVisibility(View.VISIBLE);
            JSONDownloader downloader = new JSONDownloader(getActivity(), url);
            downloader.setOnDownloadListener(this);
            downloader.start();
        }


        mFab = (FloatingActionButton)view.findViewById(R.id.fab);


        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mAdapter.toggleShowSaved()){
                    if(mData.size() == 0){
                        ((TextView)mEmptyView).setText(getString(R.string.no_saved_info_sessions));
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEmptyView.setVisibility(View.VISIBLE);
                            }
                        }, 300);

                    }
                }else{
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        });

        if(!mSavedShowingAll){
            mFab.setVisibility(View.VISIBLE);
            mFab.performClick();
        }

        mBottomSheetViewGroup = (RelativeLayout) view.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetViewGroup);
        mBottomSheetRV = (RecyclerView)view.findViewById(R.id.bottom_recycler_view);
        mBottomSheetEmployer = (TextView)view.findViewById(R.id.company);
        mBottomSheetAdapter = new BottomSheetInfoSessionAdapter(mBottomSheetData, this);

        mBottomSheetRV.setAdapter(mBottomSheetAdapter);
        mBottomSheetRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mBottomSheetRV.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));
        mDimOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.clearFocus();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(DATA, mOriginalData);
        outState.putBoolean(IS_SHOWING_ALL, mAdapter.getIsShowingAll());
        outState.putInt(CURRENT_INFO_SESSION_POSITION, mCurrentInfoSessionPosition);
        super.onSaveInstanceState(outState);
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
        mParser.setAPIResult(apiResult);
        mParser.parseJSON();
        ArrayList<InfoSession> infoSessions = mParser.getInfoSessions();
        ListIterator li = infoSessions.listIterator(infoSessions.size());
        InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(activity.getApplicationContext());
        DateFormat format = new SimpleDateFormat("MMM d, yy HH:mm", Locale.CANADA);
        while (li.hasPrevious()){
            InfoSession infoSession = (InfoSession)li.previous();
            Date date = null;
            try {
                date = format.parse(infoSession.getDate() + " " + infoSession.getStart_time());
            } catch (ParseException exception) {
                Log.e(TAG, "onReceive ParseException: " + exception.getMessage());
            }
            if (date == null ||  date.getTime() < System.currentTimeMillis()) {
                break;
            }

            boolean isAlertSet = dbHelper.checkForInfoSession(String.valueOf(infoSession.getId()));

            mData.add(new InfoSessionData(infoSession, isAlertSet, date.getTime()));

        }
        Collections.sort(mData, new CustonComparator());
        mOriginalData.addAll(mData);
        android.os.Handler handler = new android.os.Handler(activity.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                if(mData == null || mData.size() == 0){
                    mEmptyView.setVisibility(View.VISIBLE);
                    mFab.setVisibility(View.GONE);
                }else {
                    mEmptyView.setVisibility(View.GONE);
                    mFab.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ALERT_CHANGE_REQUEST && data != null && data.getBooleanExtra(SHOULD_TOGGLE, false)) {
            final int position = data.getIntExtra(POSITION, 0);
            if (resultCode == Activity.RESULT_OK) {
                Log.d("test" , "test " + position);
                mData.get(position).toggleAlert();
                mAdapter.notifyItemChanged(position);
            }else{
                Log.d("test" , "test1");
                InfoSessionData infoSessionData = mData.get(position);
                if(!mAdapter.getIsShowingAll()){
                    mData.remove(infoSessionData);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, mData.size());
                }

                infoSessionData.setPinned(false);
                infoSessionData.toggleAlert();
                mAdapter.notifyItemChanged(position);
            }
        } else if (requestCode == DIALOG_FRAGMENT) {
            Log.d("test" , "test3");
            if (resultCode == Activity.RESULT_OK) {

                Snackbar.make(mRecyclerView, data.getStringExtra(SaveInfoSessionDialogFragment.MESSAGE), Snackbar.LENGTH_LONG).show();
                mAdapter.notifyItemChanged(data.getIntExtra(POSITION, 0));
            }
        }
    }

    @Override
    public void onInfoSessionClick(final InfoSessionData infoSessionData, final int position, int type) {
        mCurrentInfoSessionPosition = position;

        switch (type) {
            case InfoSessionAdapter.INFO_SESSION_CLICK:
                Intent intent = new Intent(getActivity(), InfoSessionActivity.class);
                intent.putExtra(InfoSessionActivity.INFO_SESSION, infoSessionData.getInfoSession());
                intent.putExtra(InfoSessionActivity.IS_ALARM_SET, infoSessionData.isAlertSet());
                intent.putExtra(InfoSessionActivity.INFO_SESSION_DATA, infoSessionData);
                intent.putExtra(InfoSessionActivity.POSITION, position);
                startActivityForResult(intent, ALERT_CHANGE_REQUEST);
                break;
            case InfoSessionAdapter.ROUNDED_TEXT_VIEW_CLICK:
                mBottomSheetData.clear();
                int i = 0;
                for(InfoSessionData data : mData){
                    if(data.getInfoSession().getEmployer().equals(infoSessionData.getInfoSession().getEmployer())){
                        Pair<Integer, InfoSession> pair = new Pair<>(i, data.getInfoSession());
                        mBottomSheetData.add(pair);
                    }
                    i++;
                }
                mBottomSheetAdapter.notifyDataSetChanged();
                mBottomSheetEmployer.setText(infoSessionData.getInfoSession().getEmployer());
                mBottomSheetRV.post(new Runnable() {
                    @Override
                    public void run() {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        mBottomSheetViewGroup.post(new Runnable() {
                            @Override
                            public void run() {
                                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        });
                    }
                });

                break;
            case InfoSessionAdapter.SAVE_CLICK:

                if (infoSessionData.shouldAttemptToggleAlert()) {
                    final CharSequence[] items =
                            {"At time of event", "10 minutes before", "30 minutes before"};

                    android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment dialogFrag = SaveInfoSessionDialogFragment.newInstance(items, infoSessionData, position);
                    dialogFrag.setTargetFragment(this, DIALOG_FRAGMENT);
                    dialogFrag.show(ft, "dialog");

                } else {
                    InfoSessionDBModel infoSessionDBModel = new InfoSessionDBModel();
                    int id = infoSessionData.getInfoSession().getId();
                    infoSessionDBModel.setId(id);
                    InfoSessionDBHelper.getInstance(getActivity().getApplicationContext())
                            .deleteInfoSession(infoSessionDBModel);

                    Intent serviceIntent = new Intent();
                    serviceIntent.putExtra(InfoSessionAlarmReceiver.INFO_SESSION_MODEL, infoSessionDBModel);
                    final PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), id,
                            serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pIntent);
                    if(!mAdapter.getIsShowingAll()){
                        mData.remove(infoSessionData);
                        mAdapter.notifyItemRemoved(position);
                        mAdapter.notifyItemRangeChanged(position, mData.size());
                    }
                    Snackbar.make(mRecyclerView, getString(R.string.info_session_removed),Snackbar.LENGTH_LONG)
                            .show();
                    infoSessionData.setPinned(false);
                    infoSessionData.toggleAlert();
                    mAdapter.notifyItemChanged(position);

                }

                break;

        }

    }

    @Override
    public void onBottomSheetInfoSessionClick(int position) {
        Log.i(TAG, "onBottomSheetInfoSessionClick");
        mRecyclerView.smoothScrollToPosition(position);

    }

    public class CustonComparator implements Comparator<InfoSessionData>{
        @Override
        public int compare(InfoSessionData o1, InfoSessionData o2) {
            return  o1.getTime()<o2.getTime()?-1:
                    o1.getTime()>o2.getTime()?1:0;
        }

    }

    public class SnappingLinearLayoutManager extends LinearLayoutManager {

        public SnappingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                           int position) {
            RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext());
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class TopSnappedSmoothScroller extends LinearSmoothScroller {
            public TopSnappedSmoothScroller(Context context) {
                super(context);

            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return SnappingLinearLayoutManager.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        }
    }

    @Override
    public void onFragmentBackPressed() {
        Log.i(TAG, "onFragmentBackPressed");
        if(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else if(!mSearchView.isIconified()){
            mSearchView.setQuery(null, false);
            mSearchView.setIconified(true);
        }else{
            MainActivity activity = (MainActivity)getActivity();
            activity.navigateToHome();
        }
    }

}

