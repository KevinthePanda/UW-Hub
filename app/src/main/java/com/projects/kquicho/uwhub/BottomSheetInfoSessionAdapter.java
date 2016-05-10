package com.projects.kquicho.uwhub;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.kquicho.uw_api_client.Resources.InfoSession;

import java.util.ArrayList;

/**
 * Created by Kevin Quicho on 4/7/2016.
 */
public class BottomSheetInfoSessionAdapter extends RecyclerView.Adapter<BottomSheetInfoSessionAdapter.InfoSessionHolder>{
    private final String TAG = "BottomSheetInfoSessionAdapter";
    private ArrayList<Pair<Integer,InfoSession>> mData;
    private onBottomSheetInfoSessionClickListener mOnBottomSheetInfoSessionClickListener;

    public BottomSheetInfoSessionAdapter(ArrayList<Pair<Integer,InfoSession>> data,
                                         onBottomSheetInfoSessionClickListener onBottomSheetInfoSessionClickListener){
        mData = data;
        mOnBottomSheetInfoSessionClickListener = onBottomSheetInfoSessionClickListener;
    }

    public class InfoSessionHolder extends RecyclerView.ViewHolder {
        public View mContainer;
        public TextView mDate;
        public TextView mTime;
        public TextView mLocation;

        public InfoSessionHolder(View itemView){
            super(itemView);
            mContainer = itemView.findViewById(R.id.container);
            mDate = (TextView)itemView.findViewById(R.id.date);
            mTime = (TextView)itemView.findViewById(R.id.time);
            mLocation = (TextView)itemView.findViewById(R.id.location);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    @Override
    public InfoSessionHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_info_session_row, parent, false);
        return new InfoSessionHolder(view);
    }

    @Override
    public void onBindViewHolder(final InfoSessionHolder viewHolder, final int position) {
        final InfoSession data = mData.get(position).second;
        viewHolder.mDate.setText(data.getDate());
        viewHolder.mTime.setText(data.getDisplay_time_range());
        viewHolder.mLocation.setText(data.getBuildingCode() + " - " + data.getBuildingRoom());

        viewHolder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnBottomSheetInfoSessionClickListener.onBottomSheetInfoSessionClick(mData.get(position).first);
            }
        });

    }

    public interface onBottomSheetInfoSessionClickListener{
        void onBottomSheetInfoSessionClick(int position);
    }

}
