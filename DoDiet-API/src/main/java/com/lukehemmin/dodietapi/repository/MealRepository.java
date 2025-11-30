package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealRepository extends JpaRepository<Meal, UUID> {
    List<Meal> findByUserIdAndDate(UUID userId, LocalDate date);
    List<Meal> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
}
