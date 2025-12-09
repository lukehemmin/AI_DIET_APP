package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.AiAnalysisCache;
import com.lukehemmin.dodietapi.entity.AiAnalysisType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {
    private AiAnalysisType analysisType;
    private String content;
    private LocalDateTime generatedAt;
    private boolean canRefresh;
    private long remainingCooldownSeconds;
    private boolean needsUpdate;  // 새 식단이 추가되어 업데이트가 필요한지

    public static AiAnalysisResponse from(AiAnalysisCache cache, boolean needsUpdate) {
        return AiAnalysisResponse.builder()
                .analysisType(cache.getAnalysisType())
                .content(cache.getContent())
                .generatedAt(cache.getGeneratedAt())
                .canRefresh(cache.canRefresh() || needsUpdate)
                .remainingCooldownSeconds(needsUpdate ? 0 : cache.getRemainingCooldownSeconds())
                .needsUpdate(needsUpdate)
                .build();
    }

    public static AiAnalysisResponse empty(AiAnalysisType type) {
        return AiAnalysisResponse.builder()
                .analysisType(type)
                .content(null)
                .generatedAt(null)
                .canRefresh(true)
                .remainingCooldownSeconds(0)
                .needsUpdate(true)
                .build();
    }
}
