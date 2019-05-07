package com.example.enigmator.controller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.entity.UserEnigmator;
import com.example.enigmator.fragment.UserFragment.OnListFragmentInteractionListener;

import java.util.List;


public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {
    private final List<UserEnigmator> mValues;
    private final OnListFragmentInteractionListener mListener;

    public UserRecyclerViewAdapter(List<UserEnigmator> items, OnListFragmentInteractionListener listener) {
        super();
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        UserEnigmator user = mValues.get(position);
        holder.mItem = user;
        holder.mUsername.setText(user.getUsername());
        holder.mUserRank.setText(holder.mView.getContext().getString(R.string.rank, user.getClassement()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(v, holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mUsername;
        private final TextView mUserRank;
        private UserEnigmator mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mUsername = view.findViewById(R.id.text_username);
            mUserRank = view.findViewById(R.id.text_user_rank);
        }
    }
}
