package com.lukehemmin.dodietapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("${app.gemini.api-key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    public List<Map<String, Object>> analyzeFoodImage(String imagePath) {
        log.info("Analyzing image at: {}", imagePath);

        try {
            // 1. Read image file and encode to Base64
            Path file = storageService.load(imagePath);
            byte[] fileContent = Files.readAllBytes(file);
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            String mimeType = Files.probeContentType(file);
            if (mimeType == null) mimeType = "image/jpeg";

            // 2. Construct Request Body
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", encodedString);

            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("inline_data", inlineData);

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", "Analyze this food image. Identify main dishes and side dishes. Group ingredients like boiled eggs or vegetables inside a main dish (e.g., Tteokbokki) into that main dish entry; do not list them separately. However, list distinct side dishes (e.g., Kimchi, Danmuji) or drinks (e.g., Soju) as separate items. IMPORTANT: Return all 'foodItem' names in KOREAN. For each item, estimate serving size (g), calories (kcal), carbs (g), protein (g), and fat (g). Also determine if each item is a snack (isSnack: true for snacks like cookies, chips, candy, chocolate, ice cream, cake, bread snacks, beverages like coffee/tea/soda; false for regular meals like rice, soup, stew, main dishes, side dishes). Return ONLY a JSON array with this structure: [{\"foodItem\": \"Korean Name\", \"servingSize\": number, \"kcal\": number, \"carbs\": number, \"protein\": number, \"fat\": number, \"isSnack\": boolean}, ...]. Do not include markdown formatting.");

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(textPart, imagePart));

            requestBody.put("contents", List.of(content));

            // 3. Call Gemini API
            String response = webClientBuilder.build()
                    .post()
                    .uri(GEMINI_API_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Gemini API Error: " + body)))
                    .bodyToMono(String.class)
                    .block();

            // 4. Parse Response
            return parseGeminiResponse(response);

        } catch (Exception e) {
            log.error("Error analyzing image with Gemini", e);
            throw new RuntimeException("Failed to analyze food image: " + e.getMessage(), e);
        }
    }

    public String chat(String message) {
        log.info("Chatting with Gemini: {}", message);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", "You are a helpful diet assistant. Answer the user's question about diet, nutrition, or health. User message: " + message);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(textPart));

            requestBody.put("contents", List.of(content));

            String response = webClientBuilder.build()
                    .post()
                    .uri(GEMINI_API_URL + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Gemini API Error: " + body)))
                    .bodyToMono(String.class)
                    .block();

            return parseChatResponse(response);

        } catch (Exception e) {
            log.error("Error chatting with Gemini", e);
            throw new RuntimeException("Failed to chat with Gemini: " + e.getMessage(), e);
        }
    }

    private String parseChatResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            return "죄송합니다. 답변을 생성할 수 없습니다.";
        } catch (Exception e) {
            log.error("Error parsing Gemini chat response: {}", response, e);
            throw new RuntimeException("Failed to parse chat response", e);
        }
    }

    private List<Map<String, Object>> parseGeminiResponse(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String text = parts.get(0).path("text").asText();
                    // Clean up markdown if present (e.g. ```json ... ```)
                    text = text.replaceAll("```json", "").replaceAll("```", "").trim();
                    
                    // Check if it's an array or object. If object, wrap in list.
                    if (text.startsWith("{")) {
                         Map<String, Object> singleResult = objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {});
                         return List.of(singleResult);
                    } else {
                         return objectMapper.readValue(text, new TypeReference<List<Map<String, Object>>>() {});
                    }
                }
            }
            throw new RuntimeException("Invalid response format from Gemini API");
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", response, e);
            throw new RuntimeException("Failed to parse analysis result", e);
        }
    }
}
