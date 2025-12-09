package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.request.ChatRequest;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.ChatHistoryResponse;
import com.lukehemmin.dodietapi.dto.response.ChatResponse;
import com.lukehemmin.dodietapi.entity.ChatHistory;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.ChatHistoryRepository;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.service.AiChatService;
import com.lukehemmin.dodietapi.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;
    private final AiChatService aiChatService;
    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * AI 채팅 - Function Calling + 메모리 시스템
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            Authentication authentication,
            @RequestBody ChatRequest request) {
        
        User user = getUser(authentication);
        String reply = aiChatService.chat(user, request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(reply)));
    }

    /**
     * 기본 채팅 (인증 없이, 기존 방식)
     */
    @PostMapping("/chat/simple")
    public ResponseEntity<ApiResponse<ChatResponse>> simpleChat(@RequestBody ChatRequest request) {
        String reply = geminiService.chat(request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(reply)));
    }

    /**
     * 채팅 히스토리 조회 (최근 50개)
     */
    @GetMapping("/chat/history")
    public ResponseEntity<ApiResponse<List<ChatHistoryResponse>>> getChatHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit) {
        
        User user = getUser(authentication);
        List<ChatHistory> histories = chatHistoryRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(0, Math.min(limit, 100)));
        
        // 시간순으로 정렬 (오래된 것 먼저)
        Collections.reverse(histories);
        
        List<ChatHistoryResponse> response = histories.stream()
                .map(ChatHistoryResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 채팅 히스토리 삭제
     */
    @DeleteMapping("/chat/history")
    @Transactional
    public ResponseEntity<ApiResponse<String>> clearChatHistory(Authentication authentication) {
        User user = getUser(authentication);
        chatHistoryRepository.deleteByUser(user);
        return ResponseEntity.ok(ApiResponse.success("채팅 기록이 삭제되었습니다."));
    }

    private User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
