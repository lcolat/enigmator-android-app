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
import com.example.enigmator.entity.UserEnigmator;

public class UserActivity extends HttpActivity {
    public static final String USER_KEY = "user_key";

    private boolean isFriend;
    private FloatingActionButton button;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        final UserEnigmator user = (UserEnigmator) getIntent().getSerializableExtra(USER_KEY);
        username = user.getUsername();
        setTitle(username);
        // TODO: display stats

        button = findViewById(R.id.btn_user_action);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            button.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: remove nextLine
                isFriend = true;
                if (isFriend) {
                    Intent intent = new Intent(UserActivity.this, ChatActivity.class);
                    intent.putExtra(USER_KEY, user);
                    startActivity(intent);
                } else {
                    // TODO: send invite
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    /**
     * Do some UI updates to show that a Http request will be performed
     */
    @Override
    public void prepareRequest() {
        button.setEnabled(false);
    }

    /**
     * Update the UI and handle the HTTP response.
     *
     * @param result The HTTP response
     */
    @Override
    public void handleSuccess(String result) {
        button.setEnabled(true);
        button.setImageResource(R.drawable.ic_chat_white_24dp);
    }

    @Override
    public void handleError(String error) {
        button.setEnabled(true);
    }
}
