package com.example.enigmator.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.activity.UserActivity;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.UserRecyclerViewAdapter;
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.example.enigmator.fragment.UserFragment.OnListFragmentInteractionListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends Fragment {
    private OnListFragmentInteractionListener mListener;
    private ProgressBar mProgressBar;
    private UserRecyclerViewAdapter mAdapter;
    private List<UserEnigmator> mFriends, mAllUsers;
    private UserEnigmator currentUser;

    private TextView textEmpty;

    private HttpManager httpManager;
    private Gson gson;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        gson = new Gson();
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            mListener = new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(View view, UserEnigmator user) {
                    Intent intent = new Intent(getContext(), UserActivity.class);
                    intent.putExtra(UserActivity.USER_KEY, user);
                    startActivity(intent);
                }
            };
        }

        currentUser = UserEnigmator.getCurrentUser(context);
        httpManager = new HttpManager(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriends = new ArrayList<>();
        mAllUsers = new ArrayList<>();
        mAdapter = new UserRecyclerViewAdapter(mAllUsers, mListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        RecyclerView leaderboard = view.findViewById(R.id.list_leaderboard);
        leaderboard.setAdapter(mAdapter);
        leaderboard.setLayoutManager(new LinearLayoutManager(getContext()));

        textEmpty = view.findViewById(R.id.text_empty_ladder);

        final TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    mAdapter.setValues(mAllUsers);
                } else {
                    mAdapter.setValues(mFriends);
                }
                if (mAdapter.isEmpty()) {
                    textEmpty.setVisibility(View.VISIBLE);
                } else {
                    textEmpty.setVisibility(View.GONE);
                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        TextView userName = view.findViewById(R.id.text_username);
        TextView userRank = view.findViewById(R.id.text_user_rank);
        userName.setText(currentUser.getUsername());
        userRank.setText(getString(R.string.rank, currentUser.getRank()));
        View selfUserItem = view.findViewById(R.id.user_item);
        selfUserItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserActivity.class);
                intent.putExtra(UserActivity.USER_KEY, currentUser);
                intent.putExtra(UserActivity.IS_SELF_KEY, true);
                startActivity(intent);
            }
        });

        mProgressBar = view.findViewById(R.id.progress_loading);

        if (mAllUsers.isEmpty()) {
            httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators?filter[order]=rank%20Desc",
                    null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void handleSuccess(Response response) {
                    mProgressBar.setVisibility(View.GONE);
                    if (response.getStatusCode() != 204) {
                        mAllUsers = Arrays.asList(gson.fromJson(response.getContent(), UserEnigmator[].class));

                        if (mAllUsers.isEmpty()) {
                            textEmpty.setVisibility(View.VISIBLE);
                        }
                        mAdapter.setValues(mAllUsers);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void handleError(Response error) {
                    mProgressBar.setVisibility(View.GONE);
                    textEmpty.setVisibility(View.VISIBLE);
                    Log.e(LeaderboardFragment.class.getName(), error.toString());
                }
            });
        }

        if (mFriends.isEmpty()) {
            httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/GetMyFriend", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                }

                @Override
                public void handleSuccess(Response response) {
                    if (response.getStatusCode() != 204) {
                        mFriends = Arrays.asList(gson.fromJson(response.getContent(), UserEnigmator[].class));
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void handleError(Response error) {
                    Log.e(LeaderboardFragment.class.getName(), error.toString());
                }
            });
        }

        return view;
    }
}
