package com.projects.kquicho.network.Core;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by ZainH on 31/08/2015.
 *
 * Abstract:
 * The JSONDownloader class is responsible for downloading raw JSON data from a given URL.
 */
public class JSONDownloader extends Thread {

    // url array to contain all urls that we will download from
    String urls[] = null;
    Context mContext;

    // callback used to pass APIResults back at the class that created this object
    onDownloadListener callBack = null;

    private InputStream is = null;
    private String rawDownloadedJSON = null;

    public interface onDownloadListener {
        void onDownloadComplete(@NonNull APIResult apiResult);
        void onDownloadFail(String givenURL, int index, boolean noNetwork);
    }


    // set the callBack method, for when each download is complete
    public void setOnDownloadListener(onDownloadListener callBack) {
        try {
            this.callBack = callBack;
        } catch (ClassCastException e) {
            throw new ClassCastException(callBack.getClass().toString()
                    + " must implement onDownloadListener");
        }
    }

    // urls should be created using UWOpenDataAPI.buildURL(...);
    public JSONDownloader(Context context, String ... urls){
        this.mContext = context;
        this.urls = urls;
    }

    public JSONDownloader(){

    }

    public void setUrls(String ... urls){
        this.urls = urls;
    }

    // starts the download, with all the given urls
    @Override
    public void run() {
        if(urls == null || urls.length == 0) return;

        int NUM_URLS = urls.length;
        for(int i = 0; i <NUM_URLS; i++){
            String executableURL = urls[i];
            if(!isNetworkAvailable()){
                callBack.onDownloadFail(executableURL, i, true);
                continue;
            }
            APIResult result = downloadJSON(executableURL, i);

            if(callBack != null) {
                if (result != null) {
                    callBack.onDownloadComplete(result);
                } else {
                    callBack.onDownloadFail(executableURL, i, false);
                }
            }
        }
    }


    private APIResult downloadJSON(String url, int index){
        try {
            URL requesturl = new URL(url);
            URLConnection connection = requesturl.openConnection();
            is = connection.getInputStream();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n"); // read each line, add to string builder
            }
            is.close();
            rawDownloadedJSON = sb.toString();
        } catch (Exception e) {

        }
        // try to parse the string
        try {
            APIResult apiResult = new APIResult();
            JSONObject jsonObject = new JSONObject(rawDownloadedJSON);

            apiResult.setResultJSON(jsonObject);
            apiResult.setIndex(index);
            apiResult.setRawJSON(rawDownloadedJSON);
            apiResult.setUrl(url);

            return apiResult;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
