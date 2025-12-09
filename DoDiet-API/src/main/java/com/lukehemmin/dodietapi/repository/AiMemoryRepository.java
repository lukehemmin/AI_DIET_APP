package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.AiMemory;
import com.lukehemmin.dodietapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiMemoryRepository extends JpaRepository<AiMemory, UUID> {
    
    // 사용자의 모든 메모리 조회 (중요도 내림차순)
    List<AiMemory> findByUserOrderByImportanceDescCreatedAtDesc(User user);
    
    // 사용자의 특정 카테고리 메모리 조회
    List<AiMemory> findByUserAndCategoryOrderByImportanceDesc(User user, String category);
    
    // 사용자의 상위 N개 중요 메모리 조회
    @Query("SELECT m FROM AiMemory m WHERE m.user = :user ORDER BY m.importance DESC, m.updatedAt DESC")
    List<AiMemory> findTopMemoriesByUser(@Param("user") User user);
    
    // 제목으로 기존 메모리 찾기 (업데이트용)
    Optional<AiMemory> findByUserAndTitle(User user, String title);
    
    // 사용자의 메모리 개수
    long countByUser(User user);
    
    // 사용자의 특정 카테고리 메모리 삭제
    void deleteByUserAndCategory(User user, String category);
}
