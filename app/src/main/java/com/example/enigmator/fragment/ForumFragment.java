package com.example.enigmator.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.activity.TopicActivity;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.TopicRecyclerViewAdapter;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.Topic;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.example.enigmator.activity.TopicActivity.TOPIC_ID_KEY;
import static com.example.enigmator.activity.TopicActivity.TOPIC_TITLE_KEY;

public class ForumFragment extends Fragment {
    private static final String TAG = ForumFragment.class.getName();

    private static final int REQUEST_CODE = 4;

    private OnListFragmentInteractionListener mListener;
    private HttpManager httpManager;
    private List<Topic> topTopics, searchedTopics;
    private TopicRecyclerViewAdapter adapter;

    private ImageButton button;
    private TextView listTitle, textEmpty;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private Gson gson;

    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        gson = new Gson();
        if (context instanceof UserFragment.OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(Topic topic) {
                    Intent intent = new Intent(getContext(), TopicActivity.class);
                    intent.putExtra(TOPIC_ID_KEY, topic.getId());
                    intent.putExtra(TOPIC_TITLE_KEY, topic.getTitle());
                    startActivityForResult(intent, REQUEST_CODE);
                }
            };
        }
        httpManager = new HttpManager(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topTopics = new ArrayList<>();
        searchedTopics = new ArrayList<>();

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            locale = getResources().getConfiguration().getLocales().get(0);
        } else{
            locale = getResources().getConfiguration().locale;
        }
        adapter = new TopicRecyclerViewAdapter(topTopics, mListener, locale);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forum, container, false);

        final EditText searchTopic = v.findViewById(R.id.edit_search);
        button = v.findViewById(R.id.btn_search);
        listTitle = v.findViewById(R.id.text_forum_list);
        textEmpty = v.findViewById(R.id.text_empty);
        recyclerView = v.findViewById(R.id.list_topics);
        progressBar = v.findViewById(R.id.progress_loading);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String searched = searchTopic.getText().toString();
                if (searched.length() > 0) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    httpManager.addToQueue(HttpRequest.GET, "/Topics?filter={\"where\":{\"title\":{\"like\":\"%25"
                            + searched + "%25\",\"options\":\"i\"}}}", null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            progressBar.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            button.setEnabled(false);
                            listTitle.setText(R.string.results);
                        }

                        @Override
                        public void handleSuccess(Response response) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            button.setEnabled(true);

                            if (response.getStatusCode() != 204) {
                                searchedTopics = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Topic[].class)));
                                adapter.setValues(searchedTopics);
                                adapter.notifyDataSetChanged();
                                textEmpty.setVisibility(View.GONE);
                            }
                            if (response.getStatusCode() == 204 || searchedTopics.isEmpty()) {
                                textEmpty.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void handleError(Response error) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            if (topTopics.isEmpty()) textEmpty.setVisibility(View.VISIBLE);
                            button.setEnabled(true);
                            Log.e(TAG, "/Topics?filter={\"where\":{\"title\":{\"like\":\"%"
                                    + searched + "%\",\"options\":\"i\"}}}");
                            Log.e(TAG, error.toString());
                        }
                    });
                }
            }
        });

        searchTopic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    listTitle.setText(R.string.top_topics);
                    adapter.setValues(topTopics);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        searchTopic.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    button.performClick();
                }
                return false;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if (topTopics.isEmpty()) {
            httpManager.addToQueue(HttpRequest.GET, "/Topics?filter[order]=messagesCount%20Desc",
                    null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    button.setEnabled(false);
                }

                @Override
                public void handleSuccess(Response response) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);
                    if (response.getStatusCode() != 204) {
                        topTopics = new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent(), Topic[].class)));
                        adapter.setValues(topTopics);
                        textEmpty.setVisibility(View.GONE);
                    } else {
                        textEmpty.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void handleError(Response error) {
                    progressBar.setVisibility(View.GONE);
                    button.setEnabled(true);
                    textEmpty.setVisibility(View.VISIBLE);
                    Log.e(TAG, "/Topics?filter[order]=messagesCount%20Desc");
                    Log.e(TAG, error.toString());
                }
            });
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int topicId = data.getIntExtra(TOPIC_ID_KEY, -1);
                int postsCount = data.getIntExtra(TopicActivity.POSTS_COUNT_KEY, 0);

                if (topicId > -1 && postsCount > 0) {
                    for (Topic topic : topTopics) {
                        if (topic.getId() == topicId) {
                            topic.setMessagesCount(postsCount);
                            break;
                        }
                    }
                    for (Topic topic : searchedTopics) {
                        if (topic.getId() == topicId) {
                            topic.setMessagesCount(postsCount);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        httpManager.cancel(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Topic topic);
    }
}
