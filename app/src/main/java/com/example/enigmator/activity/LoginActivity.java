package com.example.enigmator.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpAsyncTask;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Date;

public class LoginActivity extends HttpActivity implements IHttpComponent {
    static final String PREF_USERNAME = "pref_username";
    static final String PREF_PASSWORD = "pref_password";

    private ProgressBar mProgressBar;
    private Button mButton;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressBar = findViewById(R.id.progress_loading);
        mButton = findViewById(R.id.btn_login);
        final EditText editUsername = findViewById(R.id.edit_username);
        final EditText editPassword = findViewById(R.id.edit_password);

        gson = new Gson();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString(PREF_USERNAME, null);
        String password = prefs.getString(PREF_PASSWORD, null);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editUsername.getText().toString();
                String password = editPassword.getText().toString();

                if (username.length() > 0 && password.length() > 0) {

                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if (isConnected) {
                        login(username, password);
                    } else {
                        AlertDialog dialog = buildNoConnectionErrorDialog(LoginActivity.this, null);
                        dialog.show();
                    }
                }
            }
        });

        editPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mButton.performClick();
                }
                return false;
            }
        });

        if (username != null && password != null) {
            editUsername.setText(username);
            editPassword.setText(password);
            login(username, password);
        }
    }

    private void login(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        String request = gson.toJson(credentials);

        httpAsyncTask = new HttpAsyncTask(this, HttpAsyncTask.POST, "/login", request);

        // TODO: remove
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(PREF_USERNAME, username);
        editor.putString(PREF_PASSWORD, password);
        UserEnigmator currentUser = new UserEnigmator(1, 1, "Current", "user", new Date(),
                username, "current@gmail.com", true, 1, password);
        editor.putString(MainActivity.PREF_USER, new Gson().toJson(currentUser));
        editor.apply();

        startActivity(new Intent(this, MainActivity.class));
        // httpAsyncTask.execute();
    }

    @Override
    public void prepareRequest() {
        mProgressBar.setVisibility(View.VISIBLE);
        mButton.setEnabled(false);
    }

    @Override
    public void handleSuccess(String result) {
        mProgressBar.setVisibility(View.GONE);
        mButton.setEnabled(true);

        // TODO: save user locally
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(PREF_USERNAME, "username");
        editor.putString(PREF_PASSWORD, "password");
        editor.putString(MainActivity.PREF_USER, result);
        editor.apply();

        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        Log.d(LoginActivity.class.getName(), result);
    }

    @Override
    public void handleError(String error) {
        mProgressBar.setVisibility(View.GONE);
        mButton.setEnabled(true);

        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        Log.e(LoginActivity.class.getName(), "Error: "+error);
    }

    private static class Credentials implements Serializable {
        final String username;
        final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
