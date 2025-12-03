package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.FriendshipStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FriendResponse {
    private UUID id; // Friendship ID
    private UUID userId;
    private String email;
    private String name;
    private FriendshipStatus status;
}
