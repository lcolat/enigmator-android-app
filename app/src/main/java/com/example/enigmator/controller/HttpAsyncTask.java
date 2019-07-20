package com.example.enigmator.controller;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.enigmator.entity.Media;
import com.example.enigmator.entity.Response;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

class HttpAsyncTask extends AsyncTask<Void, Void, Response> {
    private static final String TAG = HttpAsyncTask.class.getName();

    static final String BASE_URL = "http://35.180.227.54:3000/api";

    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;

    private final HttpManager httpManager;
    private final HttpRequest request;
    private final String token;

    HttpAsyncTask(HttpManager httpManager, @Nullable String token, HttpRequest request) {
        this.httpManager = httpManager;
        this.request = request;
        this.token = token;
    }

    @Override
    protected void onPreExecute() {
        if (request.getListener() != null) {
            request.getListener().prepareRequest();
        }
    }

    @Override
    protected Response doInBackground(Void... voids) {
        Log.d(TAG, "Request: " + BASE_URL + request.getRoute());

        String result = null;
        HttpURLConnection connection = null;
        int responseCode = 400; // Default
        com.squareup.okhttp.Response response = null;

        try {
            // Initialization
            URL url = new URL(BASE_URL + request.getRoute());

            // File upload
            String filePath = request.getFilePath();
            if (filePath != null) {
                OkHttpClient httpClient = new OkHttpClient();
                RequestBody requestBody = Media.buildRequestBody(filePath, request.getMediaType());

                assert token != null;
                Request request = new Request.Builder()
                        .header("Authorization", token)
                        .url(url)
                        .post(requestBody)
                        .build();

                response = httpClient.newCall(request).execute();

                responseCode = response.code();
                result = response.message();
            } else {
                // Other requests
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod(request.getMethod());
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setRequestProperty("Accept", "application/json");

                // Set Token
                if (token != null) {
                    connection.setRequestProperty("Authorization", token);
                }

                String requestBody = request.getRequestBody();
                // Send data
                if (requestBody != null) {
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setDoOutput(true);
                    byte[] output;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        output = requestBody.getBytes(UTF_8);
                    } else {
                        //noinspection CharsetObjectCanBeUsed
                        output = requestBody.getBytes("UTF-8");
                    }
                    //connection.setFixedLengthStreamingMode(output.length);

                    OutputStream om = new BufferedOutputStream(connection.getOutputStream());
                    om.write(output);
                    om.flush();
                    om.close();
                }

                responseCode = connection.getResponseCode();
            }

            Log.d(TAG, "StatusCode: " + responseCode + " " + request.getMethod() + "/ " + request.getRoute());

            // Handle result
            if (responseCode < 400) {
                // Read data
                InputStream im;
                if (connection != null) {
                    im = new BufferedInputStream(connection.getInputStream());
                    result = readStream(im);
                } else {
                    result = response.body().string();
                }
            } else {
                if (connection != null) {
                    InputStream errorStream = new BufferedInputStream(connection.getErrorStream());
                    result = readStream(errorStream);
                }
                cancel(false);
                Log.e(TAG, "Error in handling result Stream: " + result);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURL: " + BASE_URL + request.getRoute(), e);
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

        return new Response(responseCode, result);
    }

    @Override
    protected void onCancelled(Response response) {
        super.onCancelled();
        if (request.getListener() != null && response != null) {
            request.getListener().handleError(response);
        }
        httpManager.startNext(true);
    }

    @Override
    protected void onPostExecute(Response r) {
        Log.d(TAG, r.getContent());
        if (request.getListener() != null) {
            request.getListener().handleSuccess(r);
        }
        httpManager.startNext(true);
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
