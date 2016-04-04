package com.projects.kquicho.uwatm8;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class UWParserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DraggableItemAdapter<RecyclerView.ViewHolder>,
        SwipeableItemAdapter<RecyclerView.ViewHolder> {

    Context mContext;
    private final String TAG = "UWParserAdapter";
    
    private interface Draggable extends DraggableItemConstants {
    }
    private interface Swipeable extends SwipeableItemConstants {
    }

    private ArrayList<UWData> mData;
    private final int WEATHER = 0, INFO_SESSIONS = 1;

    public UWParserAdapter(ArrayList<UWData> data, Context context){
        mData = data;
        mContext = context;
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
        public TextView mCurrentTemp;
        public TextView mHighTemp;
        public TextView mLowTemp;
        public TextView mWindSpeed;
        public TextView mHumidity;
        public TextView mPrecip;
        public TextView mWindChill;
        public TextView mCurrentTempPrefix;
        public TextView mHighTempPrefix;
        public TextView mLowTempPrefix;
        public TextView mWindChillPrefix;
        public WeatherViewHolder(View itemView) {
            super(itemView);
            mCurrentTemp = (TextView) itemView.findViewById(R.id.current_temp);
            mHighTemp = (TextView) itemView.findViewById(R.id.high_temp);
            mLowTemp = (TextView) itemView.findViewById(R.id.low_temp);
            mWindSpeed = (TextView) itemView.findViewById(R.id.wind_speed);
            mHumidity = (TextView) itemView.findViewById(R.id.humidity);
            mPrecip = (TextView) itemView.findViewById(R.id.precip);
            mWindChill = (TextView) itemView.findViewById(R.id.wind_chill_temp);
            mCurrentTempPrefix = (TextView) itemView.findViewById(R.id.current_temp_prefix);
            mHighTempPrefix = (TextView) itemView.findViewById(R.id.high_temp_prefix);
            mLowTempPrefix = (TextView) itemView.findViewById(R.id.low_temp_prefix);
            mWindChillPrefix = (TextView) itemView.findViewById(R.id.wind_chill_temp_prefix);
        }
    }

    public static class InfoSessionViewHolder extends DraggableSwipeableHolder {
        public TextView mCompany1;
        public TextView mCompany2;
        public TextView mCompany3;

        public InfoSessionViewHolder(View itemView) {
            super(itemView);
            mCompany1 = (TextView) itemView.findViewById(R.id.company_1);
            mCompany2 = (TextView) itemView.findViewById(R.id.company_2);
            mCompany3 = (TextView) itemView.findViewById(R.id.company_3);
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
                view = inflater.inflate(R.layout.widget_weather_row, parent, false);
                viewHolder = new WeatherViewHolder(view);
                break;
            case INFO_SESSIONS:
                view = inflater.inflate(R.layout.widget_info_session_row, parent, false);
                viewHolder = new InfoSessionViewHolder(view);
                break;
            default:
                view = inflater.inflate(R.layout.widget_weather_row, parent, false);
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
                switch (viewHolder.getItemViewType()) {
                    case WEATHER:
                        WeatherWidget.destroyWidget();
                        break;
                    case INFO_SESSIONS:
                        InfoSessionWidget.destroyWidget();
                        break;
                    default:
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

    public void setTempField(TextView textView, TextView prefix, double temp){
        textView.setText(String.valueOf(Math.abs(temp)));
        if(temp < 0){
            prefix.setVisibility(View.VISIBLE);
        }else{
            prefix.setVisibility(View.GONE);
        }
    }
    private void configureWeatherViewHolder(WeatherViewHolder viewHolder, int position){
        UWParser parser = mData.get(position).getParser();

        WeatherParser weatherParser = (WeatherParser)parser;

        setTempField(viewHolder.mCurrentTemp, viewHolder.mCurrentTempPrefix, weatherParser.getCurrentTemperature());
        setTempField(viewHolder.mHighTemp, viewHolder.mHighTempPrefix, weatherParser.getTemperature24hrMax());
        setTempField(viewHolder.mLowTemp, viewHolder.mLowTempPrefix, weatherParser.getTemperature24hrMin());
        setTempField(viewHolder.mWindChill, viewHolder.mWindChillPrefix, weatherParser.getWindchill());
        viewHolder.mWindSpeed.setText(String.valueOf(weatherParser.getWindSpeed()));
        viewHolder.mHumidity.setText(String.valueOf(weatherParser.getRelativeHumidityPercent()));
        viewHolder.mPrecip.setText(String.valueOf(weatherParser.getPrecipitation24hr()));


        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)viewHolder.mCurrentTemp.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mCurrentTemp.setLayoutParams(layoutParams);

        layoutParams = (RelativeLayout.LayoutParams)viewHolder.mHighTemp.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mHighTemp.setLayoutParams(layoutParams);

        layoutParams = (RelativeLayout.LayoutParams)viewHolder.mLowTemp.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mLowTemp.setLayoutParams(layoutParams);

        layoutParams = (RelativeLayout.LayoutParams)viewHolder.mWindSpeed.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mWindSpeed.setLayoutParams(layoutParams);


        layoutParams = (RelativeLayout.LayoutParams)viewHolder.mHumidity.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mHumidity.setLayoutParams(layoutParams);


        layoutParams = (RelativeLayout.LayoutParams)viewHolder.mPrecip.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mPrecip.setLayoutParams(layoutParams);

        layoutParams = (RelativeLayout.LayoutParams)viewHolder.mWindChill.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        viewHolder.mWindChill.setLayoutParams(layoutParams);

    }

    private void configureInfoSessionViewHolder(InfoSessionViewHolder viewHolder, int position){
        ResourcesParser parser = (ResourcesParser)mData.get(position).getParser();

        // Set item views based on the data model
        TextView mCompany1 = viewHolder.mCompany1;
        TextView mCompany2 = viewHolder.mCompany2;
        TextView mCompany3 = viewHolder.mCompany3;

        ArrayList<InfoSession> infoSessions = parser.getInfoSessions();
        if(infoSessions.size() >= 3) {
            String employer1 = parser.getInfoSessions().get(0).getEmployer();
            String employer2 = parser.getInfoSessions().get(1).getEmployer();
            String employer3 = parser.getInfoSessions().get(2).getEmployer();
            mCompany1.setText(employer1);
            mCompany2.setText(employer2);
            mCompany3.setText(employer3);
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
