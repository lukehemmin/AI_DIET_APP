package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.request.AddCommentRequest;
import com.lukehemmin.dodietapi.dto.request.AddReactionRequest;
import com.lukehemmin.dodietapi.dto.request.ShareMealRequest;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.FeedCommentResponse;
import com.lukehemmin.dodietapi.dto.response.GroupFeedResponse;
import com.lukehemmin.dodietapi.entity.ReactionType;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.GroupFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupFeedController {

    private final GroupFeedService feedService;
    private final UserRepository userRepository;

    /**
     * 그룹 피드 목록 조회
     */
    @GetMapping("/{groupId}/feeds")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGroupFeeds(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        List<GroupFeedResponse> feeds = feedService.getGroupFeeds(groupId, user, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "feeds", feeds,
                "page", page,
                "size", size
        )));
    }

    /**
     * 식단 그룹에 공유
     */
    @PostMapping("/share-meal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> shareMealToGroups(
            @RequestBody ShareMealRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        List<GroupFeedResponse> feeds = feedService.shareMealToGroups(user, request);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "sharedFeeds", feeds,
                "message", feeds.size() + "개 그룹에 공유되었습니다."
        )));
    }

    /**
     * 피드에 댓글 추가
     */
    @PostMapping("/feeds/{feedId}/comments")
    public ResponseEntity<ApiResponse<FeedCommentResponse>> addComment(
            @PathVariable UUID feedId,
            @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        FeedCommentResponse comment = feedService.addComment(feedId, user, request);
        
        return ResponseEntity.ok(ApiResponse.success(comment));
    }

    /**
     * 피드 댓글 목록 조회
     */
    @GetMapping("/feeds/{feedId}/comments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComments(
            @PathVariable UUID feedId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        List<FeedCommentResponse> comments = feedService.getComments(feedId, user);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("comments", comments)));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        feedService.deleteComment(commentId, user);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "댓글이 삭제되었습니다.")));
    }

    /**
     * 피드에 반응 토글 (추가/제거)
     */
    @PostMapping("/feeds/{feedId}/reactions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleReaction(
            @PathVariable UUID feedId,
            @RequestBody AddReactionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        ReactionType reactionType = ReactionType.valueOf(request.getReactionType());
        boolean added = feedService.toggleReaction(feedId, user, reactionType);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "reactionType", reactionType.name(),
                "added", added,
                "message", added ? "반응이 추가되었습니다." : "반응이 제거되었습니다."
        )));
    }

    /**
     * 피드 삭제
     */
    @DeleteMapping("/feeds/{feedId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteFeed(
            @PathVariable UUID feedId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = getUser(userPrincipal);
        feedService.deleteFeed(feedId, user);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "피드가 삭제되었습니다.")));
    }

    private User getUser(UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
