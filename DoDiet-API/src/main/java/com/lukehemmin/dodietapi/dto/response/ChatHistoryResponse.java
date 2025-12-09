package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.ChatHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    private UUID id;
    private String userMessage;
    private String aiResponse;
    private LocalDateTime createdAt;

    public static ChatHistoryResponse from(ChatHistory history) {
        return new ChatHistoryResponse(
                history.getId(),
                history.getUserMessage(),
                history.getAiResponse(),
                history.getCreatedAt()
        );
    }
}
