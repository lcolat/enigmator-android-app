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
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.MessageRecyclerViewAdapter;
import com.example.enigmator.entity.Message;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: periodically pull
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
                        final Message message = new Message(content, new Date());
                        messages.add(message);
                        httpManager.addToQueue(HttpRequest.POST, "/messages", gson.toJson(message), new HttpRequest.HttpRequestListener() {
                            @Override
                            public void prepareRequest() {

                            }

                            @Override
                            public void handleSuccess(String result) {
                                messages.add(message);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void handleError(String error) {
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } else {
                    buildNoConnectionErrorDialog(null).show();
                }
            }
        });

        // TODO: change route: get all messages
        httpManager.addToQueue(HttpRequest.GET, "/messages", null, new HttpRequest.HttpRequestListener() {
            @Override
            public void prepareRequest() {

            }

            @Override
            public void handleSuccess(String result) {
                // TODO: update message List
                messages.add(new Message("Hello", new Date()));

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void handleError(String error) {
                mAdapter.notifyDataSetChanged();
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
