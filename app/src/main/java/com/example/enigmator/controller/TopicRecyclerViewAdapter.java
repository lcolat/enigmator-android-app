package com.example.enigmator.controller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.enigmator.R;
import com.example.enigmator.entity.Topic;
import static com.example.enigmator.fragment.ForumFragment.OnListFragmentInteractionListener;

import java.util.List;
import java.util.Locale;

public class TopicRecyclerViewAdapter extends RecyclerView.Adapter<TopicRecyclerViewAdapter.ViewHolder> {
    private List<Topic> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Locale mLocale;

    public TopicRecyclerViewAdapter(List<Topic> items, OnListFragmentInteractionListener listener, Locale locale) {
        super();
        mValues = items;
        mListener = listener;
        mLocale = locale;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.topic_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Topic topic = mValues.get(position);
        holder.mItem = topic;
        holder.mTitle.setText(topic.getEnigmaTitle());
        holder.mMessageAmount.setText(String.format(mLocale,"%d",topic.getMessageCount()));

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

    public void setValues(List<Topic> values) {
        this.mValues = values;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mTitle;
        private final TextView mMessageAmount;
        private Topic mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.text_topic_title);
            mMessageAmount = view.findViewById(R.id.text_message_count);
        }
    }
}
