package com.projects.kquicho.uwatm8;


import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.projects.kquicho.uw_api_client.Core.UWParser;
import com.projects.kquicho.uw_api_client.Resources.InfoSession;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;
import com.projects.kquicho.uw_api_client.Weather.WeatherParser;

import java.util.ArrayList;
import java.util.Collections;

public class UWParserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements DraggableItemAdapter<RecyclerView.ViewHolder> {

    private final String TAG = "UWParserAdapter";
    private interface Draggable extends DraggableItemConstants {
    }

    private ArrayList<UWParser> mParsers;
    private final int WEATHER = 0, INFO_SESSIONS = 1;

    public UWParserAdapter(ArrayList<UWParser> parsers){
        mParsers = parsers;
        setHasStableIds(true);
    }


    private static abstract class DraggableHolder extends AbstractDraggableItemViewHolder{
        public View container;

        public DraggableHolder(View itemView){
            super(itemView);
            container = itemView.findViewById(R.id.container);
        }
    }

    public static class WeatherViewHolder extends DraggableHolder {
        public TextView currentTemp;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            currentTemp = (TextView) itemView.findViewById(R.id.current_temp);
        }
    }

    public static class InfoSessionViewHolder extends DraggableHolder {
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
        if (mParsers.get(position) instanceof WeatherParser) {
            return WEATHER;
        } else if (mParsers.get(position) instanceof ResourcesParser) {
            return INFO_SESSIONS;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mParsers.size();
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
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

    }

    private void configureWeatherViewHolder(WeatherViewHolder viewHolder, int position){
        UWParser parser = mParsers.get(position);

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
        ResourcesParser parser = (ResourcesParser)mParsers.get(position);

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

        Collections.swap(mParsers, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(RecyclerView.ViewHolder holder, int position, int x, int y) {
        return true;

    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(RecyclerView.ViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }




}
