package com.projects.kquicho.uwhub;

public interface UWClientResponseHandler {

    public void onSuccess(UWData data, Integer position);

    public void onError(String error);

}
