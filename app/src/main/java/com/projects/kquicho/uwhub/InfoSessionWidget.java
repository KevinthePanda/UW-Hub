package com.projects.kquicho.uwhub;


import android.content.Context;
import android.util.Log;

import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;

public class InfoSessionWidget implements JSONDownloader.onDownloadListener {

    public static final String TAG = "InfoSessionWidget";
    private static InfoSessionWidget mInstance = null;
    private static ResourcesParser mParser;
    private static Integer mPosition;
    private UWClientResponseHandler mHandler;
    private Context mContext;

    public static InfoSessionWidget getInstance(UWClientResponseHandler handler, Context context) {
        if(mInstance == null){
            mInstance = new InfoSessionWidget(handler, context);
        }
        return mInstance;
    }

    public static InfoSessionWidget getInstance(UWClientResponseHandler handler, Integer position, Context context) {
        if(mInstance == null){
            mPosition = position;
            mInstance = new InfoSessionWidget(handler, context);
        }
        return mInstance;
    }

    public static void destroyWidget() {
        mParser = null;
        mInstance = null;
        mPosition = null;
    }


    private InfoSessionWidget(UWClientResponseHandler handler, Context context) {
        mParser = new ResourcesParser();
        mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
        mHandler = handler;
        mContext = context;
        String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());

        JSONDownloader downloader = new JSONDownloader(url);
        downloader.setOnDownloadListener(this);
        downloader.start();
    }

    @Override
    public void onDownloadFail(String givenURL, int index) {
        mHandler.onError(TAG + ": " + "Download failed.. url = " + givenURL );
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
