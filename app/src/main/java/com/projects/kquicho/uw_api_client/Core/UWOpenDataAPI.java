package com.projects.kquicho.uw_api_client.Core;
/**
 * Created by ZainH on 31/08/2015.
 */
public class UWOpenDataAPI {
    // the base url for each API call
    public final static String URL_BASE = "https://api.uwaterloo.ca/v2/";
    public final static String API_KEY = "6eaf16842765f4a6d908e1a6bb13b7e9";

    /* Params:
    *       endPoint: the endpoint URL specific to each call, and JSON Data requested
    *       apiKey: a registered, valid API key that can be requested from https://api.uwaterloo.ca/#!/keygen
    * */
    public static String buildURL(final String endPoint){
        return URL_BASE + endPoint + ".json?key=" + API_KEY;
    }
}
