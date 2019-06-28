package com.example.enigmator.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class UserActivity extends HttpActivity {
    public static final String USER_KEY = "user_key";
    public static final String IS_SELF_KEY = "is_self_key";

    private FloatingActionButton button;
    private boolean isFriend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        final UserEnigmator user = (UserEnigmator) intent.getSerializableExtra(USER_KEY);
        boolean isSelfProfile = intent.getBooleanExtra(IS_SELF_KEY, false);

        String username = user.getUsername();
        setTitle(username);
        // TODO: display stats

        button = findViewById(R.id.btn_user_action);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            button.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }

        if (isSelfProfile) {
            button.setImageResource(R.drawable.ic_edit_white_24dp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: editable form
                    Toast.makeText(UserActivity.this, "Edit profile", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/" + user.getId() + "/isFriend",
                    null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            button.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            isFriend = new Gson().fromJson(response.getContent(), JsonObject.class)
                                    .get("isFriend").getAsBoolean();

                            button.setEnabled(true);

                            if (isFriend) {
                                button.setImageResource(R.drawable.ic_chat_white_24dp);
                            }
                        }

                        @Override
                        public void handleError(Response error) {

                        }
                    });

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isFriend) {
                        Intent intent = new Intent(UserActivity.this, ChatActivity.class);
                        intent.putExtra(USER_KEY, user);
                        startActivity(intent);
                    } else {
                        httpManager.addToQueue(HttpRequest.POST, "/UserEnigmators/" + user.getId()
                                + "/AddAFriend", null, new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {
                                button.setEnabled(false);
                            }

                            @Override
                            public void handleSuccess(Response response) {
                                button.setEnabled(true);
                                button.setImageResource(R.drawable.ic_chat_white_24dp);
                            }

                            @Override
                            public void handleError(Response error) {
                                button.setEnabled(true);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
