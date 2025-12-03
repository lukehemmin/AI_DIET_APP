package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {
    List<Challenge> findByIsActiveTrue();
}
