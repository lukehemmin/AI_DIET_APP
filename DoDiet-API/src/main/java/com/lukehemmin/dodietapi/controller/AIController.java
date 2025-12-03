package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.request.ChatRequest;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.ChatResponse;
import com.lukehemmin.dodietapi.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final GeminiService geminiService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@RequestBody ChatRequest request) {
        String reply = geminiService.chat(request.getMessage());
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(reply)));
    }
}
