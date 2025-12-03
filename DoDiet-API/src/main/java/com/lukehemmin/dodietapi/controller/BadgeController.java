package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.BadgeResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getAllBadges() {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getAllBadges()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> getMyBadges(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(badgeService.getUserBadges(user)));
    }
}
