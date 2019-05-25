package com.example.enigmator.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpManager {
    public static final String PREF_USER_TOKEN = "pref_user_token";

    private final String token;
    private final Queue<HttpRequest> requestQueue;
    private HttpAsyncTask httpAsyncTask;

    public HttpManager(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        token = prefs.getString(PREF_USER_TOKEN, null);

        requestQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Adds an {@link HttpRequest} to the request Queue.
     * Executes this request if no request is performing
     *
     * @param method  the method to perform
     * @param route   the HTTP route to query
     * @param requestBody  the request body as a JSON String
     * @param listener   the listener for UI operation
     */
    public void addToQueue(String method, String route, @Nullable String requestBody, HttpRequest.HttpRequestListener listener) {
        requestQueue.add(new HttpRequest(method, route, requestBody, listener));
        startNext(false);
    }

    /**
     * Starts the next request if there is one pending, and the previous has finished
     */
    void startNext(boolean forceNext) {
        if (forceNext || httpAsyncTask == null || httpAsyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            if (requestQueue.size() > 0) {
                httpAsyncTask = new HttpAsyncTask(this, token, requestQueue.remove());
                httpAsyncTask.execute();
            }
        }
    }

    public void cancel(boolean mayInterruptIfRunning) {
        if (httpAsyncTask != null) {
            httpAsyncTask.cancel(mayInterruptIfRunning);
        }
    }
}
