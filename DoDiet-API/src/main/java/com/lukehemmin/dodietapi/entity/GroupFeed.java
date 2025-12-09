package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "group_feeds", indexes = {
    @Index(name = "idx_group_feed_group", columnList = "group_id"),
    @Index(name = "idx_group_feed_created", columnList = "created_at DESC")
})
@Getter @Setter
@NoArgsConstructor
public class GroupFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedType feedType;

    // 식단 공유인 경우 연결된 Meal
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private Meal meal;

    // 피드 내용 (챌린지 달성 메시지 등)
    @Column(columnDefinition = "TEXT")
    private String content;

    // 이미지 URL (식단 사진 등)
    @Column(length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "feed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedReaction> reactions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
