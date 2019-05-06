package com.example.enigmator.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpAsyncTask;

/**
 * This class handles network stuff.
 */
public abstract class HttpActivity extends AppCompatActivity implements IHttpActivity {
    protected HttpAsyncTask httpAsyncTask;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpAsyncTask != null) {
            httpAsyncTask.cancel(true);
        }
    }

    protected AlertDialog buildNoConnectionErrorDialog(Context context, @Nullable DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_no_connection_title);
        builder.setMessage(R.string.dialog_no_connection_message);
        builder.setPositiveButton(android.R.string.ok, listener);
        return builder.create();
    }
}
