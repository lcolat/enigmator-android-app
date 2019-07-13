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
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;

import lombok.AllArgsConstructor;

public class LoginActivity extends HttpActivity {
    private static final String TAG = LoginActivity.class.getName();

    private static final String PREF_USERNAME = "pref_username";

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


        String userToken = prefs.getString(HttpManager.PREF_USER_TOKEN, null);
        String user = prefs.getString(CategoriesActivity.PREF_USER, null);

        if (userToken != null && user != null) {
            if (!isInternetConnected()) {
                buildNoConnectionErrorDialog(null);
            } else {
                Intent intent = new Intent(this, CategoriesActivity.class);
                startActivity(intent);
            }
        } else {
            String username = prefs.getString(PREF_USERNAME, null);
            if (username != null) {
                editUsername.setText(username);
            }
        }
    }

    private void login(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        final String body = gson.toJson(credentials);

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
                final int userId = object.get("userId").getAsInt();
                String token = object.get("id").getAsString();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                editor.putString(HttpManager.PREF_USER_TOKEN, token);
                editor.apply();

                httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/" + userId, null,
                        new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {
                                mProgressBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void handleSuccess(Response response) {
                                mProgressBar.setVisibility(View.GONE);

                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                                editor.putString(CategoriesActivity.PREF_USER, response.getContent());
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, CategoriesActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void handleError(Response error) {
                                mProgressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "Couldn't get User", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "/UserEnigmators/" + userId);
                                Log.e(TAG, error.toString());
                            }
                        });
            }

            @Override
            public void handleError(Response error) {
                mProgressBar.setVisibility(View.GONE);
                mButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "/UserEnigmators/login : " + body);
                Log.e(TAG, error.toString());
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
