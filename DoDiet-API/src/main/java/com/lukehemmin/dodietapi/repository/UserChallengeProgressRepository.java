package com.lukehemmin.dodietapi.repository;

import com.lukehemmin.dodietapi.entity.Challenge;
import com.lukehemmin.dodietapi.entity.ChallengeStatus;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.UserChallengeProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserChallengeProgressRepository extends JpaRepository<UserChallengeProgress, UUID> {
    List<UserChallengeProgress> findByUser(User user);
    Optional<UserChallengeProgress> findByUserAndChallenge(User user, Challenge challenge);
    
    // 완료된 챌린지 수 조회 (status가 COMPLETED인 것)
    long countByUserAndStatus(User user, ChallengeStatus status);
}
