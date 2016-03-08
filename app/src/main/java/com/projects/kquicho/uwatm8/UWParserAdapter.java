package com.projects.kquicho.uwatm8;


import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.projects.kquicho.uw_api_client.Core.UWParser;
import com.projects.kquicho.uw_api_client.Resources.InfoSession;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;
import com.projects.kquicho.uw_api_client.Weather.WeatherParser;

import java.util.ArrayList;
import java.util.Collections;

public class UWParserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DraggableItemAdapter<RecyclerView.ViewHolder>,
        SwipeableItemAdapter<RecyclerView.ViewHolder> {

    private final String TAG = "UWParserAdapter";
    
    private interface Draggable extends DraggableItemConstants {
    }
    private interface Swipeable extends SwipeableItemConstants {
    }

    private ArrayList<UWData> mData;
    private final int WEATHER = 0, INFO_SESSIONS = 1;

    public UWParserAdapter(ArrayList<UWData> data){
        mData = data;
        setHasStableIds(true);
    }


    private static abstract class DraggableSwipeableHolder extends AbstractDraggableSwipeableItemViewHolder {
        public View container;
        public Button removeBtn;
        public View dragHandle;

        public DraggableSwipeableHolder(View itemView){
            super(itemView);
            container = itemView.findViewById(R.id.container);
            removeBtn = (Button) itemView.findViewById(R.id.remove_widget);
            dragHandle = itemView.findViewById(R.id.header);
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }

    public static class WeatherViewHolder extends DraggableSwipeableHolder {
        public TextView currentTemp;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            currentTemp = (TextView) itemView.findViewById(R.id.current_temp);
        }
    }

    public static class InfoSessionViewHolder extends DraggableSwipeableHolder {
        public TextView company1;
        public TextView company2;
        public TextView company3;

        public InfoSessionViewHolder(View itemView) {
            super(itemView);
            company1 = (TextView) itemView.findViewById(R.id.company_1);
            company2 = (TextView) itemView.findViewById(R.id.company_2);
            company3 = (TextView) itemView.findViewById(R.id.company_3);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getParser() instanceof WeatherParser) {
            return WEATHER;
        } else if (mData.get(position).getParser() instanceof ResourcesParser) {
            return INFO_SESSIONS;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        switch (viewType) {
            case WEATHER:
                view = inflater.inflate(R.layout.weather_row, parent, false);
                viewHolder = new WeatherViewHolder(view);
                break;
            case INFO_SESSIONS:
                view = inflater.inflate(R.layout.info_session_row, parent, false);
                viewHolder = new InfoSessionViewHolder(view);
                break;
            default:
                view = inflater.inflate(R.layout.weather_row, parent, false);
                viewHolder = new WeatherViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        ((DraggableSwipeableHolder) viewHolder).removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.remove(position);
                notifyDataSetChanged();
                String tag;
                switch (viewHolder.getItemViewType()) {
                    case WEATHER:
                        tag = WeatherWidget.TAG;
                        WeatherWidget.destroyWidget();
                        break;
                    case INFO_SESSIONS:
                        tag = InfoSessionWidget.TAG;
                        InfoSessionWidget.destroyWidget();
                        break;
                    default:
                        tag = "";
                        break;
                }
            }
        });
        ((DraggableSwipeableHolder)viewHolder).container.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mData.get(position).setPinned(false);
                notifyItemChanged(position);
            }
        });
        switch (viewHolder.getItemViewType()) {
            case WEATHER:
                WeatherViewHolder vh1 = (WeatherViewHolder) viewHolder;
                final int dragState1 = vh1.getDragStateFlags();

                if (((dragState1 & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
                    int bgResId;

                    if ((dragState1 & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                        bgResId = R.drawable.bg_item_dragging_active_state;

                        // need to clear drawable state here to get correct appearance of the dragging item.
                        DrawableUtils.clearState(vh1.container.getForeground());
                    } else if ((dragState1 & Draggable.STATE_FLAG_DRAGGING) != 0) {
                        bgResId = R.drawable.bg_item_dragging_state;
                    } else {
                        bgResId = R.drawable.bg_item_normal_state;
                    }

                    vh1.container.setBackgroundResource(bgResId);
                }

                configureWeatherViewHolder(vh1, position);
                break;
            case INFO_SESSIONS:
                InfoSessionViewHolder vh2 = (InfoSessionViewHolder) viewHolder;
                final int dragState2 = vh2.getDragStateFlags();
                if (((dragState2 & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
                    int bgResId;

                    if ((dragState2 & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                        bgResId = R.drawable.bg_item_dragging_active_state;

                        // need to clear drawable state here to get correct appearance of the dragging item.
                        DrawableUtils.clearState(vh2.container.getForeground());
                    } else if ((dragState2 & Draggable.STATE_FLAG_DRAGGING) != 0) {
                        bgResId = R.drawable.bg_item_dragging_state;
                    } else {
                        bgResId = R.drawable.bg_item_normal_state;
                    }

                    vh2.container.setBackgroundResource(bgResId);
                }

                configureInfoSessionViewHolder(vh2, position);
                break;
            default:
                WeatherViewHolder vh3 = (WeatherViewHolder) viewHolder;
                configureWeatherViewHolder(vh3, position);
                break;
        }
        // set swiping properties
        ((DraggableSwipeableHolder)viewHolder).setMaxLeftSwipeAmount(-0.29f);
        ((DraggableSwipeableHolder)viewHolder).setMaxRightSwipeAmount(0);
        ((DraggableSwipeableHolder)viewHolder).setSwipeItemHorizontalSlideAmount(
                mData.get(position).isPinned() ? -0.29f : 0);

    }

    private void configureWeatherViewHolder(WeatherViewHolder viewHolder, int position){
        UWParser parser = mData.get(position).getParser();

        // Set item views based on the data model
        TextView textView = viewHolder.currentTemp;
        double temp = ((WeatherParser)parser).getCurrentTemperature();
        if(temp >= 0){
            textView.setTextColor(Color.GREEN);
        }else{
            textView.setTextColor(Color.RED);
        }

        textView.setText(temp + "°C");

    }

    private void configureInfoSessionViewHolder(InfoSessionViewHolder viewHolder, int position){
        ResourcesParser parser = (ResourcesParser)mData.get(position).getParser();

        // Set item views based on the data model
        TextView company1 = viewHolder.company1;
        TextView company2 = viewHolder.company2;
        TextView company3 = viewHolder.company3;

        ArrayList<InfoSession> infoSessions = parser.getInfoSessions();
        if(infoSessions.size() >= 3) {
            String employer1 = parser.getInfoSessions().get(0).getEmployer();
            String employer2 = parser.getInfoSessions().get(1).getEmployer();
            String employer3 = parser.getInfoSessions().get(2).getEmployer();
            company1.setText(employer1);
            company2.setText(employer2);
            company3.setText(employer3);
        }
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }

        Collections.swap(mData, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = ((DraggableSwipeableHolder)holder).container;
        final View dragHandleView = ((DraggableSwipeableHolder)holder).dragHandle;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerView.ViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public int onGetSwipeReactionType(RecyclerView.ViewHolder holder, int position, int x, int y) {
        if (ViewUtils.hitTest(((DraggableSwipeableHolder)holder).getSwipeableContainerView(), x, y)) {
            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        } else {
            return Swipeable.REACTION_CAN_NOT_SWIPE_BOTH_H;
        }
    }

    @Override
    public void onSetSwipeBackground(RecyclerView.ViewHolder holder, int position, int type) {
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

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(RecyclerView.ViewHolder holder, int position, int result) {
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
        private UWParserAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(UWParserAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            UWData item = mAdapter.mData.get(mPosition);

            if (!item.isPinned()) {
                item.setPinned(true);
                mAdapter.notifyItemChanged(mPosition);
                mSetPinned = true;
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
        private UWParserAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(UWParserAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            UWData item = mAdapter.mData.get(mPosition);
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

}
