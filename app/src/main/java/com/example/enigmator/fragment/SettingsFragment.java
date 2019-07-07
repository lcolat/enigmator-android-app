package com.example.enigmator.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Response;

public class SettingsFragment extends PreferenceFragmentCompat {
    private HttpManager httpManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        httpManager = new HttpManager(getContext());

        Preference gdprPref = findPreference("gdpr");
        gdprPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                assert getContext() != null;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.gdpr_dialog_title);
                builder.setMessage(R.string.gdpr_dialog_message);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        // TODO: change route
                        /*
                        httpManager.addToQueue(HttpRequest.GET, "/", null, new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() { }

                            @Override
                            public void handleSuccess(Response response) {
                                Toast.makeText(getContext(), R.string.gdpr_toast_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void handleError(Response error) {
                                Toast.makeText(getContext(), "Gdpr failed", Toast.LENGTH_SHORT).show();
                            }
                        });*/
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }
}
