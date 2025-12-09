package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.FeedReaction;
import com.lukehemmin.dodietapi.entity.GroupFeed;
import com.lukehemmin.dodietapi.entity.ReactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupFeedResponse {
    private UUID id;
    private UUID groupId;
    private String feedType;
    private UserSummary user;
    private MealSummary meal;
    private String content;
    private String imageUrl;
    private Map<String, Long> reactions;  // {"LIKE": 5, "HEART": 3, ...}
    private List<String> userReactions;   // 현재 사용자가 누른 반응
    private int commentCount;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
        private String profileImageUrl;
    }

    @Data
    @Builder
    public static class MealSummary {
        private UUID id;
        private String foodItem;
        private Double kcal;
        private String mealTime;
        private String imageUrl;
    }

    public static GroupFeedResponse from(GroupFeed feed, List<FeedReaction> allReactions, 
                                          List<FeedReaction> userReactions, int commentCount) {
        // 반응 타입별 개수 집계
        Map<String, Long> reactionCounts = allReactions.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getReactionType().name(),
                        Collectors.counting()
                ));

        // 사용자가 누른 반응 목록
        List<String> userReactionTypes = userReactions.stream()
                .map(r -> r.getReactionType().name())
                .collect(Collectors.toList());

        GroupFeedResponseBuilder builder = GroupFeedResponse.builder()
                .id(feed.getId())
                .groupId(feed.getGroup().getId())
                .feedType(feed.getFeedType().name())
                .user(UserSummary.builder()
                        .id(feed.getUser().getId())
                        .name(feed.getUser().getName())
                        .profileImageUrl(feed.getUser().getProfileImageUrl())
                        .build())
                .content(feed.getContent())
                .imageUrl(feed.getImageUrl())
                .reactions(reactionCounts)
                .userReactions(userReactionTypes)
                .commentCount(commentCount)
                .createdAt(feed.getCreatedAt());

        // 식단 공유인 경우 Meal 정보 추가
        if (feed.getMeal() != null) {
            builder.meal(MealSummary.builder()
                    .id(feed.getMeal().getId())
                    .foodItem(feed.getMeal().getFoodItem())
                    .kcal(feed.getMeal().getKcal())
                    .mealTime(feed.getMeal().getMealTime().name())
                    .imageUrl(feed.getMeal().getImageUrl())
                    .build());
        }

        return builder.build();
    }
}
