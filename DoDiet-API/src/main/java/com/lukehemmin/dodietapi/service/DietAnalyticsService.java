package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.DietAnalyticsResponse;
import com.lukehemmin.dodietapi.dto.response.DietAnalyticsResponse.*;
import com.lukehemmin.dodietapi.dto.response.WeeklyReportResponse;
import com.lukehemmin.dodietapi.entity.ChallengeStatus;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.MealTime;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.UserChallengeProgress;
import com.lukehemmin.dodietapi.repository.MealRepository;
import com.lukehemmin.dodietapi.repository.UserChallengeProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DietAnalyticsService {

    private final MealRepository mealRepository;
    private final UserChallengeProgressRepository challengeProgressRepository;

    /**
     * 전체 분석 데이터 조회
     */
    @Transactional(readOnly = true)
    public DietAnalyticsResponse getAnalytics(User user, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        
        List<Meal> meals = mealRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        
        double targetCalories = calculateTargetCalories(user);
        double targetProtein = calculateTargetProtein(user);
        double targetCarbs = calculateTargetCarbs(user);
        double targetFat = calculateTargetFat(user);
        
        return DietAnalyticsResponse.builder()
                .calorieTrend(buildCalorieTrend(meals, startDate, endDate, targetCalories))
                .nutritionBalance(buildNutritionBalance(meals, targetProtein, targetCarbs, targetFat))
                .eatingHabitsAnalysis(buildEatingHabitsAnalysis(meals, targetCalories))
                .patternAnalysis(buildPatternAnalysis(meals, targetCalories, targetProtein, targetCarbs, targetFat))
                .mealTimePattern(buildMealTimePattern(meals, startDate, endDate))
                .build();
    }

    /**
     * 칼로리 트렌드 데이터 생성
     */
    private List<CalorieTrendItem> buildCalorieTrend(List<Meal> meals, LocalDate startDate, LocalDate endDate, double targetCalories) {
        // 날짜별 칼로리 합계
        Map<LocalDate, Double> caloriesByDate = meals.stream()
                .collect(Collectors.groupingBy(
                        Meal::getDate,
                        Collectors.summingDouble(m -> m.getKcal() != null ? m.getKcal() : 0)
                ));
        
        List<CalorieTrendItem> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
            double calories = caloriesByDate.getOrDefault(date, 0.0);
            
            trend.add(CalorieTrendItem.builder()
                    .date(date.format(formatter))
                    .dayOfWeek(dayOfWeek)
                    .calories(calories)
                    .targetCalories(targetCalories)
                    .build());
        }
        
        return trend;
    }

    /**
     * 주간 영양 균형 데이터 생성
     */
    private NutritionBalance buildNutritionBalance(List<Meal> meals, double targetProtein, double targetCarbs, double targetFat) {
        if (meals.isEmpty()) {
            return NutritionBalance.builder()
                    .myProtein(0).myCarbs(0).myFat(0).myFiber(0).myWater(0)
                    .recommendedProtein(100).recommendedCarbs(100).recommendedFat(100).recommendedFiber(100).recommendedWater(100)
                    .build();
        }
        
        // 일 평균 계산
        long days = meals.stream().map(Meal::getDate).distinct().count();
        if (days == 0) days = 1;
        
        double avgProtein = meals.stream().mapToDouble(m -> m.getProtein() != null ? m.getProtein() : 0).sum() / days;
        double avgCarbs = meals.stream().mapToDouble(m -> m.getCarbs() != null ? m.getCarbs() : 0).sum() / days;
        double avgFat = meals.stream().mapToDouble(m -> m.getFat() != null ? m.getFat() : 0).sum() / days;
        
        // 권장량 대비 비율 (최대 150%)
        double targetFiber = 25.0; // 권장 식이섬유 (임시)
        double targetWater = 2000.0; // 권장 수분 (ml)
        
        return NutritionBalance.builder()
                .myProtein(Math.min(150, (avgProtein / targetProtein) * 100))
                .myCarbs(Math.min(150, (avgCarbs / targetCarbs) * 100))
                .myFat(Math.min(150, (avgFat / targetFat) * 100))
                .myFiber(50) // fiber 데이터 없음, 임시값
                .myWater(70) // 수분 데이터는 별도 관리 필요, 임시값
                .recommendedProtein(100)
                .recommendedCarbs(100)
                .recommendedFat(100)
                .recommendedFiber(100)
                .recommendedWater(100)
                .build();
    }

    /**
     * 식습관 분석 텍스트 생성
     */
    private String buildEatingHabitsAnalysis(List<Meal> meals, double targetCalories) {
        if (meals.isEmpty()) {
            return "아직 기록된 식단이 없습니다. 식사를 기록하면 AI가 식습관을 분석해드릴게요!";
        }
        
        long days = meals.stream().map(Meal::getDate).distinct().count();
        if (days < 3) {
            return "식단 기록이 " + days + "일 뿐이에요. 3일 이상 기록하시면 더 정확한 분석을 받으실 수 있습니다.";
        }
        
        // 일 평균 칼로리
        double totalCalories = meals.stream().mapToDouble(m -> m.getKcal() != null ? m.getKcal() : 0).sum();
        double avgCalories = totalCalories / days;
        
        // 식사 패턴 분석
        Map<MealTime, Long> mealTimeCounts = meals.stream()
                .filter(m -> m.getMealTime() != null)
                .collect(Collectors.groupingBy(Meal::getMealTime, Collectors.counting()));
        
        long breakfastCount = mealTimeCounts.getOrDefault(MealTime.BREAKFAST, 0L);
        long lunchCount = mealTimeCounts.getOrDefault(MealTime.LUNCH, 0L);
        long dinnerCount = mealTimeCounts.getOrDefault(MealTime.DINNER, 0L);
        long snackCount = mealTimeCounts.getOrDefault(MealTime.SNACK, 0L);
        
        StringBuilder analysis = new StringBuilder();
        
        // 칼로리 분석
        double calorieRatio = avgCalories / targetCalories;
        if (calorieRatio < 0.8) {
            analysis.append("⚠️ 평균 칼로리 섭취량이 권장량의 ").append(String.format("%.0f%%", calorieRatio * 100))
                    .append("로 부족합니다. 충분한 영양 섭취를 권장드려요.\n\n");
        } else if (calorieRatio > 1.2) {
            analysis.append("⚠️ 평균 칼로리 섭취량이 권장량의 ").append(String.format("%.0f%%", calorieRatio * 100))
                    .append("로 다소 높습니다. 식사량 조절을 고려해보세요.\n\n");
        } else {
            analysis.append("✅ 평균 칼로리 섭취량이 권장 범위 내에 있어요! 잘하고 계십니다.\n\n");
        }
        
        // 아침 식사 분석
        double breakfastRate = (double) breakfastCount / days;
        if (breakfastRate < 0.5) {
            analysis.append("🌅 아침 식사를 자주 거르시네요(").append(String.format("%.0f%%", breakfastRate * 100))
                    .append("). 아침 식사는 하루 대사 활성화에 중요합니다.\n");
        } else {
            analysis.append("🌅 아침 식사를 규칙적으로 하고 계세요. 좋은 습관입니다!\n");
        }
        
        // 간식 분석
        double snackRate = (double) snackCount / days;
        if (snackRate > 2) {
            analysis.append("🍪 간식 섭취가 많은 편이에요(일 평균 ").append(String.format("%.1f", snackRate))
                    .append("회). 건강한 간식으로 대체해보세요.\n");
        }
        
        return analysis.toString().trim();
    }

    /**
     * 패턴 분석 데이터 생성
     */
    private PatternAnalysis buildPatternAnalysis(List<Meal> meals, double targetCalories, 
                                                  double targetProtein, double targetCarbs, double targetFat) {
        if (meals.isEmpty()) {
            return PatternAnalysis.builder()
                    .goodHabitsPercent(0)
                    .badHabitsPercent(0)
                    .improvePercent(100)
                    .analysisText("데이터 수집 중입니다. 식단을 기록하면 분석이 시작됩니다.")
                    .build();
        }
        
        long days = meals.stream().map(Meal::getDate).distinct().count();
        if (days == 0) days = 1;
        
        int goodPoints = 0;
        int badPoints = 0;
        int totalChecks = 5;
        
        // 1. 칼로리 체크
        double avgCalories = meals.stream().mapToDouble(m -> m.getKcal() != null ? m.getKcal() : 0).sum() / days;
        if (avgCalories >= targetCalories * 0.8 && avgCalories <= targetCalories * 1.2) {
            goodPoints++;
        } else {
            badPoints++;
        }
        
        // 2. 단백질 체크
        double avgProtein = meals.stream().mapToDouble(m -> m.getProtein() != null ? m.getProtein() : 0).sum() / days;
        if (avgProtein >= targetProtein * 0.8) {
            goodPoints++;
        } else {
            badPoints++;
        }
        
        // 3. 아침 식사 체크
        Map<MealTime, Long> mealTimeCounts2 = meals.stream()
                .filter(m -> m.getMealTime() != null)
                .collect(Collectors.groupingBy(Meal::getMealTime, Collectors.counting()));
        
        long breakfastCount = mealTimeCounts2.getOrDefault(MealTime.BREAKFAST, 0L);
        if ((double) breakfastCount / days >= 0.7) {
            goodPoints++;
        } else {
            badPoints++;
        }
        
        // 4. 간식 체크
        long snackCount = mealTimeCounts2.getOrDefault(MealTime.SNACK, 0L);
        if ((double) snackCount / days <= 1.5) {
            goodPoints++;
        } else {
            badPoints++;
        }
        
        // 5. 식사 규칙성 체크
        long lunchCount = mealTimeCounts2.getOrDefault(MealTime.LUNCH, 0L);
        long dinnerCount = mealTimeCounts2.getOrDefault(MealTime.DINNER, 0L);
        if ((double) (lunchCount + dinnerCount) / (days * 2) >= 0.7) {
            goodPoints++;
        } else {
            badPoints++;
        }
        
        int improvePoints = totalChecks - goodPoints - badPoints;
        
        int goodPercent = (goodPoints * 100) / totalChecks;
        int badPercent = (badPoints * 100) / totalChecks;
        int improvePercent = 100 - goodPercent - badPercent;
        
        String analysisText;
        if (goodPercent >= 60) {
            analysisText = "전반적으로 좋은 식습관을 유지하고 계세요! 👏";
        } else if (badPercent >= 60) {
            analysisText = "식습관 개선이 필요해요. 규칙적인 식사와 영양 균형에 신경써보세요.";
        } else {
            analysisText = "식습관이 보통이에요. 조금만 더 신경쓰면 더 건강해질 수 있어요!";
        }
        
        return PatternAnalysis.builder()
                .goodHabitsPercent(goodPercent)
                .badHabitsPercent(badPercent)
                .improvePercent(improvePercent)
                .analysisText(analysisText)
                .build();
    }

    /**
     * 식사 시간 패턴 (히트맵용) - 최근 4주간 요일별 식사 횟수
     */
    private List<MealTimePattern> buildMealTimePattern(List<Meal> meals, LocalDate startDate, LocalDate endDate) {
        // 요일별 식사 횟수 집계 (최근 4주 = 28일)
        LocalDate fourWeeksAgo = endDate.minusDays(27);
        if (fourWeeksAgo.isBefore(startDate)) {
            fourWeeksAgo = startDate;
        }
        
        // 요일별(0=일요일~6=토요일) 식사타입별 횟수
        int[][] counts = new int[7][3]; // [요일][0=아침, 1=점심, 2=저녁]
        
        LocalDate finalFourWeeksAgo = fourWeeksAgo;
        meals.stream()
                .filter(m -> m.getMealTime() != null && !m.getDate().isBefore(finalFourWeeksAgo))
                .forEach(m -> {
                    int dayOfWeek = m.getDate().getDayOfWeek().getValue() % 7; // 0=일, 1=월, ..., 6=토
                    switch (m.getMealTime()) {
                        case BREAKFAST -> counts[dayOfWeek][0]++;
                        case LUNCH -> counts[dayOfWeek][1]++;
                        case DINNER -> counts[dayOfWeek][2]++;
                        default -> {}
                    }
                });
        
        // 결과 생성 (일, 월, 화, 수, 목, 금, 토 순서)
        String[] dayNames = {"일", "월", "화", "수", "목", "금", "토"};
        List<MealTimePattern> patterns = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            patterns.add(MealTimePattern.builder()
                    .dayOfWeek(dayNames[i])
                    .breakfastCount(Math.min(counts[i][0], 4)) // 최대 4
                    .lunchCount(Math.min(counts[i][1], 4))
                    .dinnerCount(Math.min(counts[i][2], 4))
                    .build());
        }
        
        return patterns;
    }

    // ========== 권장량 계산 ==========
    
    private double calculateTargetCalories(User user) {
        if (user.getHeight() == null || user.getWeight() == null || user.getAge() == null) {
            return 2000; // 기본값
        }
        
        // BMR (Harris-Benedict)
        double bmr;
        if (user.getGender() != null && user.getGender().name().equals("MALE")) {
            bmr = 88.362 + (13.397 * user.getWeight()) + (4.799 * user.getHeight()) - (5.677 * user.getAge());
        } else {
            bmr = 447.593 + (9.247 * user.getWeight()) + (3.098 * user.getHeight()) - (4.330 * user.getAge());
        }
        
        // 활동 계수
        double activityMultiplier = switch (user.getActivityLevel()) {
            case SEDENTARY -> 1.2;
            case LIGHT -> 1.375;
            case MODERATE -> 1.55;
            case ACTIVE -> 1.725;
            case VERY_ACTIVE -> 1.9;
            default -> 1.55;
        };
        
        return bmr * activityMultiplier;
    }
    
    private double calculateTargetProtein(User user) {
        // 체중 kg당 1.2g 권장
        return user.getWeight() != null ? user.getWeight() * 1.2 : 60;
    }
    
    private double calculateTargetCarbs(User user) {
        // 전체 칼로리의 50%를 탄수화물에서 (1g = 4kcal)
        return calculateTargetCalories(user) * 0.5 / 4;
    }
    
    private double calculateTargetFat(User user) {
        // 전체 칼로리의 25%를 지방에서 (1g = 9kcal)
        return calculateTargetCalories(user) * 0.25 / 9;
    }
    
    // ========== 주간 건강 리포트 ==========
    
    /**
     * 주간 건강 리포트 데이터 조회
     */
    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(User user) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 최근 7일
        
        List<Meal> meals = mealRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        
        // 기록된 일수
        long recordedDays = meals.stream().map(Meal::getDate).distinct().count();
        
        // 총 칼로리 및 평균 칼로리
        double totalCalories = meals.stream()
                .mapToDouble(m -> m.getKcal() != null ? m.getKcal() : 0)
                .sum();
        double avgCalories = recordedDays > 0 ? totalCalories / recordedDays : 0;
        
        // AI 주간 총평
        String aiSummary = buildWeeklySummary(meals, recordedDays, avgCalories, calculateTargetCalories(user));
        
        // 가장 많이 먹은 음식 TOP 3
        List<WeeklyReportResponse.TopFood> topFoods = buildTopFoods(meals);
        
        // 완료한 챌린지
        List<UserChallengeProgress> completedChallenges = challengeProgressRepository.findByUser(user)
                .stream()
                .filter(p -> p.getStatus() == ChallengeStatus.COMPLETED)
                .filter(p -> p.getCompletedAt() != null && 
                        p.getCompletedAt().toLocalDate().isAfter(startDate.minusDays(1)))
                .toList();
        
        List<WeeklyReportResponse.CompletedChallenge> challengeList = completedChallenges.stream()
                .map(p -> WeeklyReportResponse.CompletedChallenge.builder()
                        .id(p.getChallenge().getId())
                        .name(p.getChallenge().getTitle())
                        .completedAt(p.getCompletedAt() != null ? 
                                p.getCompletedAt().format(DateTimeFormatter.ofPattern("M월 d일")) : null)
                        .build())
                .collect(Collectors.toList());
        
        return WeeklyReportResponse.builder()
                .aiSummary(aiSummary)
                .averageCalories(Math.round(avgCalories))
                .totalCalories(Math.round(totalCalories))
                .recordedDays((int) recordedDays)
                .topFoods(topFoods)
                .completedChallenges(challengeList)
                .completedChallengeCount(challengeList.size())
                .build();
    }
    
    /**
     * 주간 AI 총평 생성
     */
    private String buildWeeklySummary(List<Meal> meals, long recordedDays, double avgCalories, double targetCalories) {
        if (meals.isEmpty()) {
            return "이번 주는 식단 기록이 없어요. 다음 주에는 꾸준히 기록해보세요! 💪";
        }
        
        if (recordedDays < 3) {
            return "이번 주 " + recordedDays + "일 기록하셨네요! 더 꾸준히 기록하면 정확한 분석을 받으실 수 있어요.";
        }
        
        StringBuilder summary = new StringBuilder();
        
        // 칼로리 분석
        double ratio = avgCalories / targetCalories;
        if (ratio >= 0.8 && ratio <= 1.2) {
            summary.append("이번 주 칼로리 섭취가 적정 수준이에요! 👏 ");
        } else if (ratio < 0.8) {
            summary.append("이번 주 칼로리 섭취가 다소 부족했어요. 충분히 드세요! ");
        } else {
            summary.append("이번 주 칼로리 섭취가 조금 많았어요. 조절해보세요! ");
        }
        
        // 기록 일수에 따른 격려
        if (recordedDays >= 6) {
            summary.append("거의 매일 기록하셨네요! 정말 대단해요! 🌟");
        } else if (recordedDays >= 4) {
            summary.append("꾸준히 기록하고 계세요. 좋은 습관이에요! ✨");
        } else {
            summary.append("조금 더 자주 기록하면 건강 관리에 도움이 돼요!");
        }
        
        return summary.toString();
    }
    
    /**
     * 가장 많이 먹은 음식 TOP 3
     */
    private List<WeeklyReportResponse.TopFood> buildTopFoods(List<Meal> meals) {
        if (meals.isEmpty()) {
            return List.of();
        }
        
        // 음식명별 횟수 집계
        Map<String, Long> foodCounts = meals.stream()
                .filter(m -> m.getFoodItem() != null && !m.getFoodItem().isBlank())
                .collect(Collectors.groupingBy(
                        m -> m.getFoodItem().trim(),
                        Collectors.counting()
                ));
        
        // 상위 3개 추출
        return foodCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> WeeklyReportResponse.TopFood.builder()
                        .foodName(e.getKey())
                        .count(e.getValue().intValue())
                        .build())
                .toList();
    }
}
