package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.BadgeResponse;
import com.lukehemmin.dodietapi.dto.response.DynamicBadgeResponse;
import com.lukehemmin.dodietapi.entity.DynamicBadge;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.DynamicBadgeRepository;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.AiBadgeGeneratorService;
import com.lukehemmin.dodietapi.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final AiBadgeGeneratorService aiBadgeGeneratorService;
    private final DynamicBadgeRepository dynamicBadgeRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges() {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getAllBadges()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getMyBadges(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 업적 체크 실행
        badgeService.checkAllBadges(user);
        
        return ResponseEntity.ok(ApiResponse.success(badgeService.getUserBadges(user)));
    }
    
    /**
     * 모든 업적 조회 (기본 + AI 생성)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMyBadges(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 업적 체크 및 AI 업적 진행 업데이트
        badgeService.checkAllBadges(user);
        aiBadgeGeneratorService.updateBadgeProgress(user);
        
        // 기본 업적
        List<BadgeResponse> staticBadges = badgeService.getUserBadges(user);
        
        // AI 생성 업적
        List<DynamicBadge> dynamicBadges = dynamicBadgeRepository.findByUserOrderByCreatedAtDesc(user);
        List<DynamicBadgeResponse> dynamicBadgeResponses = dynamicBadges.stream()
                .map(DynamicBadgeResponse::from)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("staticBadges", staticBadges);
        result.put("dynamicBadges", dynamicBadgeResponses);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * AI 맞춤 업적 생성 요청
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<DynamicBadgeResponse>>> generateAiBadges(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<DynamicBadge> newBadges = aiBadgeGeneratorService.checkAndGenerateBadges(user);
        List<DynamicBadgeResponse> responses = newBadges.stream()
                .map(DynamicBadgeResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    /**
     * AI 생성 업적만 조회
     */
    @GetMapping("/dynamic")
    public ResponseEntity<ApiResponse<List<DynamicBadgeResponse>>> getDynamicBadges(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 진행 상황 업데이트
        aiBadgeGeneratorService.updateBadgeProgress(user);
        
        List<DynamicBadge> badges = dynamicBadgeRepository.findByUserOrderByCreatedAtDesc(user);
        List<DynamicBadgeResponse> responses = badges.stream()
                .map(DynamicBadgeResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
