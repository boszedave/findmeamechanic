package com.sze.findmeamechanic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.models.Message;

public class MessageAdapter extends FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> {
    String userId;
    String userNameText;
    private final int MESSAGE_IN_VIEW_TYPE = 1;
    private final int MESSAGE_OUT_VIEW_TYPE = 2;

    public MessageAdapter(@NonNull FirestoreRecyclerOptions<Message> options, String userID, String userName) {
        super(options);
        this.userId = userID;
        this.userNameText = userName;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageHolder holder, int position, @NonNull Message model) {
        holder.textMessage.setText(model.getMessageText());
        holder.userName.setText(userNameText);
        holder.sentTime.setText(model.getMessageTime());
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getMessageUserId().equals(userId)) {
            return MESSAGE_OUT_VIEW_TYPE;
        }
        return MESSAGE_IN_VIEW_TYPE;
    }

    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == MESSAGE_IN_VIEW_TYPE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_in, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_out, parent, false);
        }
        return new MessageHolder(view);
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        TextView userName;
        TextView sentTime;

        public MessageHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_view_desc_chat);
            userName = itemView.findViewById(R.id.text_view_title_chat);
            sentTime = itemView.findViewById(R.id.text_view_post_date_chat);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }
}