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
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.activity.TopicActivity;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.TopicRecyclerViewAdapter;
import com.example.enigmator.entity.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ForumFragment extends Fragment {
    private OnListFragmentInteractionListener mListener;
    private HttpManager httpManager;
    private List<Topic> topTopics, searchedTopics;
    private TopicRecyclerViewAdapter adapter;

    private ImageButton button;
    private TextView listTitle;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof UserFragment.OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(View view, Topic topic) {
                    Intent intent = new Intent(getContext(), TopicActivity.class);
                    intent.putExtra(TopicActivity.TOPIC_ID_KEY, topic.getId());
                    intent.putExtra(TopicActivity.TOPIC_TITLE_KEY, topic.getEnigmaTitle());
                    startActivity(intent);
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

        topTopics.add(new Topic("Une miche pour deux", 2, 432)); //TODO: remove

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
        recyclerView = v.findViewById(R.id.list_topics);
        progressBar = v.findViewById(R.id.progress_loading);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searched = searchTopic.getText().toString();
                if (searched.length() > 0) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    // TODO: change route
                    httpManager.addToQueue(HttpRequest.GET, "/topics", null, new HttpRequest.HttpRequestListener() {
                        @Override
                        public void prepareRequest() {
                            progressBar.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            button.setEnabled(false);
                        }

                        @Override
                        public void handleSuccess(String result) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            listTitle.setText(R.string.results);
                            button.setEnabled(true);

                            // TODO: add to searched
                            searchedTopics.add(new Topic("Searched", 3, 543));
                            adapter.setValues(searchedTopics);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void handleError(String error) {
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            button.setEnabled(true);
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

        // TODO: change route
        if (topTopics.isEmpty()) {
            httpManager.addToQueue(HttpRequest.GET, "/topics/top", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    button.setEnabled(false);
                }

                @Override
                public void handleSuccess(String result) {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    button.setEnabled(true);

                    // TODO: add topics

                    adapter.setValues(topTopics);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void handleError(String error) {
                    progressBar.setVisibility(View.GONE);
                    button.setEnabled(true);
                }
            });
        }

        return v;
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(View view, Topic topic);
    }
}
