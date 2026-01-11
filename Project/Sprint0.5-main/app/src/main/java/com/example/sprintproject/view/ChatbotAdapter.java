package com.example.sprintproject.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sprintproject.R;
import com.example.sprintproject.model.ChatModel;

import java.util.List;

public class ChatbotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatModel> list;

    public ChatbotAdapter(List<ChatModel> list) {
        this.list = list;
    }

    // Return 0 if user message, return 1 for bot message
    @Override
    public int getItemViewType(int position) {
        if (list.get(position).getSender().equals("user")) {
            return 0;
        } else if (list.get(position).getSender().equals("bot")) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chatbot_user_messages, parent, false);
            return new UserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chatbot_bot_messages, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel model = list.get(position);

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userText.setText(model.getMessage());
            ((UserViewHolder) holder).userTimeText.setText(model.getTimestamp());
        } else if (holder instanceof BotViewHolder) {
            ((BotViewHolder) holder).botText.setText(model.getMessage());
            ((BotViewHolder) holder).botTimeText.setText(model.getTimestamp());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userText;
        private TextView userTimeText;

        public UserViewHolder(View itemView) {
            super(itemView);
            userText = itemView.findViewById(R.id.user_message);
            userTimeText = itemView.findViewById(R.id.user_timestamp);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        private TextView botText;
        private TextView botTimeText;

        public BotViewHolder(View itemView) {
            super(itemView);
            botText = itemView.findViewById(R.id.bot_message);
            botTimeText = itemView.findViewById(R.id.bot_timestamp);
        }
    }
}
