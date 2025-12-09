package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.WaterIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaterIntakeRepository extends JpaRepository<WaterIntake, UUID> {
    Optional<WaterIntake> findByUserAndDate(User user, LocalDate date);
    
    // 특정 기간 내 수분 섭취 기록 조회
    List<WaterIntake> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    // 목표(8잔) 달성한 날짜 수 조회
    @Query("SELECT COUNT(w) FROM WaterIntake w WHERE w.user = :user AND w.glasses >= 8")
    long countGoalAchievedDays(@Param("user") User user);
}
