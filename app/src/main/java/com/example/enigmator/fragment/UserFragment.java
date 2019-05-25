package com.example.enigmator.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.activity.UserActivity;
import com.example.enigmator.controller.HttpManager;
import com.example.enigmator.controller.HttpRequest;
import com.example.enigmator.controller.UserRecyclerViewAdapter;
import com.example.enigmator.entity.UserEnigmator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {
    private List<UserEnigmator> mFriends, mOthers;
    private ProgressBar mProgressBar;
    private LinearLayout mLayout;
    private OnListFragmentInteractionListener mListener;
    private UserRecyclerViewAdapter mFriendsAdapter, mOthersAdapter;

    private HttpManager httpManager;
    private int userId;

    public UserFragment() {
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
        httpManager = new HttpManager(getContext());

        mFriends = new ArrayList<>();
        mOthers = new ArrayList<>();

        setRetainInstance(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserEnigmator currentUser = UserEnigmator.getCurrentUser(getContext());
        if (currentUser == null) {
            throw new IllegalStateException("User cannot be null");
        } else {
            userId = currentUser.getId();
        }

        // TODO remove
        mFriends.add(new UserEnigmator(1, 1, "Theo", "admin", new Date(),
                "Kalfaa"));

        mFriendsAdapter = new UserRecyclerViewAdapter(mFriends, mListener);
        mOthersAdapter = new UserRecyclerViewAdapter(mOthers, mListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        final EditText searchUser = view.findViewById(R.id.edit_search);
        RecyclerView friends = view.findViewById(R.id.list_friends);
        RecyclerView others = view.findViewById(R.id.list_search_user);
        friends.setAdapter(mFriendsAdapter);
        friends.setLayoutManager(new LinearLayoutManager(getContext()));
        others.setAdapter(mOthersAdapter);
        others.setLayoutManager(new LinearLayoutManager(getContext()));

        mLayout = view.findViewById(R.id.layout_user_fragment);
        mProgressBar = view.findViewById(R.id.progress_loading);
        final ImageButton button = view.findViewById(R.id.btn_search);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String searched = searchUser.getText().toString();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // TODO: search for users, change route
                httpManager.addToQueue(HttpRequest.POST, "/friends", searched, new HttpRequest.HttpRequestListener() {
                    @Override
                    public void prepareRequest() {
                        button.setEnabled(false);
                    }

                    @Override
                    public void handleSuccess(String result) {
                        button.setEnabled(true);
                        // TODO: handle success
                        mOthers.add(new UserEnigmator(31,1213, "Paul", "user", new Date(), "User Searched"));
                        mOthersAdapter.setValues(mOthers);
                        mOthersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void handleError(String error) {
                        button.setEnabled(true);
                    }
                });
            }
        });

        searchUser.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    button.performClick();
                }
                return false;
            }
        });

        if (mFriends.isEmpty()) {
            // TODO: change route
            httpManager.addToQueue(HttpRequest.GET, "/users/" + userId + "/friends", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mLayout.setVisibility(View.GONE);
                }

                @Override
                public void handleSuccess(String result) {
                    mProgressBar.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);

                    //TODO: handle results
                    mFriends.add(new UserEnigmator(31, 345, "Michel", "normal", new Date(),
                            "ForeverTonight"));
                    mFriendsAdapter.notifyDataSetChanged();
                }

                @Override
                public void handleError(String error) {
                    mProgressBar.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);
                    Log.e(UserFragment.class.getName(), "Error: " + error);
                }
            });
        }

        return view;
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
        void onListFragmentInteraction(View view, UserEnigmator user);
    }
}
