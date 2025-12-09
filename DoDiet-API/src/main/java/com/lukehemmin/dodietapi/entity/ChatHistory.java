package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI 채팅 히스토리
 */
@Entity
@Table(name = "chat_histories", indexes = {
    @Index(name = "idx_chat_history_user", columnList = "user_id"),
    @Index(name = "idx_chat_history_user_date", columnList = "user_id, created_at")
})
@Getter @Setter
@NoArgsConstructor
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiResponse;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChatHistory(User user, String userMessage, String aiResponse) {
        this.user = user;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
    }
}
