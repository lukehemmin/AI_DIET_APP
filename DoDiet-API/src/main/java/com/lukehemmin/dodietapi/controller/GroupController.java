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
}
