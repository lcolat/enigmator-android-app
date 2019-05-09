package com.example.enigmator.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.enigmator.activity.IHttpComponent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpAsyncTask extends AsyncTask<Void, Void, String> {
    public static final String PREF_USER_TOKEN = "pref_user_token";
    private static final String TAG = HttpAsyncTask.class.getName();

    private static final String BASE_URL = "http://3.19.31.245:3000/api";

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;

    private final WeakReference<IHttpComponent> httpComponent;
    private final String route;
    private final String method;
    private String requestBody;
    private final String token;

    public HttpAsyncTask(IHttpComponent component, String method, String route, @Nullable String requestBody) {
        if (component instanceof Context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((AppCompatActivity) component);
            token = prefs.getString(PREF_USER_TOKEN, null);
        } else if (component instanceof Fragment) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(((Fragment) component).getContext());
            token = prefs.getString(PREF_USER_TOKEN, null);
        } else {
            token = null;
        }

        this.httpComponent = new WeakReference<>(component);
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
        httpComponent.get().prepareRequest();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result;
        HttpURLConnection connection = null;

        try {
            // Initialization
            URL url = new URL(BASE_URL + route);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");

            // Set Token
            if (token != null) {
                connection.setRequestProperty("Authorization", token);
            }

            // Send data
            if (requestBody != null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(requestBody.length());

                OutputStream om = new BufferedOutputStream(connection.getOutputStream());
                om.write(requestBody.getBytes());
                om.close();
            }

            int responseCode  = connection.getResponseCode();
            Log.e(TAG, "Code: " + responseCode);

            if (responseCode < 400) {
                // Read data
                InputStream im = new BufferedInputStream(connection.getInputStream());
                result = readStream(im);
            } else {
                InputStream errorStream = new BufferedInputStream(connection.getErrorStream());
                result = readStream(errorStream);
                cancel(false);
                Log.e(TAG, "Error Stream: " + result);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURL: " + BASE_URL + route);
            cancel(false);
            result = e.getMessage();
        } catch (IOException e) {
            Log.e(TAG, "Error connection", e);
            result = e.getMessage();
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
        httpComponent.get().handleError(str);
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, s);
        if (s != null) {
            httpComponent.get().handleSuccess(s);
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
