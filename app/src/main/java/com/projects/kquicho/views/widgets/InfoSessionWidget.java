package com.projects.kquicho.views.widgets;


import android.content.Context;
import android.util.Log;

import com.projects.kquicho.models.InfoSessionWidgetData;
import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.JSONDownloader;
import com.projects.kquicho.network.Core.UWOpenDataAPI;
import com.projects.kquicho.network.Resources.ResourcesParser;
import com.projects.kquicho.network.UWClientResponseHandler;

public class InfoSessionWidget implements JSONDownloader.onDownloadListener {

    public static final String TAG = "InfoSessionWidget";
    private static InfoSessionWidget mInstance = null;
    private static ResourcesParser mParser;
    private static Integer mPosition;
    private UWClientResponseHandler mHandler;
    private Context mContext;

    public static boolean hasInstance(){
        return mInstance != null;
    }


    public static InfoSessionWidget getInstance(Context context, UWClientResponseHandler handler) {
        if(mInstance == null){
            mInstance = new InfoSessionWidget(context, handler);
        }
        return mInstance;
    }

    public static InfoSessionWidget getInstance(Context context, UWClientResponseHandler handler, Integer position) {
        if(mInstance == null){
            mPosition = position;
            mInstance = new InfoSessionWidget(context, handler);
        }
        return mInstance;
    }

    public static void destroyWidget() {
        mParser = null;
        mInstance = null;
        mPosition = null;
    }


    private InfoSessionWidget(Context context, UWClientResponseHandler handler) {
        mParser = new ResourcesParser();
        mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
        mHandler = handler;
        mContext = context;
        String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());

        JSONDownloader downloader = new JSONDownloader(context, url);
        downloader.setOnDownloadListener(this);
        downloader.start();
    }

    @Override
    public void onDownloadFail(String givenURL, int index, boolean noConnection) {
        mHandler.onError(TAG + ": " + "Download failed.. url = " + givenURL, noConnection );
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        Log.d(TAG, "onDownloadComplete");
        if(mParser == null || mHandler == null){
            Log.e(TAG, "download completed but nothing to pass to");
            return;
        }
        mParser.setAPIResult(apiResult);
        mParser.parseHomeWidgetInfoSessions(mContext);
        InfoSessionWidgetData data = new InfoSessionWidgetData(mParser.getHomeWidgetInfoSessions(),
                mParser.getHomeWidgetSavedInfoSessions());
        mHandler.onSuccess(data, mPosition);
    }

}
