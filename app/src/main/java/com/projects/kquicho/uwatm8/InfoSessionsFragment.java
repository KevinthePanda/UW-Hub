package com.projects.kquicho.uwatm8;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Resources.InfoSession;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

public class InfoSessionsFragment extends Fragment implements JSONDownloader.onDownloadListener {
    final String TAG = "InfoSessionsFragment";

    private ArrayList<InfoSessionData> mData = new ArrayList<>();
    private InfoSessionAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private  ResourcesParser mParser = new ResourcesParser();


    public InfoSessionsFragment(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_sessions, parent, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        Drawable selected = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star);
        Drawable unselected = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star_outline);
        mAdapter = new InfoSessionAdapter(mData, selected, unselected, InfoSessionDBHelper.getInstance(getContext()),
                getActivity());

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

        mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
        String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());
        JSONDownloader downloader = new JSONDownloader(url);
        downloader.setOnDownloadListener(this);
        downloader.start();
    }


    @Override
    public void onDownloadFail(String givenURL, int index) {
        Log.e(TAG, "Download failed.. url = " + givenURL);
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        mParser.setAPIResult(apiResult);
        mParser.parseJSON();
        final int curSize = mAdapter.getItemCount();

        ArrayList<InfoSession> infoSessions = mParser.getInfoSessions();
        ListIterator li = infoSessions.listIterator(infoSessions.size());
        InfoSessionDBHelper dbHelper = InfoSessionDBHelper.getInstance(getActivity().getApplicationContext());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        while (li.hasPrevious()){
            InfoSession infoSession = (InfoSession)li.previous();
            Date date = null;
            try {
                date = format.parse(infoSession.getDate() + " " + infoSession.getStart_time());
            } catch (ParseException exception) {
                Log.e(TAG, "onReceive ParseException: " + exception.getMessage());
            }
            if (date == null || date.getTime() < System.currentTimeMillis()) {
                break;
            }

            boolean isAlertSet = dbHelper.checkForInfoSession(String.valueOf(infoSession.getId()));

            mData.add(new InfoSessionData(infoSession, isAlertSet));

        }
        android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemRangeChanged(curSize, mData.size());
            }
        };
        handler.post(runnable);
    }



}
