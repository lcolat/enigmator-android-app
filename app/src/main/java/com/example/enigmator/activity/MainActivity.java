package com.example.enigmator.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.enigmator.R;

public class MainActivity extends AppCompatActivity {
    public static final String PREF_USER = "user_pref_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            case R.id.menu_settings:
                // TODO : start Settings activity
                return true;
            case R.id.menu_disconnect:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.remove(LoginActivity.PREF_USERNAME);
                editor.remove(LoginActivity.PREF_PASSWORD);
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
}
