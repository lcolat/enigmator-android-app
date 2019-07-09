package com.example.enigmator.utils;


import android.content.Context;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;


public final class HttpRequestGenerator {
    private HttpManager httpManager;

    public HttpRequestGenerator(Context context) {
        httpManager = new HttpManager(context);
    }

    //http://35.180.227.54:3000/api/Media?filter=%7B%22where%22%3A%7B%22id%22%3A%221%22%7D%7D&access_token=OQWYNfWleUsaSuc3CPgZvTyZtB6kgGX9HaCYldEqGCAN0B3DxXbE29q93lIbDfJM
    public void requestMediaOfEnigma(long enigmaId, HttpRequest.HttpRequestListener listener) {
        httpManager.addToQueue(HttpRequest.GET,
                "/Media?filter={\"where\":{\"enigmeID\":\"" + enigmaId + "\"}}" + enigmaId,
                null,
                listener);
    }

    public void downloadMediaOfEnigma(String fileName, HttpRequest.HttpRequestListener listener) {
        httpManager.addToQueue(HttpRequest.GET,
                "/Containers/enigme/download/" + fileName,
                null,
                listener);
    }
}