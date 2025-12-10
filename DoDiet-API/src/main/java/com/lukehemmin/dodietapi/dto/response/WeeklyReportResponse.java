package com.lukehemmin.dodietapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportResponse {
    
    // AI 주간 총평
    private String aiSummary;
    
    // 주간 평균 칼로리
    private double averageCalories;
    
    // 주간 총 칼로리
    private double totalCalories;
    
    // 기록한 일수
    private int recordedDays;
    
    // 가장 많이 먹은 음식 TOP 3
    private List<TopFood> topFoods;
    
    // 완료한 챌린지 목록
    private List<CompletedChallenge> completedChallenges;
    
    // 완료한 챌린지 수
    private int completedChallengeCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopFood {
        private String foodName;
        private int count;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedChallenge {
        private String id;
        private String name;
        private String completedAt;
    }
}
