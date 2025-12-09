package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.FeedComment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FeedCommentResponse {
    private UUID id;
    private UUID feedId;
    private UserSummary user;
    private String content;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
        private String profileImageUrl;
    }

    public static FeedCommentResponse from(FeedComment comment) {
        return FeedCommentResponse.builder()
                .id(comment.getId())
                .feedId(comment.getFeed().getId())
                .user(UserSummary.builder()
                        .id(comment.getUser().getId())
                        .name(comment.getUser().getName())
                        .profileImageUrl(comment.getUser().getProfileImageUrl())
                        .build())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
