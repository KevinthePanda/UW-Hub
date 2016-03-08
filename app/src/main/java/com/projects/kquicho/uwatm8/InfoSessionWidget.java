package com.projects.kquicho.uwatm8;


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

    public static InfoSessionWidget getInstance(UWClientResponseHandler handler) {
        if(mInstance == null){
            mInstance = new InfoSessionWidget(handler);
        }
        return mInstance;
    }

    public static InfoSessionWidget getInstance(UWClientResponseHandler handler, Integer position) {
        if(mInstance == null){
            mPosition = position;
            mInstance = new InfoSessionWidget(handler);
        }
        return mInstance;
    }

    public static void destroyWidget() {
        mParser = null;
        mInstance = null;
        mPosition = null;
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
        mHandler.onError(TAG + ": " + "Download failed.. url = " + givenURL );
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        mParser.setAPIResult(apiResult);
        mParser.parseJSON();
        UWData data = new UWData(mParser, TAG);
        mHandler.onSuccess(data, mPosition);
    }

}
