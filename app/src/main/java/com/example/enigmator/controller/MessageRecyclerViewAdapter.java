package com.example.enigmator.controller;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.enigmator.R;
import com.example.enigmator.entity.Message;

import java.util.List;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {

    private final List<Message> mValues;
    @ColorInt
    private final int sentColor, receivedColor;

    public MessageRecyclerViewAdapter(List<Message> items, @ColorInt int colorSent, @ColorInt int colorReceived) {
        mValues = items;
        sentColor = colorSent;
        receivedColor = colorReceived;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Message message = mValues.get(position);
        holder.mContent.setText(message.getContent());
        holder.mHour.setText(message.getHour());

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Context context = v.getContext();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Message content", message.getContent());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, R.string.message_copied, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        if (message.isReceived()) {
            holder.mViewRight.setVisibility(View.INVISIBLE);
            holder.mContent.setBackgroundColor(receivedColor);
        } else {
            holder.mViewLeft.setVisibility(View.INVISIBLE);
            holder.mContent.setBackgroundColor(sentColor);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final View mViewLeft, mViewRight;
        private final TextView mContent;
        private final TextView mHour;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mContent = view.findViewById(R.id.textMessageContent);
            mHour = view.findViewById(R.id.textMessageHour);
            mViewLeft = view.findViewById(R.id.viewMessageLeft);
            mViewRight = view.findViewById(R.id.viewMessageRight);
        }
    }
}
