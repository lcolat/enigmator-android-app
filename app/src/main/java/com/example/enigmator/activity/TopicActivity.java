package com.example.enigmator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpAsyncTask;

public class TopicActivity extends HttpActivity {
    public static final String TOPIC_ID_KEY = "topic_id_key";
    public static final String TOPIC_TITLE_KEY  = "topic_title_key";

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Intent intent = getIntent();
        final int topicId = intent.getIntExtra(TOPIC_ID_KEY, -1);
        if (topicId == -1) {
            throw new IllegalStateException("Topic id is missing");
        }
        final String title = intent.getStringExtra(TOPIC_TITLE_KEY);
        setTitle(title != null ? title : getString(R.string.forum));

        progressBar = findViewById(R.id.progress_loading);

        // TODO:
        httpAsyncTask = new HttpAsyncTask(this, HttpAsyncTask.GET, "/forum/" + topicId, null);
        //httpAsyncTask.execute();
    }

    /**
     * Do some UI updates to show that a Http request will be performed
     */
    @Override
    public void prepareRequest() {
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Update the UI and handle the HTTP response.
     *
     * @param result The HTTP response
     */
    @Override
    public void handleSuccess(String result) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void handleError(String error) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
