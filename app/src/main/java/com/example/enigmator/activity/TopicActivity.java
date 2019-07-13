package com.example.enigmator.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.PostRecyclerViewAdapter;
import com.example.enigmator.entity.Enigma;
import com.example.enigmator.entity.Post;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TopicActivity extends HttpActivity {
    private static final String TAG = TopicActivity.class.getName();

    public static final String POSTS_COUNT_KEY = "posts_count_key";
    public static final String TOPIC_ID_KEY = "topic_id_key";
    public static final String TOPIC_TITLE_KEY  = "topic_title_key";

    private ProgressBar progressBar;

    private List<Post> posts;
    private PostRecyclerViewAdapter mAdapter;
    private UserEnigmator currentUser;

    private int topicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        currentUser = UserEnigmator.getCurrentUser(this);

        final Intent intent = getIntent();
        topicId = intent.getIntExtra(TOPIC_ID_KEY, -1);
        if (topicId == -1) {
            throw new IllegalStateException("Topic id is missing");
        }

        final String title = intent.getStringExtra(TOPIC_TITLE_KEY);
        setTitle(title != null ? title : getString(R.string.forum));

        progressBar = findViewById(R.id.progress_loading);

        TextView topicTitle = findViewById(R.id.text_topic_title);
        topicTitle.setText(title);
        final TextView textEmpty = findViewById(R.id.text_empty);

        posts = new ArrayList<>();
        RecyclerView postsRecyclerView = findViewById(R.id.list_posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PostRecyclerViewAdapter(posts);
        postsRecyclerView.setAdapter(mAdapter);

        final ImageButton btnOpenEnigma = findViewById(R.id.btn_open_enigma);

        final EditText editPost = findViewById(R.id.edit_post);
        final ImageButton btnSendPost = findViewById(R.id.btn_send_post);
        btnSendPost.setEnabled(false);
        btnSendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnected()) {
                    Editable editable = editPost.getText();
                    final String content = editable.toString();
                    if (content.length() > 0) {
                        editable.clear();

                        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        // Add post
                        final Post post = new Post(0, content, new Date(), currentUser);
                        httpManager.addToQueue(HttpRequest.POST, "/Topics/" + topicId + "/PostAMessage",
                                "{\"content\": \"" + content + "\"}", new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {
                                btnSendPost.setEnabled(false);
                            }

                            @Override
                            public void handleSuccess(Response response) {
                                btnSendPost.setEnabled(true);

                                if (posts.isEmpty()) {
                                    textEmpty.setVisibility(View.GONE);
                                    posts.add(post);
                                    mAdapter.setValues(posts);
                                } else {
                                    posts.add(post);
                                }

                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void handleError(Response error) {
                                btnSendPost.setEnabled(true);

                                Log.e(TAG, "/Messages : " + "{\"content\": \"" + content + "\"}");
                                Log.e(TAG, error.toString());
                            }
                        });
                    }
                } else {
                    buildNoConnectionErrorDialog(null).show();
                }
            }
        });

        // Get Enigma for topic
        httpManager.addToQueue(HttpRequest.GET, "/Enigmes?filter[where][topicId]=" + topicId, null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleSuccess(Response response) {
                btnOpenEnigma.setVisibility(View.VISIBLE);
                final Enigma enigma = gson.fromJson(gson.fromJson(response.getContent(), JsonArray.class).get(0), Enigma.class);

                btnOpenEnigma.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent enigmaIntent = new Intent(TopicActivity.this, EnigmaActivity.class);
                        enigmaIntent.putExtra(EnigmaActivity.ENIGMA_KEY, enigma);
                        startActivity(enigmaIntent);
                    }
                });
            }

            @Override
            public void handleError(Response error) {
                Log.e(TAG, "/Enigmes?filter[where][topicId]=" + topicId);
                Log.e(TAG, error.toString());
            }
        });

        //  Get all Posts for topic
        httpManager.addToQueue(HttpRequest.GET, "/Messages?filter[where][topicId]=" + topicId+
                "&filter[order]=creationDate%20ASC&filter[include]=user", null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleSuccess(Response response) {
                if (response.getStatusCode() != 204) {
                    posts = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Post[].class)));

                    if (posts.isEmpty()) {
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        mAdapter.setValues(posts);
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    textEmpty.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
                btnSendPost.setEnabled(true);
            }

            @Override
            public void handleError(Response error) {
                progressBar.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "/Messages?filter[where][topicId]=" + topicId + "&filter[order]=creationDate%20ASC&filter[include]=user");
                Log.e(TAG, error.toString());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(TOPIC_ID_KEY, topicId);
        returnIntent.putExtra(POSTS_COUNT_KEY, posts.size());
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }
}
