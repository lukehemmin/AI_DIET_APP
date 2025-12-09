package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.DynamicBadge;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DynamicBadgeRepository extends JpaRepository<DynamicBadge, UUID> {
    
    // 사용자의 모든 동적 업적 조회
    List<DynamicBadge> findByUserOrderByCreatedAtDesc(User user);
    
    // 사용자의 미완료 동적 업적 조회
    List<DynamicBadge> findByUserAndIsUnlockedFalse(User user);
    
    // 사용자의 완료된 동적 업적 조회
    List<DynamicBadge> findByUserAndIsUnlockedTrue(User user);
    
    // 사용자의 동적 업적 개수
    long countByUser(User user);
    
    // 사용자의 미완료 동적 업적 개수
    long countByUserAndIsUnlockedFalse(User user);
}
