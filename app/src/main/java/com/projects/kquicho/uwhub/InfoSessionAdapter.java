package com.projects.kquicho.uwhub;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.projects.kquicho.uw_api_client.Resources.InfoSession;
import java.util.ArrayList;
import java.util.Iterator;

public class InfoSessionAdapter extends RecyclerView.Adapter<InfoSessionAdapter.InfoSessionHolder>
        implements SwipeableItemAdapter<InfoSessionAdapter.InfoSessionHolder>{

    private final String TAG = "InfoSessionAdapter";
    public static final int INFO_SESSION_CLICK = 0;
    public static final int ROUNDED_TEXT_VIEW_CLICK = 1;
    public static final int SAVE_CLICK = 2;
    private ArrayList<InfoSessionData> mData;
    private Drawable mSelectedDrawable;
    private Drawable mUnselectedDrawable;
    private final InfoSessionDBHelper mDBHelper;
    private final Activity mActivity;
    private static onInfoSessionClickListener mInfoSessionClickListener;
    private boolean mIsShowingAll = true;
    ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    ArrayList<InfoSessionData> mDataFabCopy = new ArrayList<>();

    private interface Swipeable extends SwipeableItemConstants {
    }

    public InfoSessionAdapter(ArrayList<InfoSessionData> data, Drawable selected, Drawable unselected,
                              InfoSessionDBHelper dbHelper, Activity activity, onInfoSessionClickListener onInfoSessionClickListener){
        mData = data;
        mSelectedDrawable = selected;
        mUnselectedDrawable = unselected;
        mDBHelper = dbHelper;
        mActivity = activity;
        mInfoSessionClickListener = onInfoSessionClickListener;
        setHasStableIds(true);
    }

    public class InfoSessionHolder extends AbstractSwipeableItemViewHolder {
        public View mContainer;
        public ImageView mSaveBtn;
        public ImageView mSaveIcon;
        public TextView mCompany;
        public TextView mDate;
        public TextView mTime;
        public TextView mLocation;
        public ImageView mRoundedLetter;

        public InfoSessionHolder(View itemView){
            super(itemView);
            mContainer = itemView.findViewById(R.id.container);
            mSaveBtn = (ImageView)itemView.findViewById(R.id.save_button);
            mSaveIcon = (ImageView)itemView.findViewById(R.id.saved_image);
            mCompany = (TextView)itemView.findViewById(R.id.company);
            mDate = (TextView)itemView.findViewById(R.id.date);
            mTime = (TextView)itemView.findViewById(R.id.time);
            mLocation = (TextView)itemView.findViewById(R.id.location);
            mRoundedLetter = (ImageView)itemView.findViewById(R.id.rounded_letter);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }

    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getInfoSession().getId();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public InfoSessionHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_session_row, parent, false);
        return new InfoSessionHolder(view);
    }

    public void toggleSetAlert(int position){
        final InfoSessionData data = mData.get(position);
        mInfoSessionClickListener.onInfoSessionClick(data, position, SAVE_CLICK);
    }

    @Override
    public void onBindViewHolder(final InfoSessionHolder viewHolder, final int position){
        final InfoSessionData data = mData.get(position);
        InfoSession infoSession = data.getInfoSession();
        if(infoSession.isCancelled()){
            viewHolder.mCompany.setPaintFlags(viewHolder.mCompany.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.setMaxLeftSwipeAmount(0);
            viewHolder.setMaxRightSwipeAmount(0);
            viewHolder.setSwipeItemHorizontalSlideAmount(0);
        }else{
            viewHolder.mCompany.setPaintFlags( viewHolder.mCompany.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.setMaxLeftSwipeAmount(-0.17f);
            viewHolder.setMaxRightSwipeAmount(0);
            viewHolder.setSwipeItemHorizontalSlideAmount(data.isPinned() ? -0.17f : 0);
        }
        viewHolder.mCompany.setText(infoSession.getEmployer());
        viewHolder.mDate.setText(infoSession.getDate());
        viewHolder.mTime.setText(infoSession.getDisplay_time_range());
        viewHolder.mLocation.setText(infoSession.getBuildingCode() + " - " + infoSession.getBuildingRoom());

        if(data.isAlertSet()){
            viewHolder.mSaveIcon.setImageDrawable(mSelectedDrawable);
            viewHolder.mSaveBtn.setImageDrawable(mSelectedDrawable);
        }else{
            viewHolder.mSaveIcon.setImageDrawable(null);
            viewHolder.mSaveBtn.setImageDrawable(mUnselectedDrawable);
        }

        int color = mColorGenerator.getColor(infoSession.getEmployer());



        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.SANS_SERIF)
                .toUpperCase()
                .endConfig()
                .buildRound(String.valueOf(infoSession.getEmployer().charAt(0)), color); // radius in px

        viewHolder.mRoundedLetter.setImageDrawable(drawable);

        viewHolder.mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSetAlert(position);
            }
        });
        viewHolder.mRoundedLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(data.isPinned()) {
                    data.setPinned(false);
                    notifyItemChanged(position);
                }else{
                    mInfoSessionClickListener.onInfoSessionClick(data, position, ROUNDED_TEXT_VIEW_CLICK);
                }
            }
        });

        viewHolder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.isPinned()) {
                    data.setPinned(false);
                    notifyItemChanged(position);
                } else {
                    mInfoSessionClickListener.onInfoSessionClick(data, position, INFO_SESSION_CLICK);
                }
            }
        });

    }


    @Override
    public int onGetSwipeReactionType(InfoSessionHolder viewHolder, int position, int x, int y) {
        if (ViewUtils.hitTest(viewHolder.getSwipeableContainerView(), x, y)) {
            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        } else {
            return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;
        }
    }

    @Override
    public void onSetSwipeBackground(InfoSessionHolder viewHolder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }

        viewHolder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(InfoSessionHolder viewHolder, int position, int result) {
        Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        switch (result) {
            // swipe left --- pin
            case Swipeable.RESULT_SWIPED_LEFT:
                return new SwipeLeftResultAction(this, position);
            // other --- do nothing
            case Swipeable.RESULT_SWIPED_RIGHT:
            case Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }

        }
    }


    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private InfoSessionAdapter mAdapter;
        private final int mPosition;

        SwipeLeftResultAction(InfoSessionAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            InfoSessionData item = mAdapter.mData.get(mPosition);

            if (!item.isPinned()) {
                item.setPinned(true);
                mAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private static class UnpinResultAction extends SwipeResultActionDefault {
        private InfoSessionAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(InfoSessionAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            InfoSessionData item = mAdapter.mData.get(mPosition);
            if (item.isPinned()) {
                item.setPinned(false);
                mAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }


    public boolean showOnlyFavourites() {
        boolean existsAlarmSet = false;
        mDataFabCopy.addAll(mData);
        for(Iterator<InfoSessionData> iterator = mData.iterator(); iterator.hasNext();){
            if (!iterator.next().isAlertSet()) {
                iterator.remove();
                existsAlarmSet = true;
            }
        }
        if(!existsAlarmSet){
            mDataFabCopy.clear();
            return false;
        }
        notifyDataSetChanged();
        return true;
    }

    public boolean showAll() {
        if(mDataFabCopy.size() == 0){
            return false;
        }
        mData.clear();
        mData.addAll(mDataFabCopy);
        mDataFabCopy.clear();
        notifyDataSetChanged();
        return true;
    }

    public boolean toggleShowSaved(){
        if(mIsShowingAll && showOnlyFavourites()){
            mIsShowingAll = false;
        }else if(!mIsShowingAll && showAll()){
            mIsShowingAll = true;
        }
        return mIsShowingAll;
    }

    public boolean getIsShowingAll(){
        return mIsShowingAll;
    }

    public interface onInfoSessionClickListener{
        void onInfoSessionClick(InfoSessionData infoSessionData, int position, int type);
    }
}
