package com.projects.kquicho.network;

import com.projects.kquicho.models.UWData;

public interface UWClientResponseHandler {

    void onSuccess(UWData data, Integer position);

    void onError(String error);

}
