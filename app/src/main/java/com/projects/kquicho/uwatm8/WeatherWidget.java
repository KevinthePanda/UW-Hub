package com.projects.kquicho.uwatm8;

import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Weather.WeatherParser;

public class WeatherWidget extends HomeWidget implements JSONDownloader.onDownloadListener {


    public static HomeWidget getInstance(UWClientResponseHandler handler) {
        if(mInstance == null){
            mParser = new WeatherParser();
            mParser.setParseType(WeatherParser.ParseType.CURRENT.ordinal());
            mInstance = new WeatherWidget(handler);
        }
        return mInstance;
    }

    private WeatherWidget(UWClientResponseHandler handler) {
        super(handler);
    }

}
