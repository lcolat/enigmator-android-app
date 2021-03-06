package com.example.enigmator.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.google.gson.Gson;

/**
 * This class handles network stuff.
 */
public abstract class HttpActivity extends AppCompatActivity {
    HttpManager httpManager;
    Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpManager = new HttpManager(this);
        gson = new Gson();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpManager != null) {
            httpManager.cancel(true);
        }
    }

    AlertDialog buildNoConnectionErrorDialog(@Nullable DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_no_connection_title);
        builder.setMessage(R.string.dialog_no_connection_message);
        builder.setPositiveButton(android.R.string.ok, listener);
        return builder.create();
    }

    boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
