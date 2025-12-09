package com.lukehemmin.ai_diet_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.data.model.ChatMessage;

import io.noties.markwon.Markwon;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages = new ArrayList<>();
    private Markwon markwon;

    public ChatAdapter(Context context) {
        this.markwon = Markwon.create(context);
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message, markwon);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutAiMessage;
        LinearLayout layoutUserMessage;
        TextView txtAiMessage;
        TextView txtUserMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutAiMessage = itemView.findViewById(R.id.layout_ai_message);
            layoutUserMessage = itemView.findViewById(R.id.layout_user_message);
            txtAiMessage = itemView.findViewById(R.id.txt_ai_message);
            txtUserMessage = itemView.findViewById(R.id.txt_user_message);
        }

        public void bind(ChatMessage message, Markwon markwon) {
            if (message.isUser()) {
                layoutAiMessage.setVisibility(View.GONE);
                layoutUserMessage.setVisibility(View.VISIBLE);
                txtUserMessage.setText(message.getMessage());
            } else {
                layoutAiMessage.setVisibility(View.VISIBLE);
                layoutUserMessage.setVisibility(View.GONE);
                // AI 메시지는 마크다운으로 렌더링
                markwon.setMarkdown(txtAiMessage, message.getMessage());
            }
        }
    }
}
