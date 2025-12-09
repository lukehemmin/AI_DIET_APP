package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.request.GroupCreateRequest;
import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.GroupResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, GroupResponse>>> createGroup(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody GroupCreateRequest request) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        GroupResponse groupResponse = groupService.createGroup(user, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("group", groupResponse)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<GroupResponse>>>> getAllGroups() {
        List<GroupResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(ApiResponse.success(Map.of("groups", groups)));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<com.lukehemmin.dodietapi.dto.response.GroupDetailResponse>> getGroupDetail(
            @PathVariable java.util.UUID groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        com.lukehemmin.dodietapi.dto.response.GroupDetailResponse groupDetail = groupService.getGroupDetail(groupId, user);
        return ResponseEntity.ok(ApiResponse.success(groupDetail));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, List<GroupResponse>>>> getMyGroups(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<GroupResponse> groups = groupService.getMyGroups(user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("groups", groups)));
    }

    /**
     * 그룹 정보 수정 (방장만)
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Map<String, GroupResponse>>> updateGroup(
            @PathVariable java.util.UUID groupId,
            @RequestBody GroupCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        GroupResponse groupResponse = groupService.updateGroup(groupId, user, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("group", groupResponse)));
    }

    /**
     * 그룹 나가기
     */
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse<Map<String, String>>> leaveGroup(
            @PathVariable java.util.UUID groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        groupService.leaveGroup(groupId, user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "그룹에서 나갔습니다.")));
    }

    /**
     * 그룹 삭제 (방장만)
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteGroup(
            @PathVariable java.util.UUID groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        groupService.deleteGroup(groupId, user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "그룹이 삭제되었습니다.")));
    }

    /**
     * 멤버 초대
     */
    @PostMapping("/{groupId}/invite")
    public ResponseEntity<ApiResponse<Map<String, String>>> inviteMember(
            @PathVariable java.util.UUID groupId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        java.util.UUID inviteeId = java.util.UUID.fromString(request.get("userId"));
        groupService.inviteMember(groupId, user, inviteeId, userRepository);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "멤버가 초대되었습니다.")));
    }

    /**
     * 멤버 강퇴 (방장만)
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> kickMember(
            @PathVariable java.util.UUID groupId,
            @PathVariable java.util.UUID memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        groupService.kickMember(groupId, user, memberId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "멤버가 강퇴되었습니다.")));
    }

    /**
     * 현재 사용자의 그룹 내 역할 확인
     */
    @GetMapping("/{groupId}/role")
    public ResponseEntity<ApiResponse<Map<String, String>>> getUserRole(
            @PathVariable java.util.UUID groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String role = groupService.getUserRole(groupId, user);
        return ResponseEntity.ok(ApiResponse.success(Map.of("role", role != null ? role : "NONE")));
    }
}
