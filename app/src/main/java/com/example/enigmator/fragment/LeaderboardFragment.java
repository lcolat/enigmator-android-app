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
import com.example.enigmator.activity.IHttpComponent;
import com.example.enigmator.activity.UserActivity;
import com.example.enigmator.controller.HttpAsyncTask;
import com.example.enigmator.controller.UserRecyclerViewAdapter;
import com.example.enigmator.entity.UserEnigmator;
import com.example.enigmator.fragment.UserFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends Fragment implements IHttpComponent {
    private OnListFragmentInteractionListener mListener;
    private ProgressBar mProgressBar;
    private UserRecyclerViewAdapter mAdapter;
    private List<UserEnigmator> mFriends, mAllUsers;
    private UserEnigmator currentUser;

    private HttpAsyncTask httpAsyncTask;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
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
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriends = new ArrayList<>();
        mAllUsers = new ArrayList<>();

        UserEnigmator currentUser = UserEnigmator.getCurrentUser(getContext());
        if (currentUser == null)
            throw new IllegalStateException("User cannot be null");

        // TODO: get users
        mAllUsers.add(new UserEnigmator(24, 353, "John", "user", new Date(), "John"));
        // TODO: update asynctask with Queue<Task> (OnTaskCompleteListenet {void prepareRequest; void handleSuccess; void handleError}

        // TODO get friends
        mFriends.add(new UserEnigmator(1, 1, "Theo", "admin", new Date(),
                "Kalfaa"));

        httpAsyncTask = new HttpAsyncTask(this, HttpAsyncTask.GET, "/users/" + currentUser.getId() + "/friends", null);
        //httpAsyncTask.execute();

        mAdapter = new UserRecyclerViewAdapter(mAllUsers, mListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        RecyclerView leaderboard = view.findViewById(R.id.list_leaderboard);
        leaderboard.setAdapter(mAdapter);
        leaderboard.setLayoutManager(new LinearLayoutManager(getContext()));

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    mAdapter.setValues(mAllUsers);
                } else {
                    mAdapter.setValues(mFriends);
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
        userRank.setText(getString(R.string.rank, currentUser.getClassement()));
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
        return view;
    }

    /**
     * Do some UI updates to show that a Http request will be performed
     */
    @Override
    public void prepareRequest() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Update the UI and handle the HTTP response.
     *
     * @param result The HTTP response
     */
    @Override
    public void handleSuccess(String result) {
        mProgressBar.setVisibility(View.GONE);

        //TODO
        Log.d(UserFragment.class.getName(), "Success: " + result);

        mAllUsers.add(new UserEnigmator(31, 345, "Michel", "normal", new Date(),
                "ForeverTonight"));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void handleError(String error) {
        mProgressBar.setVisibility(View.GONE);
        Log.e(UserFragment.class.getName(), "Error: " + error);
    }
}
