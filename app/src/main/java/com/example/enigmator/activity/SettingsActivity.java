package com.example.enigmator.activity;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Response;
import com.example.enigmator.fragment.EditDialogFragment;
import com.example.enigmator.fragment.SettingsFragment;

import static com.example.enigmator.controller.HttpRequest.PUT;

public class SettingsActivity extends HttpActivity implements EditDialogFragment.NoticeDialogListener {
    private static final String TAG = SettingsActivity.class.getName();

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
            // TODO: request update password
            System.out.println("Password: " + edited);
            /*
            httpManager.addToQueue(PUT, "", "password", new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {

                }

                @Override
                public void handleSuccess(Response response) {
                    Toast.makeText(SettingsActivity.this, R.string.toast_password_updated, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void handleError(Response error) {
                    Log.e(TAG, error.toString());
                }
            });
            */
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
