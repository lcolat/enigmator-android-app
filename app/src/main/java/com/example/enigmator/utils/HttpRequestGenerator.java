package com.example.enigmator.utils;


import android.content.Context;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public final class HttpRequestGenerator {
    private HttpManager httpManager;

    public HttpRequestGenerator(Context context) {
        httpManager = new HttpManager(context);
    }

    public void requestMediaOfEnigma(long enigmaId, HttpRequest.HttpRequestListener listener) {
        httpManager.addToQueue(HttpRequest.GET,
                "/Media?filter={\"where\":{\"enigmeID\":\"" + enigmaId + "\"}}",
                null,
                listener);
    }

    public void answerEnigma(long enigmaId, String answer, HttpRequest.HttpRequestListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("id", Long.toString(enigmaId));
        body.put("answer", answer);
        Gson gson = new Gson();

        httpManager.addToQueue(HttpRequest.POST,
                "/Enigmes/" + enigmaId + "/AnswerEnigme",
                gson.toJson(body),
                listener);
    }
}