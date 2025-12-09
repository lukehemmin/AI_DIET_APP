package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.Group;
import com.lukehemmin.dodietapi.entity.GroupMember;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private String challenge;
    private int challengeDays;
    private int progress;
    private int targetValue;
    private int currentValue;
    private String userRole;  // 현재 사용자의 역할 (LEADER, MEMBER, NONE)
    private List<GroupMemberResponse> members;
    private LocalDateTime challengeStartDate;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class GroupMemberResponse {
        private UUID userId;
        private String name;
        private String profileImageUrl;
        private String role;
        private LocalDateTime joinedAt;

        public static GroupMemberResponse from(GroupMember member) {
            return GroupMemberResponse.builder()
                    .userId(member.getUser().getId())
                    .name(member.getUser().getName())
                    .profileImageUrl(member.getUser().getProfileImageUrl())
                    .role(member.getRole().name())
                    .joinedAt(member.getJoinedAt())
                    .build();
        }
    }

    public static GroupDetailResponse from(Group group, int progress, String userRole) {
        int targetValue = group.getChallengeDays() != null ? group.getChallengeDays() : 7;
        int currentValue = (int) Math.round(progress * targetValue / 100.0);
        
        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .challenge(group.getChallenge())
                .challengeDays(targetValue)
                .progress(progress)
                .targetValue(targetValue)
                .currentValue(currentValue)
                .userRole(userRole)
                .members(group.getMembers() != null 
                        ? group.getMembers().stream()
                            .map(GroupMemberResponse::from)
                            .collect(Collectors.toList())
                        : List.of())
                .challengeStartDate(group.getChallengeStartDate())
                .createdAt(group.getCreatedAt())
                .build();
    }

    // 기존 호환성을 위한 오버로드
    public static GroupDetailResponse from(Group group) {
        return from(group, 0, "NONE");
    }
}
