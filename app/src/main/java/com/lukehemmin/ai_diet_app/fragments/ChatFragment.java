package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;

public class ChatFragment extends Fragment {

    private RecyclerView rvChatMessages;
    private EditText etChatInput;
    private ImageView btnSendMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        
        initializeViews(view);
        setupListeners();
        loadInitialMessages();
        
        return view;
    }

    private void initializeViews(View view) {
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        etChatInput = view.findViewById(R.id.et_chat_input);
        btnSendMessage = view.findViewById(R.id.btn_send_message);

        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadInitialMessages() {
        // TODO: Load initial greeting message
    }

    private void sendMessage() {
        String message = etChatInput.getText().toString().trim();
        if (!message.isEmpty()) {
            // TODO: Send message and get AI response
            etChatInput.setText("");
        }
    }
}
