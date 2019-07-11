package com.example.enigmator.controller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.activity.FriendInviteActivity.OnListInteractionListener;
import com.example.enigmator.entity.UserEnigmator;

import java.util.ArrayList;
import java.util.List;

public class InviteRecyclerViewAdapter extends RecyclerView.Adapter<InviteRecyclerViewAdapter.ViewHolder> {
    private final OnListInteractionListener rejectListener, acceptListener;
    private List<UserEnigmator> mValues;

    public InviteRecyclerViewAdapter(OnListInteractionListener rejectListener,
                                     OnListInteractionListener acceptListener) {
        this.rejectListener = rejectListener;
        this.acceptListener = acceptListener;
        mValues = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invite_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserEnigmator user = mValues.get(position);

        holder.mUsername.setText(user.getUsername());
        holder.mBtnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectListener.onListInteractionListener(user.getId());
            }
        });
        holder.mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptListener.onListInteractionListener(user.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setValues(List<UserEnigmator> users) {
        this.mValues = users;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mUsername;
        private final ImageButton mBtnReject;
        private final ImageButton mBtnAccept;

        private ViewHolder(View view) {
            super(view);
            mUsername = view.findViewById(R.id.text_username);
            mBtnReject = view.findViewById(R.id.btn_reject);
            mBtnAccept = view.findViewById(R.id.btn_accept);
        }
    }
}
