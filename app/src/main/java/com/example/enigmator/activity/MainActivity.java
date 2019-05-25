package com.example.enigmator.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.UserEnigmator;

public class MainActivity extends HttpActivity {
    // Shared Preferences
    public static final String PREF_USER = "pref_user";

    // Intent
    static final String USER_ID_KEY = "user_id_key";

    private ProgressBar loadingBar;
    private ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layout_main);
        loadingBar = findViewById(R.id.progress_loading);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String user = prefs.getString(PREF_USER, null);
        if (user == null) {
            int id = getIntent().getIntExtra(USER_ID_KEY, -1);
            if (id < 0) {
                finish();
            } else {
                httpManager.addToQueue(HttpRequest.GET, "/userEnigmators/" + id, null,
                        new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {
                                layout.setVisibility(View.GONE);
                                loadingBar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void handleSuccess(String result) {
                                loadingBar.setVisibility(View.GONE);
                                layout.setVisibility(View.VISIBLE);

                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                                editor.putString(PREF_USER, result);
                                editor.apply();

                                setUpButtons();
                            }

                            @Override
                            public void handleError(String error) {
                                Toast.makeText(MainActivity.this, "Cannot get User", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
        } else {
            setUpButtons();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_profile:
                Intent intent = new Intent(this, UserActivity.class);
                intent.putExtra(UserActivity.USER_KEY, UserEnigmator.getCurrentUser(this));
                intent.putExtra(UserActivity.IS_SELF_KEY, true);
                startActivity(intent);
                return true;
            case R.id.menu_settings:
                // TODO : start Settings activity
                return true;
            case R.id.menu_disconnect:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.remove(HttpManager.PREF_USER_TOKEN);
                editor.remove(PREF_USER);
                editor.apply();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void setUpButtons() {
        final Button btnUsers = findViewById(R.id.btn_users);
        final Button btnEnigma = findViewById(R.id.btn_enigma);
        final Button btnLeaderboard = findViewById(R.id.btn_leaderboard);
        final Button btnForum = findViewById(R.id.btn_forum);

        final Intent intent = new Intent(this, CategoriesActivity.class);

        btnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(CategoriesActivity.INITIAL_FRAGMENT_KEY, R.id.navigation_users);
                startActivity(intent);
            }
        });

        btnEnigma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(CategoriesActivity.INITIAL_FRAGMENT_KEY, R.id.navigation_enigma);
                startActivity(intent);
            }
        });

        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(CategoriesActivity.INITIAL_FRAGMENT_KEY, R.id.navigation_leaderboard);
                startActivity(intent);
            }
        });

        btnForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(CategoriesActivity.INITIAL_FRAGMENT_KEY, R.id.navigation_forum);
                startActivity(intent);
            }
        });
    }
}
