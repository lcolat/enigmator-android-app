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
import com.example.enigmator.entity.Response;
import com.example.enigmator.entity.UserEnigmator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment implements EmptyView {
    private List<UserEnigmator> mFriends, mOthers;
    private ProgressBar mProgressBar;
    private LinearLayout mLayout;
    private OnListFragmentInteractionListener mListener;
    private UserRecyclerViewAdapter mFriendsAdapter, mOthersAdapter;

    private RecyclerView listFriends;
    private TextView texteEmpty;

    private HttpManager httpManager;
    private int userId;
    private Gson gson;

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
        gson = new Gson();
        UserEnigmator currentUser = UserEnigmator.getCurrentUser(getContext());
        if (currentUser == null) {
            throw new IllegalStateException("User cannot be null");
        } else {
            userId = currentUser.getId();
        }

        mFriendsAdapter = new UserRecyclerViewAdapter(mFriends, mListener);
        mOthersAdapter = new UserRecyclerViewAdapter(mOthers, mListener);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        final EditText searchUser = view.findViewById(R.id.edit_search);
        listFriends = view.findViewById(R.id.list_friends);
        RecyclerView others = view.findViewById(R.id.list_search_user);
        listFriends.setAdapter(mFriendsAdapter);
        listFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        others.setAdapter(mOthersAdapter);
        others.setLayoutManager(new LinearLayoutManager(getContext()));

        texteEmpty = view.findViewById(R.id.text_empty);

        mLayout = view.findViewById(R.id.layout_user_fragment);
        mProgressBar = view.findViewById(R.id.progress_loading);
        final ImageButton button = view.findViewById(R.id.btn_search);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String searched = searchUser.getText().toString();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators?filter={\"where\":{\"username\":{\"like\":\""
                                + searched +"%\",\"options\":\"i\"}}}",
                        null, new HttpRequest.HttpRequestListener() {
                    @Override
                    public void prepareRequest() {
                        button.setEnabled(false);
                    }

                    @Override
                    public void handleSuccess(Response response) {
                        button.setEnabled(true);
                        if (response.getStatusCode() != 204) {
                            mOthers = Arrays.asList(gson.fromJson(response.getContent(), UserEnigmator[].class));
                        }
                        mOthersAdapter.setValues(mOthers);
                        mOthersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void handleError(Response error) {
                        button.setEnabled(true);
                        Log.e(UserFragment.class.getName(), "Error: " + error);
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
            httpManager.addToQueue(HttpRequest.GET, "/UserEnigmators/GetMyFriend", null, new HttpRequest.HttpRequestListener() {
                @Override
                public void prepareRequest() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mLayout.setVisibility(View.GONE);
                }

                @Override
                public void handleSuccess(Response response) {
                    mProgressBar.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);

                    if (response.getStatusCode() != 204) {
                        mFriends = Arrays.asList(gson.fromJson(response.getContent(), UserEnigmator[].class));
                    }

                    mFriendsAdapter.setValues(mFriends);
                    mFriendsAdapter.notifyDataSetChanged();
                }

                @Override
                public void handleError(Response error) {
                    mProgressBar.setVisibility(View.GONE);
                    mLayout.setVisibility(View.VISIBLE);
                    Log.e(UserFragment.class.getName(), "Error: " + error);
                }
            });
        }

        return view;
    }

    @Override
    public void manageEmpty() {
        texteEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void manageNotEmpty() {
        texteEmpty.setVisibility(View.GONE);
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
