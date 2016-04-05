package com.projects.kquicho.uwatm8;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class HomeFragment extends Fragment implements  UWClientResponseHandler{
    private final String LOGCAT_TAG = "HomeFragment";
    private final static ArrayList<String> mAvailableWidgets =
            new ArrayList<>(Arrays.asList(WeatherWidget.TAG, InfoSessionWidget.TAG));

    private ArrayList<UWData> mData = new ArrayList<>();
    private UWParserAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private SharedPreferences mSettings;

    public HomeFragment(){
        super();
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        mSettings = getActivity().getSharedPreferences("Settings", 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        Fab fab = (Fab) view.findViewById(R.id.fab);
        View sheetView = view.findViewById(R.id.fab_sheet);
        View overlay = view.findViewById(R.id.dim_overlay);
        int sheetColor = getResources().getColor(R.color.background_fab_card);
        int fabColor = getResources().getColor(R.color.theme_primary);

        // Initialize material sheet FAB
        MaterialSheetFab materialSheetFab  = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);

        mRecyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        //drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z3));

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        mAdapter = new UWParserAdapter(mData, getContext());

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);      // wrap for dragging
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();
        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);

        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h), true));

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);

        int size = mAvailableWidgets.size();
        for(int i = 0; i < size; i++){
            String widget = mSettings.getString(String.valueOf(i), null);
            if(widget == null){
                Log.i("Test", "null");
                if(i == 0){
                    Log.i("Test", "0");
                    WeatherWidget.getInstance(this, 0);
                    InfoSessionWidget.getInstance(this, 1, getActivity().getApplicationContext());
                }
                break;
            }
            switch (widget){
                case WeatherWidget.TAG:
                    Log.i("Test", "WeatherWidget");
                    WeatherWidget.getInstance(this, i);
                    break;
                case InfoSessionWidget.TAG:
                    Log.i("Test", "InfoSessionWidget");
                    InfoSessionWidget.getInstance(this, getActivity().getApplicationContext());
                    break;
            }
        }

        final UWClientResponseHandler handler = this;
        view.findViewById(R.id.fab_sheet_weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeatherWidget.getInstance(handler);
            }
        });

        view.findViewById(R.id.fab_sheet_info_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InfoSessionWidget.getInstance(handler, getActivity().getApplicationContext());
            }
        });
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        int index = 0;
        SharedPreferences.Editor editor = mSettings.edit();
        int availableSize = mAvailableWidgets.size();
        int size = mData.size();
        for(;index < size; index++){
            editor.putString(String.valueOf(index), mData.get(index).getWidgetTag());
        }
        for(;index < availableSize; index++){
            editor.putString(String.valueOf(index), null);
        }
        editor.commit();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
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
        mAdapter = null;
        mLayoutManager = null;

        WeatherWidget.destroyWidget();
        InfoSessionWidget.destroyWidget();

        super.onDestroyView();
    }



    @Override
    public void onSuccess(UWData data, Integer position) {
        Log.i(LOGCAT_TAG, "onSuccess");
        final int dataPosition = (position == null || position > mData.size() ) ? mData.size() : position;
        mData.add(dataPosition, data);
        android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemInserted(dataPosition);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onError(String error){
        Log.e(LOGCAT_TAG, error);
    }


    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

}
