package com.example.enigmator.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserActivity extends HttpActivity {
    private static final String TAG = UserActivity.class.getName();

    public static final String USER_KEY = "user_key";

    private FloatingActionButton btnCompare, btnAddUser;
    private ProgressBar progressLoading;
    private LinearLayout layoutCompare;

    private UserEnigmator currentUser;

    private boolean isFriend, isComparing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        final UserEnigmator user = (UserEnigmator) intent.getSerializableExtra(USER_KEY);
        currentUser = UserEnigmator.getCurrentUser(this);
        assert currentUser != null;
        boolean isSelfProfile = user.getId() == currentUser.getId();

        isComparing = false;

        String username = user.getUsername();
        setTitle(username);

        layoutCompare = findViewById(R.id.layout_compare);
        btnCompare = findViewById(R.id.btn_compare);
        btnAddUser = findViewById(R.id.btn_user_action);
        progressLoading = findViewById(R.id.progress_loading);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            btnAddUser.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
            btnCompare.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }

        TextView textScore = findViewById(R.id.text_user_score);
        textScore.setText(getString(R.string.text_score, user.getScore()));

        final TextView textEasy = findViewById(R.id.text_easy_solved);
        final TextView textMedium = findViewById(R.id.text_medium_solved);
        final TextView textHard = findViewById(R.id.text_hard_solved);
        final TextView textExtreme = findViewById(R.id.text_extreme_solved);
        final TextView textTotal = findViewById(R.id.text_total_solved);

        httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/" + user.getId() + "/GetEnigmeDone", null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                progressLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleSuccess(Response response) {
                progressLoading.setVisibility(View.GONE);

                List<Enigma> userSolved = new ArrayList<>();
                if (response.getStatusCode() != 204) {
                    userSolved = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class)));
                }

                setTextViews(UserActivity.this, userSolved, textEasy, textMedium, textHard, textExtreme, textTotal);
            }

            @Override
            public void handleError(Response error) {
                progressLoading.setVisibility(View.GONE);
                Log.e(TAG, "/UserEnigmators/" + user.getId() + "/GetEnigmeDone");
                Log.e(TAG, error.toString());
            }
        });

        if (isSelfProfile) {
            btnCompare.hide();
        } else {
            TextView textSelfScore = findViewById(R.id.text_self_user_score);
            textSelfScore.setText(getString(R.string.text_score, currentUser.getScore()));

            final TextView textSelfEasy = findViewById(R.id.text_self_easy_solved);
            final TextView textSelfMedium = findViewById(R.id.text_self_medium_solved);
            final TextView textSelfHard = findViewById(R.id.text_self_hard_solved);
            final TextView textSelfExtreme = findViewById(R.id.text_self_extreme_solved);
            final TextView textSelfTotal = findViewById(R.id.text_self_total_solved);

            httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/" + currentUser.getId() + "/GetEnigmeDone", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    btnCompare.setEnabled(false);
                }

                @Override
                public void handleSuccess(Response response) {
                    btnCompare.setEnabled(true);

                    List<Enigma> selfSolved = new ArrayList<>();
                    if (response.getStatusCode() != 204) {
                        selfSolved = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Enigma[].class)));
                    }

                    setTextViews(UserActivity.this, selfSolved, textSelfEasy, textSelfMedium, textSelfHard, textSelfExtreme, textSelfTotal);
                }

                @Override
                public void handleError(Response error) {
                    Log.e(TAG, "/UserEnigmators/" + currentUser.getId() + "/GetEnigmeDone");
                    Log.e(TAG, error.toString());
                }
            });

            httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/" + user.getId() + "/isFriend",
                    null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            btnAddUser.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            isFriend = gson.fromJson(response.getContent(), JsonObject.class)
                                    .get("isFriend").getAsBoolean();

                            if (!isFriend) {
                                btnAddUser.show();
                                btnAddUser.setEnabled(true);
                            }
                        }

                        @Override
                        public void handleError(Response error) {
                            Log.e(TAG, "/UserEnigmators/" + user.getId() + "/isFriend");
                            Log.e(TAG, error.toString());
                        }
                    });

            btnCompare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isComparing = !isComparing;
                    layoutCompare.setVisibility(isComparing ? View.VISIBLE : View.GONE);
                    btnCompare.setSupportBackgroundTintList(ColorStateList.valueOf(getResources()
                            .getColor(isComparing ? R.color.colorPrimaryDark : R.color.colorPrimary)));
                }
            });

            btnAddUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    httpManager.addToQueue(HttpRequest.POST, "/UserEnigmators/" + user.getId()
                            + "/AddAFriend", null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            btnAddUser.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            btnAddUser.hide();
                            Toast.makeText(UserActivity.this, R.string.invite_sent, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleError(Response error) {
                            btnAddUser.setEnabled(true);
                            Log.e(TAG, "/UserEnigmators/" + user.getId());
                            Log.e(TAG, error.toString());
                        }
                    });
                }
            });
        }
    }

    private static void setTextViews(Context context, List<Enigma> enigmas, TextView easy, TextView medium, TextView hard, TextView extreme, TextView total) {
        int easyCount = 0, mediumCount = 0, hardCount = 0, extremeCount = 0;
        for (Enigma e : enigmas) {
            int score = e.getScoreReward();
            if (score < Enigma.MEDIUM_THRESHOLD) easyCount++;
            else if (score < Enigma.HARD_THRESHOLD) mediumCount++;
            else if (score < Enigma.EXTREME_THRESHOLD) hardCount++;
            else extremeCount++;
        }

        easy.setText(context.getString(R.string.easy_solved, easyCount));
        medium.setText(context.getString(R.string.medium_solved, mediumCount));
        hard.setText(context.getString(R.string.hard_solved, hardCount));
        extreme.setText(context.getString(R.string.extreme_solved, extremeCount));
        total.setText(context.getString(R.string.total_solved, enigmas.size()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
