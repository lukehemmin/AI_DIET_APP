package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI가 사용자별로 동적으로 생성하는 맞춤형 업적
 */
@Entity
@Table(name = "dynamic_badges")
@Getter @Setter
@NoArgsConstructor
public class DynamicBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false)
    private String icon; // 아이콘 타입 (nutrition, water, streak, challenge, ai_chat 등)
    
    @Column(nullable = false, length = 500)
    private String condition; // AI가 생성한 달성 조건 설명
    
    @Column(nullable = false)
    private String conditionType; // 조건 유형 (MEAL_COUNT, STREAK_DAYS, NUTRITION_BALANCE 등)
    
    @Column(nullable = false)
    private Integer targetValue; // 목표 수치
    
    @Column(nullable = false)
    private Integer currentValue = 0; // 현재 진행 상황
    
    @Column(nullable = false)
    private Boolean isUnlocked = false;
    
    private LocalDateTime unlockedAt;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // AI가 생성한 업적임을 표시
    @Column(nullable = false)
    private Boolean isAiGenerated = true;
    
    // 업적 생성 시 참고한 컨텍스트 (AI 대화, 식습관 등)
    @Column(length = 1000)
    private String generationContext;
    
    public void checkProgress(int value) {
        this.currentValue = value;
        if (this.currentValue >= this.targetValue && !this.isUnlocked) {
            this.isUnlocked = true;
            this.unlockedAt = LocalDateTime.now();
        }
    }
    
    public double getProgressPercentage() {
        if (targetValue == 0) return 0;
        return Math.min(100.0, (currentValue * 100.0) / targetValue);
    }
}
