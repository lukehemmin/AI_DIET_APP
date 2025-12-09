package com.lukehemmin.ai_diet_app.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
    private ImageView btnNewChat;
    private ImageView btnChatMenu;
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
        btnNewChat = view.findViewById(R.id.btn_new_chat);
        btnChatMenu = view.findViewById(R.id.btn_chat_menu);

        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(requireContext());
        rvChatMessages.setAdapter(chatAdapter);

        apiService = RetrofitClient.getClient(getContext()).create(ApiService.class);
    }

    private void setupListeners() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
        
        // 새 대화 버튼
        btnNewChat.setOnClickListener(v -> showNewChatConfirmDialog());
        
        // 메뉴 버튼
        btnChatMenu.setOnClickListener(v -> showChatMenu(v));
    }
    
    /**
     * 새 대화 시작 확인 다이얼로그
     */
    private void showNewChatConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("새 대화 시작")
                .setMessage("현재 대화 기록을 삭제하고 새 대화를 시작하시겠습니까?")
                .setPositiveButton("시작", (dialog, which) -> clearChatAndStartNew())
                .setNegativeButton("취소", null)
                .show();
    }
    
    /**
     * 채팅 삭제 후 새 대화 시작
     */
    private void clearChatAndStartNew() {
        apiService.clearChatHistory().enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                chatAdapter.clearMessages();
                showWelcomeMessage();
                Toast.makeText(getContext(), "새 대화를 시작합니다!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // 서버 오류여도 로컬에서는 새 대화 시작
                chatAdapter.clearMessages();
                showWelcomeMessage();
            }
        });
    }
    
    /**
     * 채팅 메뉴 표시
     */
    private void showChatMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "대화 기록 삭제");
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showDeleteHistoryConfirmDialog();
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    /**
     * 대화 기록 삭제 확인 다이얼로그
     */
    private void showDeleteHistoryConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("대화 기록 삭제")
                .setMessage("모든 대화 기록을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
                .setPositiveButton("삭제", (dialog, which) -> deleteChatHistory())
                .setNegativeButton("취소", null)
                .show();
    }
    
    /**
     * 대화 기록 삭제
     */
    private void deleteChatHistory() {
        apiService.clearChatHistory().enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    chatAdapter.clearMessages();
                    showWelcomeMessage();
                    Toast.makeText(getContext(), "대화 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "삭제 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
