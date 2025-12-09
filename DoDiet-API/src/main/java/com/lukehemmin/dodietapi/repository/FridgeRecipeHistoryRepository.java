package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.FridgeRecipeHistory;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FridgeRecipeHistoryRepository extends JpaRepository<FridgeRecipeHistory, UUID> {
    
    // 최신 레시피 1개 조회
    Optional<FridgeRecipeHistory> findTopByUserOrderByCreatedAtDesc(User user);
    
    // 히스토리 목록 조회 (최신순)
    List<FridgeRecipeHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 전체 히스토리 조회
    List<FridgeRecipeHistory> findByUserOrderByCreatedAtDesc(User user);
    
    // 히스토리 개수
    long countByUser(User user);
}
