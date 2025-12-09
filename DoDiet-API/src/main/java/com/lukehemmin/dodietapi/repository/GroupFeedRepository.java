package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Group;
import com.lukehemmin.dodietapi.entity.GroupFeed;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupFeedRepository extends JpaRepository<GroupFeed, UUID> {
    
    // 그룹별 피드 조회 (페이징, 최신순)
    Page<GroupFeed> findByGroupOrderByCreatedAtDesc(Group group, Pageable pageable);
    
    // 그룹별 피드 목록 (최신순)
    List<GroupFeed> findByGroupOrderByCreatedAtDesc(Group group);
    
    // 특정 사용자의 피드
    List<GroupFeed> findByUserOrderByCreatedAtDesc(User user);
    
    // 특정 식단이 이미 공유되었는지 확인
    Optional<GroupFeed> findByGroupAndMeal(Group group, Meal meal);
    
    // 특정 기간 내 피드 수 (챌린지 진행률 계산용)
    @Query("SELECT COUNT(f) FROM GroupFeed f WHERE f.group = :group AND f.user = :user " +
           "AND f.createdAt >= :startDate AND f.createdAt <= :endDate")
    long countByGroupAndUserAndDateRange(
            @Param("group") Group group, 
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // 그룹의 식단 공유 피드 수 (기간별)
    @Query("SELECT COUNT(DISTINCT f.user) FROM GroupFeed f WHERE f.group = :group " +
           "AND f.feedType = 'MEAL_SHARE' AND f.createdAt >= :startDate")
    long countDistinctUsersWithMealShare(
            @Param("group") Group group,
            @Param("startDate") LocalDateTime startDate);
}
