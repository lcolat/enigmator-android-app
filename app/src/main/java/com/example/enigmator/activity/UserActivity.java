package com.example.enigmator.activity;

import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import com.example.enigmator.R;
import com.example.enigmator.entity.UserEnigmator;

public class UserActivity extends HttpActivity {
    public static final String USER_KEY = "user_key";

    private boolean isFriend;
    private FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        UserEnigmator user = (UserEnigmator) getIntent().getSerializableExtra(USER_KEY);
        setTitle(user.getUsername());

        button = findViewById(R.id.btn_user_action);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            button.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
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
        // TODO check if friend and change button icon
    }

    @Override
    public void handleError(String error) {
        button.setEnabled(true);
    }
}
