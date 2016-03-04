package com.projects.kquicho.uwatm8;

import com.projects.kquicho.uw_api_client.Core.JSONDownloader;
import com.projects.kquicho.uw_api_client.Resources.ResourcesParser;

public class InfoSessionWidget extends HomeWidget implements JSONDownloader.onDownloadListener {

    public static HomeWidget getInstance(UWClientResponseHandler handler) {
        if(mInstance == null){
            mParser = new ResourcesParser();
            mParser.setParseType(ResourcesParser.ParseType.INFOSESSIONS.ordinal());
            mInstance = new InfoSessionWidget(handler);
        }
        return mInstance;
    }

    private InfoSessionWidget(UWClientResponseHandler handler) {
        super(handler);
    }

}
