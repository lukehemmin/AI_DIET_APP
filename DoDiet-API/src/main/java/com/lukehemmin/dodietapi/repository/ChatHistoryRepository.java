package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.ChatHistory;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, UUID> {
    
    // 사용자의 최근 대화 조회 (생성일 내림차순)
    List<ChatHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // 사용자의 모든 대화 개수
    long countByUser(User user);
    
    // 사용자의 모든 대화 삭제
    void deleteByUser(User user);
}
