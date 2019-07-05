package com.example.enigmator.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

import lombok.AllArgsConstructor;

public class LoginActivity extends HttpActivity {
    private static final String PREF_USERNAME = "pref_username";
    private static final String PREF_USER_ID = "pref_user_id";

    private ProgressBar mProgressBar;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mProgressBar = findViewById(R.id.progress_loading);
        mButton = findViewById(R.id.btn_login);
        final EditText editUsername = findViewById(R.id.edit_username);
        final EditText editPassword = findViewById(R.id.edit_password);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editUsername.getText().toString();
                String password = editPassword.getText().toString();

                if (username.length() > 0 && password.length() > 0) {
                    if (isInternetConnected()) {
                        login(username, password);
                    } else {
                        buildNoConnectionErrorDialog(null).show();
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString(PREF_USERNAME, null);
        String userToken = prefs.getString(HttpManager.PREF_USER_TOKEN, null);
        int userId = prefs.getInt(PREF_USER_ID, -1);

        if (username != null) {
            editUsername.setText(username);
        }

        // TODO: check internet before ?
        if (userToken != null && userId > -1) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.USER_ID_KEY, userId);
            startActivity(intent);
        }
    }

    // TODO: Fix: login fails on first attempt after disconnection
    private void login(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        Gson gson = new Gson();
        String body = gson.toJson(credentials);

        httpManager.addToQueue(HttpRequest.POST, "/UserEnigmators/login", body, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                mProgressBar.setVisibility(View.VISIBLE);
                mButton.setEnabled(true);
            }

            @Override
            public void handleSuccess(Response response) {
                mProgressBar.setVisibility(View.GONE);
                mButton.setEnabled(true);

                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(response.getContent()).getAsJsonObject();
                int userId = object.get("userId").getAsInt();
                String token = object.get("id").getAsString();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                editor.putString(HttpManager.PREF_USER_TOKEN, token);
                editor.putInt(PREF_USER_ID, userId);
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.USER_ID_KEY, userId);
                startActivity(intent);
            }

            @Override
            public void handleError(Response error) {
                mProgressBar.setVisibility(View.GONE);
                mButton.setEnabled(true);
                Log.e(LoginActivity.class.getName(), error.toString());
            }
        });

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(PREF_USERNAME, username);
        editor.apply();
    }

    @AllArgsConstructor
    private static class Credentials implements Serializable {
        final String username;
        final String password;
    }
}
