package com.projects.kquicho.uwatm8;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projects.kquicho.uw_api_client.Core.UWParser;

import java.util.ArrayList;


public class HomeFragment extends Fragment implements  UWClientResponseHandler{
    final String LOGCAT_TAG = "HomeFragment";

    private ArrayList<UWParser> mParsers = new ArrayList<>();
    private UWParserAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        RecyclerView recyclerView =  (RecyclerView)view.findViewById(R.id.recycler_view);
        mAdapter = new UWParserAdapter(mParsers);
        recyclerView.setAdapter(mAdapter);
        InfoSessionWidget.getInstance(this);

//        WeatherWidget.getInstance(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onSuccess(UWParser parser) {
        Log.i(LOGCAT_TAG,"onSuccess" );
        mParsers.add(parser);
        android.os.Handler handler = new android.os.Handler(getActivity().getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyItemInserted(mParsers.size() - 1);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onError(String error){
        Log.e(LOGCAT_TAG, error);
    }
}
