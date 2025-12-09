package com.lukehemmin.ai_diet_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lukehemmin.ai_diet_app.R;
import com.lukehemmin.ai_diet_app.adapters.ChatAdapter;
import com.lukehemmin.ai_diet_app.data.model.ApiResponse;
import com.lukehemmin.ai_diet_app.data.model.ChatHistoryResponse;
import com.lukehemmin.ai_diet_app.data.model.ChatMessage;
import com.lukehemmin.ai_diet_app.data.model.ChatRequest;
import com.lukehemmin.ai_diet_app.data.model.ChatResponse;
import com.lukehemmin.ai_diet_app.network.ApiService;
import com.lukehemmin.ai_diet_app.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView rvChatMessages;
    private EditText etChatInput;
    private ImageView btnSendMessage;
    private ChatAdapter chatAdapter;
    private ApiService apiService;

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
        chatAdapter = new ChatAdapter(requireContext());
        rvChatMessages.setAdapter(chatAdapter);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
    }

    private void setupListeners() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadInitialMessages() {
        // 서버에서 이전 대화 기록 로드
        apiService.getChatHistory(50).enqueue(new Callback<ApiResponse<List<ChatHistoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatHistoryResponse>>> call, 
                                   Response<ApiResponse<List<ChatHistoryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<ChatHistoryResponse> histories = response.body().getData();
                    if (histories != null && !histories.isEmpty()) {
                        // 이전 대화 표시
                        for (ChatHistoryResponse history : histories) {
                            chatAdapter.addMessage(new ChatMessage(history.getUserMessage(), true));
                            chatAdapter.addMessage(new ChatMessage(history.getAiResponse(), false));
                        }
                        rvChatMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
                    } else {
                        // 대화 기록이 없으면 인사 메시지
                        showWelcomeMessage();
                    }
                } else {
                    showWelcomeMessage();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatHistoryResponse>>> call, Throwable t) {
                showWelcomeMessage();
            }
        });
    }

    private void showWelcomeMessage() {
        chatAdapter.addMessage(new ChatMessage(
                "안녕하세요! 저는 당신의 AI 영양사입니다. 🥗\n\n" +
                "식단, 영양, 건강에 대해 무엇이든 물어보세요!\n" +
                "당신의 식사 기록과 프로필을 바탕으로 맞춤 조언을 드릴게요.", false));
    }

    private void sendMessage() {
        String message = etChatInput.getText().toString().trim();
        if (!message.isEmpty()) {
            chatAdapter.addMessage(new ChatMessage(message, true));
            rvChatMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
            etChatInput.setText("");

            apiService.chat(new ChatRequest(message)).enqueue(new Callback<ApiResponse<ChatResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ChatResponse>> call, Response<ApiResponse<ChatResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String reply = response.body().getData().getReply();
                        chatAdapter.addMessage(new ChatMessage(reply, false));
                        rvChatMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
                    } else {
                        chatAdapter.addMessage(new ChatMessage("죄송합니다. 오류가 발생했습니다.", false));
                        rvChatMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ChatResponse>> call, Throwable t) {
                    chatAdapter.addMessage(new ChatMessage("네트워크 오류가 발생했습니다: " + t.getMessage(), false));
                    rvChatMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            });
        }
    }
}
