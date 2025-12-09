package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.FeedComment;
import com.lukehemmin.dodietapi.entity.GroupFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedCommentRepository extends JpaRepository<FeedComment, UUID> {
    
    // 피드별 댓글 (최신순)
    List<FeedComment> findByFeedOrderByCreatedAtAsc(GroupFeed feed);
    
    // 피드의 댓글 수
    long countByFeed(GroupFeed feed);
}
