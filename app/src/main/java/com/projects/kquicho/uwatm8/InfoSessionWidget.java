package com.projects.kquicho.uwatm8;


import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;

public class InfoSessionWidget implements JSONDownloader.onDownloadListener {

    private final String LOGCAT_TAG = "InfoSessionWidget";
    private static InfoSessionWidget mInstance = null;
    private static ResourcesParser mParser;
    private UWClientResponseHandler mHandler;

    public static InfoSessionWidget getInstance(UWClientResponseHandler handler) {
        if(mInstance == null){
            mInstance = new InfoSessionWidget(handler);
        }
        return mInstance;
    }

    private InfoSessionWidget(UWClientResponseHandler handler) {
        mParser = new ResourcesParser();
        mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
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
