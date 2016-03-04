package com.projects.kquicho.uwatm8;

import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Core.UWParser;

public abstract class HomeWidget implements JSONDownloader.onDownloadListener {
    private final String LOGCAT_TAG = "HomeWidget";

    protected static HomeWidget mInstance = null;
    protected static UWParser mParser;
    private UWClientResponseHandler mHandler;

    public HomeWidget(UWClientResponseHandler handler){
        mHandler = handler;
        String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());
        JSONDownloader downloader = new JSONDownloader(url);
        downloader.setOnDownloadListener(this);
        downloader.start();
    }

    @Override
    public void onDownloadFail(String givenURL, int index) {
        mHandler.onError(LOGCAT_TAG + ": " + "Download failed.. url = " + givenURL );
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        mParser.setAPIResult(apiResult);
        mParser.parseJSON();
        mHandler.onSuccess(mParser);
    }
}
