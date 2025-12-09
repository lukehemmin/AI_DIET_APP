package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.DietAnalyticsResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.service.DietAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class DietAnalyticsController {

    private final DietAnalyticsService dietAnalyticsService;
    private final UserRepository userRepository;

    /**
     * 식단 분석 데이터 조회
     * @param days 분석 기간 (기본 7일)
     */
    @GetMapping("/diet")
    public ResponseEntity<ApiResponse<DietAnalyticsResponse>> getDietAnalytics(
            Authentication authentication,
            @RequestParam(defaultValue = "7") int days) {
        
        User user = getUser(authentication);
        DietAnalyticsResponse response = dietAnalyticsService.getAnalytics(user, days);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
