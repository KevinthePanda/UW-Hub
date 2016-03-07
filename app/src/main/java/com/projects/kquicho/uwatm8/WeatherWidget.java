package com.projects.kquicho.uwatm8;


import com.projects.kquicho.uw_api_client.Core.APIResult;
import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Core.UWOpenDataAPI;
import com.projects.kquicho.uw_api_client.Weather.WeatherParser;

public class WeatherWidget implements JSONDownloader.onDownloadListener {

    public static final String TAG = "WeatherWidget";
    private static WeatherWidget mInstance = null;
    private static WeatherParser mParser;
    private UWClientResponseHandler mHandler;

    public static WeatherWidget getInstance(UWClientResponseHandler handler) {
        if(mInstance == null){
            mInstance = new WeatherWidget(handler);
        }
        return mInstance;
    }

    private WeatherWidget(UWClientResponseHandler handler) {
        mParser = new WeatherParser();
        mParser.setParseType(WeatherParser.ParseType.CURRENT.ordinal());
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
        mHandler.onSuccess(mParser);
    }


}
