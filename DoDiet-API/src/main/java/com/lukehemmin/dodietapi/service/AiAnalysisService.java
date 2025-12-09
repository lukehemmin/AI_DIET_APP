package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.AiAnalysisResponse;
import com.lukehemmin.dodietapi.dto.response.FridgeRecipeHistoryResponse;
import com.lukehemmin.dodietapi.entity.AiAnalysisCache;
import com.lukehemmin.dodietapi.entity.AiAnalysisType;
import com.lukehemmin.dodietapi.entity.FridgeRecipeHistory;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.AiAnalysisCacheRepository;
import com.lukehemmin.dodietapi.repository.FridgeRecipeHistoryRepository;
import com.lukehemmin.dodietapi.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiAnalysisService {

    private final AiAnalysisCacheRepository cacheRepository;
    private final FridgeRecipeHistoryRepository fridgeRecipeHistoryRepository;
    private final MealRepository mealRepository;
    private final GeminiService geminiService;

    private static final int COOLDOWN_MINUTES = 60;      // 새로고침 쿨다운 1시간
    private static final int CACHE_EXPIRY_DAYS = 14;     // 캐시 만료 기간 2주

    /**
     * 캐시된 AI 분석 결과 가져오기 (없거나 오래되면 자동 생성)
     */
    @Transactional
    public AiAnalysisResponse getAnalysis(User user, AiAnalysisType type) {
        Optional<AiAnalysisCache> cacheOpt = cacheRepository.findByUserAndAnalysisType(user, type);
        
        // 1. 캐시가 없거나 content가 비어있으면 자동 생성
        if (cacheOpt.isEmpty() || cacheOpt.get().getContent() == null || cacheOpt.get().getContent().isEmpty()) {
            log.info("No cache found for user {} and type {}, generating...", user.getId(), type);
            return generateAndSaveAnalysis(user, type, cacheOpt.orElse(null));
        }

        AiAnalysisCache cache = cacheOpt.get();
        boolean needsUpdate = checkIfNeedsUpdate(user, cache);
        boolean isExpired = isCacheExpired(cache);
        
        // 2. 캐시가 만료되었거나 식단이 변경되었으면 자동 갱신
        if (isExpired || needsUpdate) {
            log.info("Cache expired or needs update for user {} and type {}, regenerating...", user.getId(), type);
            return generateAndSaveAnalysis(user, type, cache);
        }
        
        return AiAnalysisResponse.from(cache, false);
    }
    
    /**
     * 캐시 만료 여부 확인
     */
    private boolean isCacheExpired(AiAnalysisCache cache) {
        if (cache.getGeneratedAt() == null) return true;
        return cache.getGeneratedAt().plusDays(CACHE_EXPIRY_DAYS).isBefore(LocalDateTime.now());
    }
    
    /**
     * AI 분석 생성 및 저장 (내부 공통 메서드)
     */
    private AiAnalysisResponse generateAndSaveAnalysis(User user, AiAnalysisType type, AiAnalysisCache existingCache) {
        try {
            String content = generateAiContent(user, type);
            LocalDateTime lastMealUpdate = getLastMealUpdateTime(user);
            
            AiAnalysisCache cache = existingCache != null ? existingCache : AiAnalysisCache.builder()
                    .user(user)
                    .analysisType(type)
                    .build();
            
            cache.setContent(content);
            cache.setGeneratedAt(LocalDateTime.now());
            cache.setLastMealUpdateAt(lastMealUpdate);
            cache.setCooldownMinutes(COOLDOWN_MINUTES);
            cache.setIsValid(true);
            
            cacheRepository.save(cache);
            return AiAnalysisResponse.from(cache, false);
        } catch (Exception e) {
            log.error("Failed to generate AI analysis for type {}: {}", type, e.getMessage());
            return AiAnalysisResponse.empty(type);
        }
    }

    /**
     * AI 분석 새로고침 (강제 또는 조건부)
     */
    @Transactional
    public AiAnalysisResponse refreshAnalysis(User user, AiAnalysisType type, boolean force) {
        Optional<AiAnalysisCache> cacheOpt = cacheRepository.findByUserAndAnalysisType(user, type);
        
        if (cacheOpt.isPresent() && !force) {
            AiAnalysisCache cache = cacheOpt.get();
            boolean needsUpdate = checkIfNeedsUpdate(user, cache);
            
            // 쿨다운 중이고 업데이트 필요 없으면 캐시 반환
            if (!cache.canRefresh() && !needsUpdate) {
                return AiAnalysisResponse.from(cache, false);
            }
        }

        // 공통 메서드로 AI 생성
        return generateAndSaveAnalysis(user, type, cacheOpt.orElse(null));
    }

    /**
     * AI 하루 식단 계획 생성
     */
    @Transactional
    public AiAnalysisResponse generateDailyMealPlan(User user, String preference) {
        String prompt = buildDailyMealPlanPrompt(user, preference);
        String content = geminiService.chat(prompt);
        
        // 하루 식단 계획은 캐시하지 않고 바로 반환
        return AiAnalysisResponse.builder()
                .analysisType(AiAnalysisType.DAILY_MEAL_PLAN)
                .content(content)
                .generatedAt(LocalDateTime.now())
                .canRefresh(true)
                .remainingCooldownSeconds(0)
                .needsUpdate(false)
                .build();
    }

    /**
     * 냉장고 파먹기 레시피 생성 (재료 기반)
     */
    @Transactional
    public AiAnalysisResponse generateFridgeRecipe(User user, String ingredients) {
        String prompt = buildFridgeRecipePrompt(user, ingredients);
        String content = geminiService.chat(prompt);
        
        // 최신 레시피 캐시 업데이트
        AiAnalysisCache cache = cacheRepository.findByUserAndAnalysisType(user, AiAnalysisType.FRIDGE_RECIPE)
                .orElse(AiAnalysisCache.builder()
                        .user(user)
                        .analysisType(AiAnalysisType.FRIDGE_RECIPE)
                        .build());
        
        cache.setContent(content);
        cache.setGeneratedAt(LocalDateTime.now());
        cache.setLastMealUpdateAt(LocalDateTime.now());
        cache.setCooldownMinutes(5);  // 냉장고 레시피는 5분 쿨다운
        cache.setIsValid(true);
        
        cacheRepository.save(cache);
        
        // 히스토리에도 저장
        FridgeRecipeHistory history = FridgeRecipeHistory.builder()
                .user(user)
                .ingredients(ingredients)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        fridgeRecipeHistoryRepository.save(history);
        
        return AiAnalysisResponse.from(cache, false);
    }
    
    /**
     * 최근 냉장고 레시피 조회 (앱 시작 시 표시용)
     */
    @Transactional(readOnly = true)
    public Optional<FridgeRecipeHistoryResponse> getLatestFridgeRecipe(User user) {
        return fridgeRecipeHistoryRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(FridgeRecipeHistoryResponse::from);
    }
    
    /**
     * 냉장고 레시피 히스토리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<FridgeRecipeHistoryResponse> getFridgeRecipeHistory(User user) {
        return fridgeRecipeHistoryRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(FridgeRecipeHistoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 식단 변경 시 캐시 무효화
     */
    @Transactional
    public void invalidateCache(User user) {
        // 운동 플랜 캐시 무효화
        cacheRepository.findByUserAndAnalysisType(user, AiAnalysisType.WEEKLY_EXERCISE_PLAN)
                .ifPresent(cache -> {
                    cache.setLastMealUpdateAt(LocalDateTime.now().minusYears(1));
                    cacheRepository.save(cache);
                });
        
        // 맞춤 레시피 캐시 무효화
        cacheRepository.findByUserAndAnalysisType(user, AiAnalysisType.CUSTOM_RECIPE)
                .ifPresent(cache -> {
                    cache.setLastMealUpdateAt(LocalDateTime.now().minusYears(1));
                    cacheRepository.save(cache);
                });
    }

    // ===== Private Methods =====

    private boolean checkIfNeedsUpdate(User user, AiAnalysisCache cache) {
        if (cache.getGeneratedAt() == null) {
            return true;  // 캐시가 생성된 적이 없으면 업데이트 필요
        }
        LocalDateTime lastMealUpdate = getLastMealUpdateTime(user);
        // 캐시 생성 이후 식단이 추가되었으면 업데이트 필요
        return lastMealUpdate.isAfter(cache.getGeneratedAt());
    }

    private LocalDateTime getLastMealUpdateTime(User user) {
        // 최근 7일간의 식단 중 가장 최근 생성/수정 시간
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<Meal> recentMeals = mealRepository.findByUserAndDateBetween(user, weekAgo, LocalDate.now());
        
        return recentMeals.stream()
                .map(Meal::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));
    }

    private String generateAiContent(User user, AiAnalysisType type) {
        String prompt = buildPrompt(user, type);
        return geminiService.chat(prompt);
    }

    private String buildPrompt(User user, AiAnalysisType type) {
        // 최근 7일 식단 데이터 조회
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        List<Meal> recentMeals = mealRepository.findByUserAndDateBetween(user, weekAgo, LocalDate.now());
        
        String mealSummary = buildMealSummary(recentMeals);
        String userInfo = buildUserInfo(user);

        switch (type) {
            case WEEKLY_EXERCISE_PLAN:
                return String.format("""
                    피트니스 코치로서 맞춤 운동 플랜 제안.
                    [사용자] %s
                    [식단요약] %s
                    형식: 1.목표(1줄) 2.추천운동(3개) 3.주의사항(1줄). 한국어, 간결하게.
                    """, userInfo, mealSummary);

            case CUSTOM_RECIPE:
                return String.format("""
                    영양사로서 부족 영양소 보충 레시피 1개 추천.
                    [사용자] %s
                    [식단요약] %s
                    형식: 요리명, 재료, 영양정보, 추천이유(1줄). 한국어, 간결하게.
                    """, userInfo, mealSummary);

            default:
                return "안녕하세요!";
        }
    }

    private String buildDailyMealPlanPrompt(User user, String preference) {
        String userInfo = buildUserInfo(user);
        
        return String.format("""
            영양사로서 하루 식단 제안.
            [사용자] %s
            [선호] %s
            형식: 🌅아침(메뉴,칼로리) ☀️점심 🌙저녁 🍎간식 📊총칼로리 💡팁(1줄). 한국어.
            """, userInfo, preference);
    }

    private String buildFridgeRecipePrompt(User user, String ingredients) {
        String userInfo = buildUserInfo(user);
        
        return String.format("""
            요리사로서 재료 활용 레시피 1개 추천.
            [사용자] %s
            [재료] %s
            형식: 요리명, 재료분량, 조리법(3단계), 영양정보. 한국어, 간결하게.
            """, userInfo, ingredients);
    }

    private String buildMealSummary(List<Meal> meals) {
        if (meals.isEmpty()) {
            return "식단없음";
        }

        double totalKcal = meals.stream().mapToDouble(m -> m.getKcal() != null ? m.getKcal() : 0).sum();
        double totalProtein = meals.stream().mapToDouble(m -> m.getProtein() != null ? m.getProtein() : 0).sum();
        double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbs() != null ? m.getCarbs() : 0).sum();
        double totalFat = meals.stream().mapToDouble(m -> m.getFat() != null ? m.getFat() : 0).sum();
        int days = (int) meals.stream().map(Meal::getDate).distinct().count();

        String foods = meals.stream()
                .map(Meal::getFoodItem)
                .distinct()
                .limit(5)  // 5개로 제한
                .collect(Collectors.joining(","));

        return String.format("%d일간 %.0fkcal(일%.0f) 단%.0fg 탄%.0fg 지%.0fg [%s]", 
            days, totalKcal, days > 0 ? totalKcal / days : 0,
            totalProtein, totalCarbs, totalFat, foods);
    }

    private String buildUserInfo(User user) {
        return String.format("%s %s %d세 %.0fcm %.0fkg %s",
            user.getName(),
            user.getGender() != null ? user.getGender().name() : "-",
            user.getAge() != null ? user.getAge() : 0,
            user.getHeight() != null ? user.getHeight() : 0,
            user.getWeight() != null ? user.getWeight() : 0,
            user.getActivityLevel() != null ? user.getActivityLevel().name() : "-");
    }
}
