package com.example.enigmator.activity;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;

import com.example.enigmator.R;
import com.example.enigmator.fragment.EditDialogFragment;
import com.example.enigmator.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity implements EditDialogFragment.NoticeDialogListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public void onDialogPositiveClick(AppCompatDialogFragment dialog, String edited, @StringRes int title) {
        if (title == R.string.new_password) {
            // TODO: request
            System.out.println("Password: " + edited);
        }
    }
}
