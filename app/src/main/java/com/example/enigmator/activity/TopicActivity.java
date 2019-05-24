package com.example.enigmator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpAsyncTask;
import com.example.enigmator.controller.PostRecyclerViewAdapter;
import com.example.enigmator.entity.Post;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TopicActivity extends HttpActivity {
    public static final String TOPIC_ID_KEY = "topic_id_key";
    public static final String TOPIC_TITLE_KEY  = "topic_title_key";

    private ProgressBar progressBar;
    private List<Post> posts;
    private PostRecyclerViewAdapter mAdapter;
    private UserEnigmator currentUser;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        currentUser = UserEnigmator.getCurrentUser(this);

        gson = new Gson();

        final Intent intent = getIntent();
        final int topicId = intent.getIntExtra(TOPIC_ID_KEY, -1);
        if (topicId == -1) {
            throw new IllegalStateException("Topic id is missing");
        }
        final String title = intent.getStringExtra(TOPIC_TITLE_KEY);
        setTitle(title != null ? title : getString(R.string.forum));

        progressBar = findViewById(R.id.progress_loading);

        TextView topicTitle = findViewById(R.id.text_topic_title);
        topicTitle.setText(title);

        posts = new ArrayList<>();
        mAdapter = new PostRecyclerViewAdapter(posts);
        RecyclerView postsRecyclerView = findViewById(R.id.list_posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(mAdapter);

        ImageButton btnOpenEnigma = findViewById(R.id.btn_open_enigma);
        btnOpenEnigma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*TODO show enigma
                Intent enigmaIntent = new Intent(TopicActivity.this, EnigmaActitivy.class);
                enigmaIntent.putExtra(, topicId);
                startActivity(enigmaIntent);*/
            }
        });

        final EditText editPost = findViewById(R.id.edit_post);
        ImageButton button = findViewById(R.id.btn_send_post);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnected()) {
                    Editable editable = editPost.getText();
                    String content = editable.toString();
                    if (content.length() > 0) {
                        editable.clear();

                        // TODO: post Post
                        Post message = new Post(currentUser, content, new Date());
                        httpAsyncTask = new HttpAsyncTask(TopicActivity.this, HttpAsyncTask.POST, "/posts", gson.toJson(message));
                        posts.add(message);
                        httpAsyncTask.execute();
                    }
                } else {
                    buildNoConnectionErrorDialog(null).show();
                }
            }
        });
        // TODO:
        httpAsyncTask = new HttpAsyncTask(this, HttpAsyncTask.GET, "/posts/" + topicId, null);
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
        // TODO:
        UserEnigmator userEnigmator = new UserEnigmator(45, 23, "John", "user", new Date(), "John Doe");
        posts.add(new Post(userEnigmator, "Cette énigme est très difficile !", new Date()));
        mAdapter.notifyDataSetChanged();
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
