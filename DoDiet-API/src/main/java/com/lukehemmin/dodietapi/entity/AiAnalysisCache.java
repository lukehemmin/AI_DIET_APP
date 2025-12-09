package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_analysis_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiAnalysisType analysisType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDateTime lastMealUpdateAt;  // 마지막 식단 변경 시간 (캐시 무효화용)

    private Integer cooldownMinutes;  // 쿨다운 시간 (분)

    @Column(nullable = false)
    private Boolean isValid;  // 캐시 유효 여부

    @PrePersist
    public void prePersist() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
        if (isValid == null) {
            isValid = true;
        }
        if (cooldownMinutes == null) {
            cooldownMinutes = 30;  // 기본 30분 쿨다운
        }
    }

    // 새로고침 가능 여부 체크
    public boolean canRefresh() {
        if (!isValid) return true;
        LocalDateTime cooldownEnd = generatedAt.plusMinutes(cooldownMinutes);
        return LocalDateTime.now().isAfter(cooldownEnd);
    }

    // 쿨다운 남은 시간 (초)
    public long getRemainingCooldownSeconds() {
        if (!isValid) return 0;
        LocalDateTime cooldownEnd = generatedAt.plusMinutes(cooldownMinutes);
        long seconds = java.time.Duration.between(LocalDateTime.now(), cooldownEnd).getSeconds();
        return Math.max(0, seconds);
    }
}
