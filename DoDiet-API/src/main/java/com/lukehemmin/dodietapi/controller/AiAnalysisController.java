package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.AiAnalysisResponse;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.FridgeRecipeHistoryResponse;
import com.lukehemmin.dodietapi.entity.AiAnalysisType;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-analysis")
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final UserRepository userRepository;

    /**
     * AI 주간 운동 플랜 조회
     */
    @GetMapping("/exercise-plan")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> getExercisePlan(Authentication authentication) {
        User user = getUser(authentication);
        AiAnalysisResponse response = aiAnalysisService.getAnalysis(user, AiAnalysisType.WEEKLY_EXERCISE_PLAN);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * AI 주간 운동 플랜 새로고침
     */
    @PostMapping("/exercise-plan/refresh")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> refreshExercisePlan(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean force) {
        User user = getUser(authentication);
        AiAnalysisResponse response = aiAnalysisService.refreshAnalysis(user, AiAnalysisType.WEEKLY_EXERCISE_PLAN, force);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * AI 맞춤 레시피 조회
     */
    @GetMapping("/custom-recipe")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> getCustomRecipe(Authentication authentication) {
        User user = getUser(authentication);
        AiAnalysisResponse response = aiAnalysisService.getAnalysis(user, AiAnalysisType.CUSTOM_RECIPE);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * AI 맞춤 레시피 새로고침
     */
    @PostMapping("/custom-recipe/refresh")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> refreshCustomRecipe(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean force) {
        User user = getUser(authentication);
        AiAnalysisResponse response = aiAnalysisService.refreshAnalysis(user, AiAnalysisType.CUSTOM_RECIPE, force);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 냉장고 파먹기 레시피 생성
     */
    @PostMapping("/fridge-recipe")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> generateFridgeRecipe(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User user = getUser(authentication);
        String ingredients = request.getOrDefault("ingredients", "");
        
        if (ingredients.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("재료를 입력해주세요."));
        }
        
        AiAnalysisResponse response = aiAnalysisService.generateFridgeRecipe(user, ingredients);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 모든 AI 분석 조회 (한 번에)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, AiAnalysisResponse>>> getAllAnalysis(Authentication authentication) {
        User user = getUser(authentication);
        
        Map<String, AiAnalysisResponse> responses = Map.of(
            "exercisePlan", aiAnalysisService.getAnalysis(user, AiAnalysisType.WEEKLY_EXERCISE_PLAN),
            "customRecipe", aiAnalysisService.getAnalysis(user, AiAnalysisType.CUSTOM_RECIPE),
            "fridgeRecipe", aiAnalysisService.getAnalysis(user, AiAnalysisType.FRIDGE_RECIPE)
        );
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    /**
     * AI 하루 식단 계획 생성
     */
    @PostMapping("/daily-meal-plan")
    public ResponseEntity<ApiResponse<AiAnalysisResponse>> generateDailyMealPlan(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        User user = getUser(authentication);
        String preference = request.getOrDefault("preference", "균형 잡힌 하루 식단");
        
        AiAnalysisResponse response = aiAnalysisService.generateDailyMealPlan(user, preference);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 캐시 프리로드 (백그라운드에서 호출)
     * 앱 시작 시 미리 캐시를 준비해둠
     */
    @PostMapping("/preload")
    public ResponseEntity<ApiResponse<String>> preloadCache(Authentication authentication) {
        User user = getUser(authentication);
        
        // 백그라운드에서 실행 (비동기)
        new Thread(() -> {
            try {
                aiAnalysisService.getAnalysis(user, AiAnalysisType.WEEKLY_EXERCISE_PLAN);
                aiAnalysisService.getAnalysis(user, AiAnalysisType.CUSTOM_RECIPE);
                log.info("Cache preloaded for user {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to preload cache for user {}: {}", user.getId(), e.getMessage());
            }
        }).start();
        
        return ResponseEntity.ok(ApiResponse.success("캐시 프리로드 시작"));
    }

    /**
     * 최근 냉장고 레시피 조회 (앱 시작 시)
     */
    @GetMapping("/fridge-recipe/latest")
    public ResponseEntity<ApiResponse<FridgeRecipeHistoryResponse>> getLatestFridgeRecipe(Authentication authentication) {
        User user = getUser(authentication);
        return aiAnalysisService.getLatestFridgeRecipe(user)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }
    
    /**
     * 냉장고 레시피 히스토리 목록 조회
     */
    @GetMapping("/fridge-recipe/history")
    public ResponseEntity<ApiResponse<List<FridgeRecipeHistoryResponse>>> getFridgeRecipeHistory(Authentication authentication) {
        User user = getUser(authentication);
        List<FridgeRecipeHistoryResponse> history = aiAnalysisService.getFridgeRecipeHistory(user);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
