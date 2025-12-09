package com.lukehemmin.dodietapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feed_reactions", 
    indexes = {
        @Index(name = "idx_reaction_feed", columnList = "feed_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_feed_user_reaction", columnNames = {"feed_id", "user_id", "reaction_type"})
    }
)
@Getter @Setter
@NoArgsConstructor
public class FeedReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private GroupFeed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
