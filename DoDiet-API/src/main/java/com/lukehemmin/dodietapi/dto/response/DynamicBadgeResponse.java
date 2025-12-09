package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.DynamicBadge;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DynamicBadgeResponse {
    private UUID id;
    private String name;
    private String description;
    private String icon;
    private String condition;
    private int targetValue;
    private int currentValue;
    private double progressPercentage;
    private boolean isUnlocked;
    private boolean isAiGenerated;
    private LocalDateTime unlockedAt;
    private LocalDateTime createdAt;
    
    public static DynamicBadgeResponse from(DynamicBadge badge) {
        return DynamicBadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .icon(badge.getIcon())
                .condition(badge.getCondition())
                .targetValue(badge.getTargetValue())
                .currentValue(badge.getCurrentValue())
                .progressPercentage(badge.getProgressPercentage())
                .isUnlocked(badge.getIsUnlocked())
                .isAiGenerated(badge.getIsAiGenerated())
                .unlockedAt(badge.getUnlockedAt())
                .createdAt(badge.getCreatedAt())
                .build();
    }
}
