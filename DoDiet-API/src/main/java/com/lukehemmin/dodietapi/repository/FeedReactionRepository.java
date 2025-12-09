package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.FeedReaction;
import com.lukehemmin.dodietapi.entity.GroupFeed;
import com.lukehemmin.dodietapi.entity.ReactionType;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedReactionRepository extends JpaRepository<FeedReaction, UUID> {
    
    // 피드의 모든 반응
    List<FeedReaction> findByFeed(GroupFeed feed);
    
    // 특정 사용자가 특정 피드에 남긴 특정 반응 찾기
    Optional<FeedReaction> findByFeedAndUserAndReactionType(GroupFeed feed, User user, ReactionType reactionType);
    
    // 특정 사용자가 특정 피드에 남긴 모든 반응
    List<FeedReaction> findByFeedAndUser(GroupFeed feed, User user);
    
    // 피드별 반응 타입별 개수
    @Query("SELECT r.reactionType, COUNT(r) FROM FeedReaction r WHERE r.feed = :feed GROUP BY r.reactionType")
    List<Object[]> countByFeedGroupByReactionType(@Param("feed") GroupFeed feed);
    
    // 피드의 총 반응 수
    long countByFeed(GroupFeed feed);
}
