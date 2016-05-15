package com.projects.kquicho.views.widgets;


import android.content.Context;
import android.util.Log;

import com.projects.kquicho.models.WeatherWidgetData;
import com.projects.kquicho.network.Core.APIResult;
import com.projects.kquicho.network.Core.JSONDownloader;
import com.projects.kquicho.network.Core.UWOpenDataAPI;
import com.projects.kquicho.network.Weather.WeatherParser;
import com.projects.kquicho.network.UWClientResponseHandler;

public class WeatherWidget implements JSONDownloader.onDownloadListener {

    public static final String TAG = "WeatherWidget";
    private static WeatherWidget mInstance = null;
    private static WeatherParser mParser;
    private UWClientResponseHandler mHandler;
    private static Integer mPosition;

    public static boolean hasInstance(){
        return mInstance != null;
    }

    public static WeatherWidget getInstance(Context context, UWClientResponseHandler handler) {
        if(mInstance == null){
            mInstance = new WeatherWidget(context, handler);
        }
        return mInstance;
    }
    public static WeatherWidget getInstance(Context context, UWClientResponseHandler handler, int position) {
        if(mInstance == null){
            mPosition = position;
            mInstance = new WeatherWidget(context, handler);
        }
        return mInstance;
    }

    private WeatherWidget(Context context, UWClientResponseHandler handler) {
        mParser = new WeatherParser();
        mParser.setParseType(WeatherParser.ParseType.CURRENT.ordinal());
        mHandler = handler;
        String url = UWOpenDataAPI.buildURL(mParser.getEndPoint());

        JSONDownloader downloader = new JSONDownloader(context, url);
        downloader.setOnDownloadListener(this);
        downloader.start();
    }
    public static void destroyWidget() {
        mParser = null;
        mInstance = null;
        mPosition = null;
    }

    @Override
    public void onDownloadFail(String givenURL, int index, boolean noNetwork) {
        mHandler.onError(TAG + ": " + "Download failed.. url = " + givenURL, noNetwork );
    }

    @Override
    public void onDownloadComplete(APIResult apiResult) {
        Log.d(TAG, "onDownloadComplete");
        if(mParser == null || mHandler == null){
            Log.e(TAG, "download completed but nothing to pass to");
            return;
        }
        mParser.setAPIResult(apiResult);
        mParser.parseJSON();
        WeatherWidgetData.Builder builder = new WeatherWidgetData.Builder();
        builder
                .currentTemp(mParser.getCurrentTemperature())
                .windChill(mParser.getWindchill())
                .maxTemp(mParser.getTemperature24hrMax())
                .minTemp(mParser.getTemperature24hrMin())
                .precip(mParser.getPrecipitation24hr())
                .humidity(mParser.getRelativeHumidityPercent())
                .windSpeed(mParser.getWindSpeed());
        mHandler.onSuccess(builder.createWeatherData(), mPosition);
    }


}
