package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI가 사용자별로 기억하는 정보
 * - 중요한 대화 내용, 선호도, 목표 등을 저장
 */
@Entity
@Table(name = "ai_memories", indexes = {
    @Index(name = "idx_ai_memory_user", columnList = "user_id"),
    @Index(name = "idx_ai_memory_category", columnList = "user_id, category")
})
@Getter @Setter
@NoArgsConstructor
public class AiMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String category; // PREFERENCE, GOAL, HEALTH_INFO, IMPORTANT_FACT, etc.

    @Column(nullable = false, length = 200)
    private String title; // 짧은 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 기억 내용

    @Column(nullable = false)
    private Integer importance = 5; // 1-10, 높을수록 중요

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public AiMemory(User user, String category, String title, String content, Integer importance) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.importance = importance;
    }
}
