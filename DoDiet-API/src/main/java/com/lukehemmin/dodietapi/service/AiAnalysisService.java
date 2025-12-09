package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.AiAnalysisResponse;
import com.lukehemmin.dodietapi.entity.AiAnalysisCache;
import com.lukehemmin.dodietapi.entity.AiAnalysisType;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.AiAnalysisCacheRepository;
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
    private final MealRepository mealRepository;
    private final GeminiService geminiService;

    private static final int COOLDOWN_MINUTES = 30;
    private static final int CACHE_EXPIRY_DAYS = 7;  // 캐시 만료 기간 (일)

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
     * 냉장고 파먹기 레시피 생성 (재료 기반)
     */
    @Transactional
    public AiAnalysisResponse generateFridgeRecipe(User user, String ingredients) {
        String prompt = buildFridgeRecipePrompt(user, ingredients);
        String content = geminiService.chat(prompt);
        
        // 냉장고 레시피는 매번 새로 생성하므로 캐시하지 않음 (또는 짧은 캐시)
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
        
        return AiAnalysisResponse.from(cache, false);
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
                    당신은 전문 피트니스 코치입니다. 다음 사용자 정보와 최근 1주일 식단을 분석하여 맞춤형 주간 운동 플랜을 제안해주세요.
                    
                    [사용자 정보]
                    %s
                    
                    [최근 1주일 식단]
                    %s
                    
                    다음 형식으로 간단하게 답변해주세요:
                    1. 이번 주 운동 목표 (1줄)
                    2. 추천 운동 (3-4개, 각각 운동명과 간단한 설명)
                    3. 주의사항 (1-2줄)
                    
                    한국어로 답변하고, 마크다운 형식은 사용하지 마세요.
                    """, userInfo, mealSummary);

            case CUSTOM_RECIPE:
                return String.format("""
                    당신은 전문 영양사입니다. 다음 사용자 정보와 최근 1주일 식단을 분석하여 맞춤형 레시피를 추천해주세요.
                    
                    [사용자 정보]
                    %s
                    
                    [최근 1주일 식단]
                    %s
                    
                    부족한 영양소를 보충할 수 있는 레시피 1개를 추천해주세요.
                    다음 형식으로 답변해주세요:
                    - 요리 이름
                    - 필요 재료 (간단히)
                    - 예상 영양 정보 (칼로리, 단백질, 탄수화물, 지방)
                    - 추천 이유 (1-2줄)
                    
                    한국어로 답변하고, 마크다운 형식은 사용하지 마세요.
                    """, userInfo, mealSummary);

            default:
                return "안녕하세요!";
        }
    }

    private String buildFridgeRecipePrompt(User user, String ingredients) {
        String userInfo = buildUserInfo(user);
        
        return String.format("""
            당신은 전문 요리사입니다. 사용자가 가지고 있는 재료로 만들 수 있는 건강한 레시피를 추천해주세요.
            
            [사용자 정보]
            %s
            
            [가지고 있는 재료]
            %s
            
            다음 형식으로 레시피 1개를 추천해주세요:
            - 요리 이름
            - 필요 재료 및 분량
            - 간단한 조리 방법 (3-5단계)
            - 예상 영양 정보
            - 조리 팁 (선택)
            
            한국어로 답변하고, 마크다운 형식은 사용하지 마세요.
            """, userInfo, ingredients);
    }

    private String buildMealSummary(List<Meal> meals) {
        if (meals.isEmpty()) {
            return "기록된 식단이 없습니다.";
        }

        double totalKcal = meals.stream().mapToDouble(m -> m.getKcal() != null ? m.getKcal() : 0).sum();
        double totalProtein = meals.stream().mapToDouble(m -> m.getProtein() != null ? m.getProtein() : 0).sum();
        double totalCarbs = meals.stream().mapToDouble(m -> m.getCarbs() != null ? m.getCarbs() : 0).sum();
        double totalFat = meals.stream().mapToDouble(m -> m.getFat() != null ? m.getFat() : 0).sum();
        int daysWithMeals = (int) meals.stream().map(Meal::getDate).distinct().count();

        String foodItems = meals.stream()
                .map(Meal::getFoodItem)
                .distinct()
                .limit(10)
                .collect(Collectors.joining(", "));

        return String.format("""
            - 기록 일수: %d일
            - 총 칼로리: %.0f kcal (일평균: %.0f kcal)
            - 총 단백질: %.1fg (일평균: %.1fg)
            - 총 탄수화물: %.1fg
            - 총 지방: %.1fg
            - 주요 음식: %s
            """, 
            daysWithMeals,
            totalKcal, daysWithMeals > 0 ? totalKcal / daysWithMeals : 0,
            totalProtein, daysWithMeals > 0 ? totalProtein / daysWithMeals : 0,
            totalCarbs,
            totalFat,
            foodItems);
    }

    private String buildUserInfo(User user) {
        return String.format("""
            - 이름: %s
            - 성별: %s
            - 나이: %d세
            - 키: %.1fcm
            - 몸무게: %.1fkg
            - 활동량: %s
            """,
            user.getName(),
            user.getGender() != null ? user.getGender().name() : "미설정",
            user.getAge() != null ? user.getAge() : 0,
            user.getHeight() != null ? user.getHeight() : 0,
            user.getWeight() != null ? user.getWeight() : 0,
            user.getActivityLevel() != null ? user.getActivityLevel().name() : "미설정");
    }
}
