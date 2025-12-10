package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.request.MealCreateRequest;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.MealResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.GeminiService;
import com.lukehemmin.dodietapi.service.MealService;
import com.lukehemmin.dodietapi.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final GeminiService geminiService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzeMeal(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam("image") MultipartFile image) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Store image
        String storedFileName = storageService.store(image, "meals", user.getId().toString());
        
        // 2. Generate full URL (force HTTPS for production)
        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .scheme("https")
                .path("/uploads/")
                .path(storedFileName)
                .toUriString();

        // 3. Analyze
        List<Map<String, Object>> analysisResults = geminiService.analyzeFoodImage(storedFileName);
        
        // 4. Construct response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("analysisResults", analysisResults);
        responseData.put("imageUrl", fileUrl);
        responseData.put("thumbnailUrl", fileUrl); // Use same for now

        return ResponseEntity.ok(ApiResponse.success(responseData));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, List<MealResponse>>>> createMeals(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody MealCreateRequest request) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MealResponse> meals = mealService.createMeals(user, request);
        
        Map<String, List<MealResponse>> data = new HashMap<>();
        data.put("meals", meals);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMeals(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<MealResponse> meals;
        if (startDate != null && endDate != null) {
            meals = mealService.getMealsByDateRange(user, startDate, endDate);
        } else if (startDate != null) {
            meals = mealService.getMealsByDate(user, startDate);
        } else {
            // Default to today
            meals = mealService.getMealsByDate(user, LocalDate.now());
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("meals", meals);
        data.put("totalCount", meals.size());
        
        // Calculate summary
        double totalKcal = meals.stream().mapToDouble(MealResponse::getKcal).sum();
        data.put("summary", Map.of("totalKcal", totalKcal));
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
