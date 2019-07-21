package com.example.enigmator.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.entity.Response;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = SettingsFragment.class.getName();

    private HttpManager httpManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        httpManager = new HttpManager(getContext());

        Preference usernamePref = findPreference("username");
        usernamePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Bundle bundle = new Bundle();
                bundle.putInt(EditDialogFragment.STRING_RESOURCE, R.string.new_username);
                EditDialogFragment dialogFragment = new EditDialogFragment();
                dialogFragment.setArguments(bundle);

                assert getFragmentManager() != null;
                dialogFragment.show(getFragmentManager(), EditDialogFragment.class.getName());

                return true;
            }
        });

        Preference passwordPref = findPreference("password");
        passwordPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Bundle bundle = new Bundle();
                bundle.putInt(EditDialogFragment.STRING_RESOURCE, R.string.new_password);
                EditDialogFragment dialogFragment = new EditDialogFragment();
                dialogFragment.setArguments(bundle);

                assert getFragmentManager() != null;
                dialogFragment.show(getFragmentManager(), EditDialogFragment.class.getName());

                return true;
            }
        });

        Preference getGdprPref = findPreference("gdpr_get");
        getGdprPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                builder.setTitle(R.string.gdpr_dialog_title);
                builder.setMessage(R.string.gdpr_dialog_message);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/SendMailWithData", null, new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() { }

                            @Override
                            public void handleSuccess(Response response) {
                                Toast.makeText(getContext(), R.string.gdpr_toast_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void handleError(Response error) {
                                Toast.makeText(getContext(), R.string.request_failed, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "/UserEnigmators/SendMailWithData");
                                Log.e(TAG, error.toString());
                            }
                        });
                    }
                });
                builder.create().show();
                return true;
            }
        });

        Preference deleteAccountPref = findPreference("delete_account");
        deleteAccountPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(preference.getContext());
                builder.setTitle(R.string.delete_account_title);
                builder.setMessage(R.string.delete_account_summary);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        httpManager.addToQueue(HttpRequest.DELETE, "/UserEnigmators/DeleteAccount", null, new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() { }

                            @Override
                            public void handleSuccess(Response response) {
                                Activity activity = getActivity();
                                assert activity != null;

                                activity.setResult(Activity.RESULT_OK);
                                activity.finish();
                            }

                            @Override
                            public void handleError(Response error) {
                                Toast.makeText(getContext(), R.string.request_failed, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "/UserEnigmators/DeleteAccount");
                                Log.e(TAG, error.toString());
                            }
                        });
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }
}
