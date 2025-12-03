package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.request.FriendRequest;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.FriendResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserRepository userRepository;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody FriendRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        friendshipService.sendFriendRequest(user, request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID friendshipId) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        friendshipService.acceptFriendRequest(user, friendshipId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<FriendResponse>>>> getFriends(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FriendResponse> friends = friendshipService.getFriends(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("friends", friends)));
    }

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Map<String, List<FriendResponse>>>> getPendingRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FriendResponse> requests = friendshipService.getPendingRequests(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("requests", requests)));
    }
}
