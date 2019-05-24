package com.example.enigmator.controller;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.activity.UserActivity;
import com.example.enigmator.entity.Post;

import java.util.List;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.ViewHolder> {
    private List<Post> mValues;

    public PostRecyclerViewAdapter(List<Post> items) {
        super();
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Post post = mValues.get(position);
        holder.mAuthor.setText(post.getAuthor().getUsername());
        holder.mContent.setText(post.getContent());
        holder.mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserActivity.class);
                intent.putExtra(UserActivity.USER_KEY, post.getAuthor());
                v.getContext().startActivity(intent);
            }
        });
        //TODO:
        // holder.mDate.setText(post.getPostDate());
    }

    public void setValues(List<Post> values) {
        this.mValues = values;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mContent;
        private final TextView mAuthor;
        private final TextView mDate;

        private ViewHolder(View view) {
            super(view);
            mContent = view.findViewById(R.id.text_post_content);
            mAuthor = view.findViewById(R.id.text_author);
            mDate = view.findViewById(R.id.text_post_date);
        }
    }
}
