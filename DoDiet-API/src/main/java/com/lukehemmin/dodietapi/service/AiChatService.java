package com.lukehemmin.dodietapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lukehemmin.dodietapi.entity.AiMemory;
import com.lukehemmin.dodietapi.entity.ChatHistory;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.AiMemoryRepository;
import com.lukehemmin.dodietapi.repository.ChatHistoryRepository;
import com.lukehemmin.dodietapi.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatService {

    @Value("${app.gemini.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiMemoryRepository memoryRepository;
    private final MealRepository mealRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    /**
     * AI 채팅 - Function Calling 지원
     */
    @Transactional
    public String chat(User user, String userMessage) {
        log.info("AI Chat for user {}: {}", user.getId(), userMessage);

        try {
            // 1. 사용자의 기존 메모리 로드
            List<AiMemory> memories = memoryRepository.findByUserOrderByImportanceDescCreatedAtDesc(user);
            String memoryContext = buildMemoryContext(memories);

            // 2. Gemini API 호출 (Function Calling 포함)
            String response = callGeminiWithFunctions(user, userMessage, memoryContext);

            // 3. 대화 히스토리 저장
            chatHistoryRepository.save(new ChatHistory(user, userMessage, response));

            return response;

        } catch (Exception e) {
            log.error("Error in AI chat", e);
            return "죄송합니다. 오류가 발생했습니다: " + e.getMessage();
        }
    }

    private String callGeminiWithFunctions(User user, String userMessage, String memoryContext) throws Exception {
        // Function 정의
        ArrayNode functions = objectMapper.createArrayNode();
        functions.add(createGetUserProfileFunction());
        functions.add(createGetMealHistoryFunction());
        functions.add(createSaveMemoryFunction());
        functions.add(createDeleteMemoryFunction());

        // 시스템 프롬프트
        String systemPrompt = buildSystemPrompt(memoryContext);

        // 요청 본문 구성
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // contents
        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode userContent = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode textPart = objectMapper.createObjectNode();
        textPart.put("text", userMessage);
        parts.add(textPart);
        userContent.set("parts", parts);
        userContent.put("role", "user");
        contents.add(userContent);
        requestBody.set("contents", contents);

        // system instruction
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode systemParts = objectMapper.createArrayNode();
        ObjectNode systemTextPart = objectMapper.createObjectNode();
        systemTextPart.put("text", systemPrompt);
        systemParts.add(systemTextPart);
        systemInstruction.set("parts", systemParts);
        requestBody.set("systemInstruction", systemInstruction);

        // tools
        ArrayNode tools = objectMapper.createArrayNode();
        ObjectNode toolsObj = objectMapper.createObjectNode();
        toolsObj.set("functionDeclarations", functions);
        tools.add(toolsObj);
        requestBody.set("tools", tools);

        // API 호출
        String responseStr = webClientBuilder.build()
                .post()
                .uri(GEMINI_API_URL + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 응답 처리 (Function Call 처리 포함)
        return processGeminiResponse(user, responseStr, userMessage, memoryContext);
    }

    private String processGeminiResponse(User user, String responseStr, String originalMessage, String memoryContext) throws Exception {
        JsonNode root = objectMapper.readTree(responseStr);
        JsonNode candidates = root.path("candidates");
        
        if (!candidates.isArray() || candidates.isEmpty()) {
            log.warn("Gemini response has no candidates");
            return "응답을 생성할 수 없습니다.";
        }

        JsonNode content = candidates.get(0).path("content");
        JsonNode parts = content.path("parts");

        if (!parts.isArray() || parts.isEmpty()) {
            log.warn("Gemini response has no parts");
            return "응답을 생성할 수 없습니다.";
        }

        // 모든 parts를 검사하여 functionCall 찾기
        // Gemini는 텍스트와 functionCall을 동시에 반환할 수 있음
        JsonNode functionCallPart = null;
        StringBuilder textResponse = new StringBuilder();
        
        for (JsonNode part : parts) {
            if (part.has("functionCall")) {
                functionCallPart = part.path("functionCall");
                log.info("Found functionCall in parts: {}", functionCallPart.path("name").asText());
            } else if (part.has("text")) {
                String text = part.path("text").asText("");
                if (!text.isEmpty()) {
                    textResponse.append(text);
                }
            }
        }

        // Function Call이 있으면 실행
        if (functionCallPart != null) {
            String functionName = functionCallPart.path("name").asText();
            JsonNode args = functionCallPart.path("args");

            log.info("Executing function call: {} with args: {}", functionName, args);

            // Function 실행
            String functionResult = executeFunctionCall(user, functionName, args);

            // Function 결과로 다시 API 호출하여 최종 응답 생성
            return callGeminiWithFunctionResult(user, originalMessage, functionName, functionResult, memoryContext);
        }

        // Function Call이 없으면 텍스트 응답 반환
        String finalText = textResponse.toString().trim();
        return finalText.isEmpty() ? "응답을 생성할 수 없습니다." : finalText;
    }

    private String executeFunctionCall(User user, String functionName, JsonNode args) {
        log.info("Executing function: {} with args: {}", functionName, args);

        switch (functionName) {
            case "get_user_profile":
                return getUserProfile(user);
            case "get_meal_history":
                int startDaysAgo = args.path("start_days_ago").asInt(0);
                int days = args.path("days").asInt(7);
                String mealType = args.path("meal_type").asText(null);
                return getMealHistory(user, startDaysAgo, days, mealType);
            case "save_memory":
                String category = args.path("category").asText("GENERAL");
                String title = args.path("title").asText();
                String content = args.path("content").asText();
                int importance = args.path("importance").asInt(5);
                return saveMemory(user, category, title, content, importance);
            case "delete_memory":
                String memoryTitle = args.path("title").asText();
                return deleteMemory(user, memoryTitle);
            default:
                return "알 수 없는 함수입니다.";
        }
    }

    private String callGeminiWithFunctionResult(User user, String originalMessage, String functionName, String functionResult, String memoryContext) throws Exception {
        String systemPrompt = buildSystemPrompt(memoryContext);

        ObjectNode requestBody = objectMapper.createObjectNode();
        
        ArrayNode contents = objectMapper.createArrayNode();
        
        // 사용자 메시지
        ObjectNode userContent = objectMapper.createObjectNode();
        ArrayNode userParts = objectMapper.createArrayNode();
        ObjectNode userTextPart = objectMapper.createObjectNode();
        userTextPart.put("text", originalMessage);
        userParts.add(userTextPart);
        userContent.set("parts", userParts);
        userContent.put("role", "user");
        contents.add(userContent);

        // Function Call 응답
        ObjectNode modelContent = objectMapper.createObjectNode();
        ArrayNode modelParts = objectMapper.createArrayNode();
        ObjectNode functionCallPart = objectMapper.createObjectNode();
        ObjectNode functionCallObj = objectMapper.createObjectNode();
        functionCallObj.put("name", functionName);
        functionCallObj.set("args", objectMapper.createObjectNode());
        functionCallPart.set("functionCall", functionCallObj);
        modelParts.add(functionCallPart);
        modelContent.set("parts", modelParts);
        modelContent.put("role", "model");
        contents.add(modelContent);

        // Function 결과
        ObjectNode functionResultContent = objectMapper.createObjectNode();
        ArrayNode functionResultParts = objectMapper.createArrayNode();
        ObjectNode functionResponsePart = objectMapper.createObjectNode();
        ObjectNode functionResponse = objectMapper.createObjectNode();
        functionResponse.put("name", functionName);
        ObjectNode responseObj = objectMapper.createObjectNode();
        responseObj.put("result", functionResult);
        functionResponse.set("response", responseObj);
        functionResponsePart.set("functionResponse", functionResponse);
        functionResultParts.add(functionResponsePart);
        functionResultContent.set("parts", functionResultParts);
        functionResultContent.put("role", "function");
        contents.add(functionResultContent);

        requestBody.set("contents", contents);

        // system instruction
        ObjectNode systemInstruction = objectMapper.createObjectNode();
        ArrayNode systemParts = objectMapper.createArrayNode();
        ObjectNode systemTextPart = objectMapper.createObjectNode();
        systemTextPart.put("text", systemPrompt);
        systemParts.add(systemTextPart);
        systemInstruction.set("parts", systemParts);
        requestBody.set("systemInstruction", systemInstruction);

        String responseStr = webClientBuilder.build()
                .post()
                .uri(GEMINI_API_URL + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(responseStr);
        JsonNode candidates = root.path("candidates");
        if (candidates.isArray() && !candidates.isEmpty()) {
            JsonNode text = candidates.get(0).path("content").path("parts").get(0).path("text");
            return text.asText("응답을 생성할 수 없습니다.");
        }
        return "응답을 생성할 수 없습니다.";
    }

    // ========== Function 정의 ==========

    private ObjectNode createGetUserProfileFunction() {
        ObjectNode func = objectMapper.createObjectNode();
        func.put("name", "get_user_profile");
        func.put("description", "사용자의 프로필 정보(이름, 나이, 성별, 키, 몸무게, 활동 수준, 목표 등)를 가져옵니다. 사용자 맞춤 답변을 위해 필요할 때 호출하세요.");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");
        params.set("properties", objectMapper.createObjectNode());
        func.set("parameters", params);
        return func;
    }

    private ObjectNode createGetMealHistoryFunction() {
        ObjectNode func = objectMapper.createObjectNode();
        func.put("name", "get_meal_history");
        func.put("description", "사용자의 식사 기록을 가져옵니다. 특정 날짜 또는 기간의 식사 내역을 조회할 수 있습니다.");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();
        
        ObjectNode startDaysAgoParam = objectMapper.createObjectNode();
        startDaysAgoParam.put("type", "integer");
        startDaysAgoParam.put("description", "조회 시작일 (며칠 전부터). 0=오늘, 1=어제, 2=그제. 기본값: 0");
        properties.set("start_days_ago", startDaysAgoParam);
        
        ObjectNode daysParam = objectMapper.createObjectNode();
        daysParam.put("type", "integer");
        daysParam.put("description", "조회할 일수 (1-90). 기본값: 7. 어제만 조회하려면 start_days_ago=1, days=1");
        properties.set("days", daysParam);

        ObjectNode mealTypeParam = objectMapper.createObjectNode();
        mealTypeParam.put("type", "string");
        mealTypeParam.put("description", "특정 식사 유형만 조회 (BREAKFAST, LUNCH, DINNER, SNACK). 생략하면 전체 조회.");
        properties.set("meal_type", mealTypeParam);

        params.set("properties", properties);
        func.set("parameters", params);
        return func;
    }

    private ObjectNode createSaveMemoryFunction() {
        ObjectNode func = objectMapper.createObjectNode();
        func.put("name", "save_memory");
        func.put("description", "사용자에 대한 중요한 정보를 기억합니다. 사용자의 선호도, 목표, 건강 정보, 중요한 사실 등을 저장하세요.");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();

        ObjectNode categoryParam = objectMapper.createObjectNode();
        categoryParam.put("type", "string");
        categoryParam.put("description", "카테고리 (PREFERENCE, GOAL, HEALTH_INFO, IMPORTANT_FACT, ALLERGY, DISLIKE)");
        properties.set("category", categoryParam);

        ObjectNode titleParam = objectMapper.createObjectNode();
        titleParam.put("type", "string");
        titleParam.put("description", "짧은 제목 (예: '단 음식 선호', '다이어트 목표')");
        properties.set("title", titleParam);

        ObjectNode contentParam = objectMapper.createObjectNode();
        contentParam.put("type", "string");
        contentParam.put("description", "기억할 내용 상세");
        properties.set("content", contentParam);

        ObjectNode importanceParam = objectMapper.createObjectNode();
        importanceParam.put("type", "integer");
        importanceParam.put("description", "중요도 (1-10, 높을수록 중요)");
        properties.set("importance", importanceParam);

        params.set("properties", properties);
        ArrayNode required = objectMapper.createArrayNode();
        required.add("title");
        required.add("content");
        params.set("required", required);
        func.set("parameters", params);
        return func;
    }

    private ObjectNode createDeleteMemoryFunction() {
        ObjectNode func = objectMapper.createObjectNode();
        func.put("name", "delete_memory");
        func.put("description", "더 이상 유효하지 않은 기억을 삭제합니다.");
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");
        ObjectNode properties = objectMapper.createObjectNode();

        ObjectNode titleParam = objectMapper.createObjectNode();
        titleParam.put("type", "string");
        titleParam.put("description", "삭제할 기억의 제목");
        properties.set("title", titleParam);

        params.set("properties", properties);
        ArrayNode required = objectMapper.createArrayNode();
        required.add("title");
        params.set("required", required);
        func.set("parameters", params);
        return func;
    }

    // ========== Function 구현 ==========

    private String getUserProfile(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자 프로필:\n");
        sb.append("- 이름: ").append(user.getName()).append("\n");
        sb.append("- 성별: ").append(user.getGender() != null ? user.getGender().name() : "미설정").append("\n");
        sb.append("- 나이: ").append(user.getAge() != null ? user.getAge() + "세" : "미설정").append("\n");
        sb.append("- 키: ").append(user.getHeight() != null ? user.getHeight() + "cm" : "미설정").append("\n");
        sb.append("- 몸무게: ").append(user.getWeight() != null ? user.getWeight() + "kg" : "미설정").append("\n");
        sb.append("- 활동 수준: ").append(user.getActivityLevel() != null ? user.getActivityLevel().name() : "미설정").append("\n");
        return sb.toString();
    }

    private String getMealHistory(User user, int startDaysAgo, int days, String mealType) {
        // startDaysAgo: 0=오늘부터, 1=어제부터, 2=그제부터
        // days: 조회할 일수
        // 예: "어제 뭐 먹었어?" → startDaysAgo=1, days=1 → 어제 하루만 조회
        // 예: "이번 주 뭐 먹었어?" → startDaysAgo=0, days=7 → 오늘부터 7일간 조회
        LocalDate endDate = LocalDate.now().minusDays(startDaysAgo);
        LocalDate startDate = endDate.minusDays(days - 1);
        
        log.info("getMealHistory: startDaysAgo={}, days={}, startDate={}, endDate={}", 
                 startDaysAgo, days, startDate, endDate);
        
        List<Meal> meals = mealRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
        
        if (mealType != null && !mealType.isEmpty()) {
            meals = meals.stream()
                    .filter(m -> m.getMealTime() != null && m.getMealTime().name().equals(mealType))
                    .collect(Collectors.toList());
        }

        // 날짜 설명 생성
        String dateDescription;
        if (startDaysAgo == 0 && days == 1) {
            dateDescription = "오늘";
        } else if (startDaysAgo == 1 && days == 1) {
            dateDescription = "어제";
        } else if (startDaysAgo == 0) {
            dateDescription = "최근 " + days + "일";
        } else {
            dateDescription = startDaysAgo + "일 전부터 " + days + "일간";
        }

        if (meals.isEmpty()) {
            return dateDescription + " 식사 기록이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(dateDescription).append(" 식사 기록 (").append(meals.size()).append("건):\n\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
        Map<LocalDate, List<Meal>> mealsByDate = meals.stream()
                .collect(Collectors.groupingBy(Meal::getDate, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<LocalDate, List<Meal>> entry : mealsByDate.entrySet()) {
            sb.append("📅 ").append(entry.getKey().format(formatter)).append(":\n");
            for (Meal meal : entry.getValue()) {
                sb.append("  - ").append(meal.getMealTime() != null ? meal.getMealTime().name() : "기타")
                  .append(": ").append(meal.getFoodItem())
                  .append(" (").append(Math.round(meal.getKcal())).append("kcal)\n");
            }
            sb.append("\n");
        }

        // 요약 통계
        double totalCalories = meals.stream().mapToDouble(Meal::getKcal).sum();
        double avgCalories = totalCalories / mealsByDate.size();
        sb.append("총 칼로리: ").append(Math.round(totalCalories)).append("kcal\n");
        sb.append("일 평균 칼로리: ").append(Math.round(avgCalories)).append("kcal");

        return sb.toString();
    }

    @Transactional
    public String saveMemory(User user, String category, String title, String content, int importance) {
        // 기존 같은 제목의 메모리가 있으면 업데이트
        Optional<AiMemory> existing = memoryRepository.findByUserAndTitle(user, title);
        if (existing.isPresent()) {
            AiMemory memory = existing.get();
            memory.setContent(content);
            memory.setCategory(category);
            memory.setImportance(importance);
            memoryRepository.save(memory);
            return "기억을 업데이트했습니다: " + title;
        }

        // 새 메모리 생성
        AiMemory memory = new AiMemory(user, category, title, content, importance);
        memoryRepository.save(memory);
        return "새로운 기억을 저장했습니다: " + title;
    }

    @Transactional
    public String deleteMemory(User user, String title) {
        Optional<AiMemory> existing = memoryRepository.findByUserAndTitle(user, title);
        if (existing.isPresent()) {
            memoryRepository.delete(existing.get());
            return "기억을 삭제했습니다: " + title;
        }
        return "해당 기억을 찾을 수 없습니다: " + title;
    }

    // ========== Helper ==========

    private String buildMemoryContext(List<AiMemory> memories) {
        if (memories.isEmpty()) {
            return "아직 이 사용자에 대해 기억하고 있는 정보가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("이 사용자에 대해 기억하고 있는 정보:\n");
        for (AiMemory m : memories) {
            sb.append("- [").append(m.getCategory()).append("] ").append(m.getTitle()).append(": ").append(m.getContent()).append("\n");
        }
        return sb.toString();
    }

    private String buildSystemPrompt(String memoryContext) {
        // 현재 날짜/시간 정보 (서버 기준, 한국 시간)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", java.util.Locale.KOREAN);
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("a h시 m분", java.util.Locale.KOREAN);
        String currentDate = now.format(dateFormatter);
        String currentTime = now.format(timeFormatter);
        String dayOfWeek = now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.KOREAN);
        
        return """
            당신은 친절하고 전문적인 AI 영양사/다이어트 코치입니다.
            
            **현재 시간 정보 (서버 기준):**
            - 오늘 날짜: """ + currentDate + """
            - 현재 시간: """ + currentTime + """
            - 요일: """ + dayOfWeek + """
            
            **역할:**
            - 사용자의 식단, 영양, 건강, 운동에 대한 질문에 답변합니다.
            - 사용자의 정보와 식사 기록을 바탕으로 맞춤형 조언을 제공합니다.
            - 중요한 정보는 기억해서 다음 대화에 활용합니다.
            
            **도구 사용 가이드:
            - get_user_profile: 사용자의 기본 정보(키, 몸무게, 나이 등)가 필요할 때
            - get_meal_history: 사용자의 식사 기록을 확인해야 할 때 (어제/오늘/이번 주 뭐 먹었는지 등)
            - save_memory: 사용자에 대한 중요한 정보(알레르기, 선호도, 목표 등)를 기억해야 할 때
            - delete_memory: 더 이상 유효하지 않은 정보를 삭제할 때
            
            **기억하고 있는 정보:**
            """ + memoryContext + """
            
            **[중요] 도구 사용 시 규칙:**
            - 도구가 필요한 질문에는 텍스트 응답 없이 바로 도구를 호출하세요!
            - "확인해볼게요", "조회해볼게요" 같은 중간 텍스트를 출력하지 마세요.
            - 도구 결과를 받은 후에만 완전한 답변을 작성하세요.
            - 잘못된 예: "네, 어제 드신 식사를 확인해볼게요" (텍스트) + get_meal_history (도구)
            - 올바른 예: get_meal_history만 호출 → 결과 받은 후 분석 답변 제공
            
            **응답 가이드:**
            - 한국어로 친근하게 답변하세요.
            - 마크다운 형식을 사용해서 읽기 쉽게 작성하세요.
            - 구체적이고 실용적인 조언을 제공하세요.
            
            **자연스러운 대화:**
            - 도구 이름(get_meal_history 등)을 직접 언급하지 마세요.
            - 도구 결과를 바탕으로 자연스럽게 분석 결과와 조언을 제공하세요.
            """;
    }
}
