package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.WaterIntake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaterIntakeRepository extends JpaRepository<WaterIntake, UUID> {
    Optional<WaterIntake> findByUserAndDate(User user, LocalDate date);
}
