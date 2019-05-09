package com.example.enigmator.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.enigmator.R;
import com.example.enigmator.controller.HttpAsyncTask;
import com.example.enigmator.controller.MessageRecyclerViewAdapter;
import com.example.enigmator.entity.Message;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends HttpActivity {
    private List<Message> messages;
    private MessageRecyclerViewAdapter mAdapter;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        UserEnigmator user = (UserEnigmator) getIntent().getSerializableExtra(UserActivity.USER_KEY);
        setTitle(user.getUsername());

        messages = new ArrayList<>();
        gson = new Gson();

        RecyclerView recyclerView = findViewById(R.id.list_messages);
        mAdapter = new MessageRecyclerViewAdapter(messages,
                getResources().getColor(R.color.message_sent), getResources().getColor(R.color.message_received));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final EditText editText = findViewById(R.id.edit_chat);
        ImageButton button = findViewById(R.id.btn_send_message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnected()) {
                    Editable editable = editText.getText();
                    String content = editable.toString();
                    if (content.length() > 0) {
                        editable.clear();

                        // TODO: post Message
                        Message message = new Message(content);
                        httpAsyncTask = new HttpAsyncTask(ChatActivity.this, HttpAsyncTask.POST, "/messages", gson.toJson(message));
                        httpAsyncTask.execute();
                    }
                } else {
                    buildNoConnectionErrorDialog(null).show();
                }
            }
        });

        // TODO: change route
        httpAsyncTask = new HttpAsyncTask(this, HttpAsyncTask.GET, "/messages", null);
        httpAsyncTask.execute();
    }

    /**
     * Do some UI updates to show that a Http request will be performed
     */
    @Override
    public void prepareRequest() {

    }

    /**
     * Update the UI and handle the HTTP response.
     *
     * @param result The HTTP response
     */
    @Override
    public void handleSuccess(String result) {
        // TODO: update message List
        messages.add(new Message("Hello"));

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void handleError(String error) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
