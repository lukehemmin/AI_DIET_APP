package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.ChallengeResponse;
import com.lukehemmin.dodietapi.dto.response.UserChallengeResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<ChallengeResponse>>>> getAllChallenges() {
        List<ChallengeResponse> challenges = challengeService.getAllActiveChallenges();
        return ResponseEntity.ok(ApiResponse.success(Map.of("challenges", challenges)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, List<UserChallengeResponse>>>> getMyChallenges(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserChallengeResponse> myChallenges = challengeService.getUserChallenges(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("myChallenges", myChallenges)));
    }

    @PostMapping("/{challengeId}/join")
    public ResponseEntity<ApiResponse<Map<String, UserChallengeResponse>>> joinChallenge(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String challengeId) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserChallengeResponse response = challengeService.joinChallenge(user, challengeId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("challenge", response)));
    }
}
