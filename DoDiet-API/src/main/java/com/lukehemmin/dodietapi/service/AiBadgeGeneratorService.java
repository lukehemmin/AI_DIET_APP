package com.lukehemmin.dodietapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukehemmin.dodietapi.entity.DynamicBadge;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.ChatHistoryRepository;
import com.lukehemmin.dodietapi.repository.DynamicBadgeRepository;
import com.lukehemmin.dodietapi.repository.MealRepository;
import com.lukehemmin.dodietapi.repository.WaterIntakeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

/**
 * AI를 활용하여 사용자 맞춤형 업적을 동적으로 생성하는 서비스
 * 
 * - 사용자의 식단 기록, AI 대화 내용, 수분 섭취 패턴 등을 분석
 * - 사용자에게 적절한 동기부여가 될 수 있는 업적을 생성
 * - 업적 완료 시 새로운 업적을 자동 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiBadgeGeneratorService {

    private final DynamicBadgeRepository dynamicBadgeRepository;
    private final MealRepository mealRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final WaterIntakeRepository waterIntakeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.gemini.api-key}")
    private String geminiApiKey;

    // 기존 GeminiService와 동일한 모델 사용
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    private static final int MAX_PENDING_BADGES = 5;
    
    // 아이콘 타입들
    private static final List<String> ICON_TYPES = Arrays.asList(
        "nutrition", "water", "streak", "challenge", "ai_chat", "first_step"
    );
    
    // 조건 타입들
    private static final List<String> CONDITION_TYPES = Arrays.asList(
        "MEAL_COUNT", "STREAK_DAYS", "WATER_GOAL", "NUTRITION_BALANCE", 
        "CALORIE_TARGET", "PROTEIN_TARGET", "VEGGIE_COUNT"
    );

    /**
     * 사용자에게 새로운 AI 생성 업적이 필요한지 확인하고 생성
     */
    @Transactional
    public List<DynamicBadge> checkAndGenerateBadges(User user) {
        long pendingCount = dynamicBadgeRepository.countByUserAndIsUnlockedFalse(user);
        
        if (pendingCount >= MAX_PENDING_BADGES) {
            log.debug("User {} already has {} pending badges", user.getId(), pendingCount);
            return Collections.emptyList();
        }
        
        int badgesToGenerate = (int) (MAX_PENDING_BADGES - pendingCount);
        return generateBadgesForUser(user, badgesToGenerate);
    }

    /**
     * 사용자 데이터를 분석하여 맞춤형 업적 생성
     */
    @Transactional
    public List<DynamicBadge> generateBadgesForUser(User user, int count) {
        try {
            // 사용자 컨텍스트 수집
            String userContext = buildUserContext(user);
            
            // AI에게 업적 생성 요청
            String prompt = buildBadgeGenerationPrompt(userContext, count);
            String aiResponse = callGeminiApi(prompt);
            
            // AI 응답 파싱 및 업적 생성
            List<DynamicBadge> badges = parseAndCreateBadges(user, aiResponse, userContext);
            
            return dynamicBadgeRepository.saveAll(badges);
            
        } catch (Exception e) {
            log.error("Failed to generate AI badges for user {}", user.getId(), e);
            // 실패 시 기본 업적 생성
            return generateDefaultBadges(user, count);
        }
    }

    private String buildUserContext(User user) {
        StringBuilder context = new StringBuilder();
        
        // 기본 정보
        context.append("사용자 정보:\n");
        context.append("- 성별: ").append(user.getGender()).append("\n");
        context.append("- 나이: ").append(user.getAge()).append("세\n");
        context.append("- 활동량: ").append(user.getActivityLevel()).append("\n");
        
        // 최근 식단 기록
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        List<Meal> recentMeals = mealRepository.findByUserIdAndDateBetween(user.getId(), weekAgo, today);
        
        context.append("\n최근 7일간 식단 기록: ").append(recentMeals.size()).append("개\n");
        
        // 총 식단 기록 수
        long totalMeals = mealRepository.countByUserId(user.getId());
        context.append("총 식단 기록: ").append(totalMeals).append("개\n");
        
        // AI 대화 횟수
        long chatCount = chatHistoryRepository.countByUser(user);
        context.append("AI 대화 횟수: ").append(chatCount).append("회\n");
        
        // 수분 섭취 목표 달성 횟수
        long waterGoalDays = waterIntakeRepository.countGoalAchievedDays(user);
        context.append("수분 목표 달성 일수: ").append(waterGoalDays).append("일\n");
        
        return context.toString();
    }

    private String buildBadgeGenerationPrompt(String userContext, int count) {
        return String.format("""
            당신은 건강 앱의 업적 시스템 설계자입니다.
            아래 사용자 정보를 분석하여 동기부여가 될 수 있는 맞춤형 업적 %d개를 생성해주세요.
            
            %s
            
            업적 생성 규칙:
            1. 사용자가 현실적으로 달성 가능한 수준이어야 합니다
            2. 너무 쉽지도, 너무 어렵지도 않아야 합니다
            3. 사용자의 현재 기록 패턴을 고려하여 적절한 목표치를 설정하세요
            4. 재미있고 동기부여가 되는 이름과 설명을 작성하세요
            
            JSON 형식으로 응답해주세요:
            ```json
            {
              "badges": [
                {
                  "name": "업적 이름 (4-8글자)",
                  "description": "업적 설명 (20-40글자)",
                  "icon": "nutrition|water|streak|challenge|ai_chat|first_step 중 하나",
                  "conditionType": "MEAL_COUNT|STREAK_DAYS|WATER_GOAL|NUTRITION_BALANCE|CALORIE_TARGET 중 하나",
                  "targetValue": 목표 수치 (숫자),
                  "condition": "달성 조건 설명"
                }
              ]
            }
            ```
            
            JSON만 응답하고 다른 텍스트는 포함하지 마세요.
            """, count, userContext);
    }

    private String callGeminiApi(String prompt) throws Exception {
        String url = GEMINI_API_URL + "?key=" + geminiApiKey;
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        
        part.put("text", prompt);
        content.put("parts", Collections.singletonList(part));
        requestBody.put("contents", Collections.singletonList(content));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, String.class
        );
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText();
        }
        
        throw new RuntimeException("Gemini API call failed");
    }

    private List<DynamicBadge> parseAndCreateBadges(User user, String aiResponse, String context) {
        List<DynamicBadge> badges = new ArrayList<>();
        
        try {
            // JSON 블록 추출
            String json = aiResponse;
            if (aiResponse.contains("```json")) {
                json = aiResponse.substring(
                    aiResponse.indexOf("```json") + 7,
                    aiResponse.lastIndexOf("```")
                ).trim();
            }
            
            JsonNode root = objectMapper.readTree(json);
            JsonNode badgesNode = root.path("badges");
            
            for (JsonNode badgeNode : badgesNode) {
                DynamicBadge badge = new DynamicBadge();
                badge.setUser(user);
                badge.setName(badgeNode.path("name").asText("새로운 도전"));
                badge.setDescription(badgeNode.path("description").asText("목표를 달성해보세요!"));
                badge.setIcon(validateIcon(badgeNode.path("icon").asText("challenge")));
                badge.setConditionType(validateConditionType(badgeNode.path("conditionType").asText("MEAL_COUNT")));
                badge.setTargetValue(badgeNode.path("targetValue").asInt(5));
                badge.setCondition(badgeNode.path("condition").asText("목표를 달성하세요"));
                badge.setGenerationContext(context.substring(0, Math.min(context.length(), 1000)));
                badge.setIsAiGenerated(true);
                
                badges.add(badge);
            }
            
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", aiResponse, e);
        }
        
        return badges;
    }

    private String validateIcon(String icon) {
        return ICON_TYPES.contains(icon) ? icon : "challenge";
    }

    private String validateConditionType(String type) {
        return CONDITION_TYPES.contains(type) ? type : "MEAL_COUNT";
    }

    /**
     * AI 생성 실패 시 기본 업적 생성
     */
    private List<DynamicBadge> generateDefaultBadges(User user, int count) {
        List<DynamicBadge> badges = new ArrayList<>();
        
        String[][] defaultBadges = {
            {"꾸준한 기록가", "이번 주 5끼 식단을 기록해보세요!", "nutrition", "MEAL_COUNT", "5"},
            {"물 마시기 챌린지", "오늘 수분 목표를 달성해보세요!", "water", "WATER_GOAL", "1"},
            {"AI와 대화하기", "AI 영양사에게 조언을 구해보세요!", "ai_chat", "MEAL_COUNT", "1"},
            {"3일 연속 기록", "3일 연속으로 식단을 기록해보세요!", "streak", "STREAK_DAYS", "3"},
            {"건강한 한 주", "이번 주 매일 식단을 기록해보세요!", "streak", "STREAK_DAYS", "7"}
        };
        
        for (int i = 0; i < Math.min(count, defaultBadges.length); i++) {
            DynamicBadge badge = new DynamicBadge();
            badge.setUser(user);
            badge.setName(defaultBadges[i][0]);
            badge.setDescription(defaultBadges[i][1]);
            badge.setIcon(defaultBadges[i][2]);
            badge.setConditionType(defaultBadges[i][3]);
            badge.setTargetValue(Integer.parseInt(defaultBadges[i][4]));
            badge.setCondition(defaultBadges[i][1]);
            badge.setGenerationContext("default_generation");
            badge.setIsAiGenerated(false);
            
            badges.add(badge);
        }
        
        return dynamicBadgeRepository.saveAll(badges);
    }

    /**
     * 동적 업적 진행 상황 업데이트
     */
    @Transactional
    public void updateBadgeProgress(User user) {
        List<DynamicBadge> pendingBadges = dynamicBadgeRepository.findByUserAndIsUnlockedFalse(user);
        
        for (DynamicBadge badge : pendingBadges) {
            int currentValue = calculateCurrentValue(user, badge.getConditionType());
            badge.checkProgress(currentValue);
        }
        
        dynamicBadgeRepository.saveAll(pendingBadges);
        
        // 완료된 업적이 있으면 새 업적 생성
        checkAndGenerateBadges(user);
    }

    private int calculateCurrentValue(User user, String conditionType) {
        LocalDate today = LocalDate.now();
        
        switch (conditionType) {
            case "MEAL_COUNT":
                LocalDate weekAgo = today.minusDays(7);
                return (int) mealRepository.findByUserIdAndDateBetween(user.getId(), weekAgo, today).size();
                
            case "STREAK_DAYS":
                // 연속 기록 일수 계산
                LocalDate thirtyDaysAgo = today.minusDays(30);
                List<Meal> meals = mealRepository.findByUserIdAndDateBetween(user.getId(), thirtyDaysAgo, today);
                return calculateStreak(meals);
                
            case "WATER_GOAL":
                return (int) waterIntakeRepository.countGoalAchievedDays(user);
                
            default:
                return 0;
        }
    }

    private int calculateStreak(List<Meal> meals) {
        if (meals.isEmpty()) return 0;
        
        Set<LocalDate> dates = new TreeSet<>();
        meals.forEach(m -> dates.add(m.getDate()));
        
        List<LocalDate> sortedDates = new ArrayList<>(dates);
        Collections.sort(sortedDates, Collections.reverseOrder());
        
        int streak = 1;
        for (int i = 0; i < sortedDates.size() - 1; i++) {
            if (sortedDates.get(i).minusDays(1).equals(sortedDates.get(i + 1))) {
                streak++;
            } else {
                break;
            }
        }
        
        return streak;
    }
}
