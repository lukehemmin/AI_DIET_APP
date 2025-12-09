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
public class DietAnalyticsResponse {
    
    // 칼로리 트렌드 데이터
    private List<CalorieTrendItem> calorieTrend;
    
    // 주간 영양 균형 (레이더 차트용)
    private NutritionBalance nutritionBalance;
    
    // 식습관 분석 텍스트
    private String eatingHabitsAnalysis;
    
    // 패턴 분석
    private PatternAnalysis patternAnalysis;
    
    // 식사 시간 패턴 (히트맵용)
    private List<MealTimePattern> mealTimePattern;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalorieTrendItem {
        private String date;       // "12/01" 형식
        private String dayOfWeek;  // "월", "화" 등
        private double calories;
        private double targetCalories;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NutritionBalance {
        // 내 섭취량 (비율로 0-100)
        private double myProtein;
        private double myCarbs;
        private double myFat;
        private double myFiber;
        private double myWater;
        
        // 권장 섭취량 (기준선, 100)
        private double recommendedProtein;
        private double recommendedCarbs;
        private double recommendedFat;
        private double recommendedFiber;
        private double recommendedWater;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternAnalysis {
        private int goodHabitsPercent;
        private int badHabitsPercent;
        private int improvePercent;
        private String analysisText;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealTimePattern {
        private String dayOfWeek;  // "일", "월", "화" 등
        private int breakfastCount;  // 0-4 (최근 4주간 해당 요일 아침식사 횟수)
        private int lunchCount;
        private int dinnerCount;
    }
}
