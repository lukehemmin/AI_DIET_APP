package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.Group;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GroupResponse {
    private UUID id;
    private String name;
    private String description;
    private String challenge;
    private int memberCount;
    private LocalDateTime createdAt;

    public static GroupResponse from(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .challenge(group.getChallenge())
                .memberCount(group.getMembers() != null ? group.getMembers().size() : 0)
                .createdAt(group.getCreatedAt())
                .build();
    }
}
