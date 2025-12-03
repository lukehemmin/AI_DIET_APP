package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Challenge;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.UserChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserChallengeProgressRepository extends JpaRepository<UserChallengeProgress, UUID> {
    List<UserChallengeProgress> findByUser(User user);
    Optional<UserChallengeProgress> findByUserAndChallenge(User user, Challenge challenge);
}
