package com.example.enigmator.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.PostRecyclerViewAdapter;
import com.example.enigmator.entity.Post;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TopicActivity extends HttpActivity {
    private static final String TAG = TopicActivity.class.getName();

    public static final String TOPIC_ID_KEY = "topic_id_key";
    public static final String TOPIC_TITLE_KEY  = "topic_title_key";

    private ProgressBar progressBar;
    private List<Post> posts;
    private PostRecyclerViewAdapter mAdapter;
    private UserEnigmator currentUser;

    private int enigmaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        currentUser = UserEnigmator.getCurrentUser(this);

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
        final TextView textEmpty = findViewById(R.id.text_empty);

        posts = new ArrayList<>();
        mAdapter = new PostRecyclerViewAdapter(posts);
        RecyclerView postsRecyclerView = findViewById(R.id.list_posts);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsRecyclerView.setAdapter(mAdapter);

        ImageButton btnOpenEnigma = findViewById(R.id.btn_open_enigma);
        btnOpenEnigma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enigmaIntent = new Intent(TopicActivity.this, EnigmaActivity.class);
                enigmaIntent.putExtra(EnigmaActivity.ENIGMA_ID_KEY, enigmaId);
                startActivity(enigmaIntent);
            }
        });

        final EditText editPost = findViewById(R.id.edit_post);
        final ImageButton btnLink = findViewById(R.id.btn_send_post);
        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnected()) {
                    Editable editable = editPost.getText();
                    String content = editable.toString();
                    if (content.length() > 0) {
                        editable.clear();

                        final Post post = new Post(0, content, new Date(), currentUser);
                        httpManager.addToQueue(HttpRequest.POST, "/Messages", gson.toJson(post), new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {}

                            @Override
                            public void handleSuccess(Response response) {
                                posts.add(post);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void handleError(Response error) {
                                Log.e(TAG, "/Messages : " + gson.toJson(post));
                                Log.e(TAG, error.toString());
                            }
                        });
                    }
                } else {
                    buildNoConnectionErrorDialog(null).show();
                }
            }
        });

        // TODO: change route
        // Get Enigma for topic
        /*
        httpManager.addToQueue(HttpRequest.GET, "/", null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {

            }

            @Override
            public void handleSuccess(Response response) {
                btnLink.setVisibility(View.VISIBLE);
                enigmaId = gson.fromJson(response.getContent(), JsonObject.class)
                        .get("id").getAsInt();
            }

            @Override
            public void handleError(Response error) {
                Log.e(TAG, "/Messages");
                Log.e(TAG, error.toString());
            }
        });*/

        //  Get all Posts for topic
        httpManager.addToQueue(HttpRequest.GET, "/Messages?filter[where][topicId]=" + topicId+
                "&filter[order]=creationDate%20ASC&filter[include]=user", null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void handleSuccess(Response response) {
                progressBar.setVisibility(View.GONE);
                if (response.getStatusCode() != 204) {
                    List<Post> posts = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Post[].class)));
                    mAdapter.setValues(posts);
                    mAdapter.notifyDataSetChanged();
                    textEmpty.setVisibility(View.GONE);
                } else {
                    textEmpty.setVisibility(View.VISIBLE);
                }
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
}
