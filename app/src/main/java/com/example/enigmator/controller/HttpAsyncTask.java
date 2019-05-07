package com.example.enigmator.controller;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.enigmator.activity.IHttpComponent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = HttpAsyncTask.class.getName();

    // TODO: change url
    private static final String BASE_URL = "http://localhost:8000";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    private static final int READ_TIMEOUT = 9000;
    private static final int CONNECT_TIMEOUT = 14000;

    private final WeakReference<IHttpComponent> activity;
    private final String route;
    private final String method;
    private String requestBody;

    public HttpAsyncTask(IHttpComponent activity, String method, String route, @Nullable String requestBody) {
        this.activity = new WeakReference<>(activity);
        this.route = route;
        this.method = method;
        if (POST.equals(method) || PUT.equals(method)) {
            if (requestBody == null) {
                throw new IllegalArgumentException("Cannot perform Post or Put without a Request Body");
            }
            this.requestBody = requestBody;
        } else if (!GET.equals(method) && !DELETE.equals(method)) {
            throw new UnsupportedOperationException("HTTP Method '" + method + "' is not allowed.");
        }
    }

    @Override
    protected void onPreExecute() {
        activity.get().prepareRequest();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = null;
        HttpURLConnection connection = null;

        try {
            // Initialization
            URL url = new URL(BASE_URL + route);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");

            // Send data
            if (requestBody != null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(requestBody.length());

                 OutputStream om = new BufferedOutputStream(connection.getOutputStream());
                om.write(requestBody.getBytes());
                om.close();
            }

            // Read data
            InputStream im = new BufferedInputStream(connection.getInputStream());
            result = readStream(im);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURL: " + BASE_URL + route);
            cancel(false);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening connection", e);
            cancel(false);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    @Override
    protected void onCancelled(String str) {
        super.onCancelled();
        activity.get().handleError(str);
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            activity.get().handleSuccess(s);
        }
    }

    private String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line = r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }
}
