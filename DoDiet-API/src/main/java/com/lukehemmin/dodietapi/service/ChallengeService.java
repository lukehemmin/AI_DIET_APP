package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.ChallengeResponse;
import com.lukehemmin.dodietapi.dto.response.UserChallengeResponse;
import com.lukehemmin.dodietapi.entity.Challenge;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.UserChallengeProgress;
import com.lukehemmin.dodietapi.repository.ChallengeRepository;
import com.lukehemmin.dodietapi.repository.UserChallengeProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeProgressRepository userChallengeProgressRepository;

    public List<ChallengeResponse> getAllActiveChallenges() {
        return challengeRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UserChallengeResponse> getUserChallenges(User user) {
        return userChallengeProgressRepository.findByUser(user).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserChallengeResponse joinChallenge(User user, String challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        if (userChallengeProgressRepository.findByUserAndChallenge(user, challenge).isPresent()) {
            throw new IllegalArgumentException("Already joined this challenge");
        }

        UserChallengeProgress progress = new UserChallengeProgress();
        progress.setUser(user);
        progress.setChallenge(challenge);
        progress.setCurrent(0);
        progress.setProgress(0.0);

        UserChallengeProgress saved = userChallengeProgressRepository.save(progress);
        return mapToUserResponse(saved);
    }

    private ChallengeResponse mapToResponse(Challenge challenge) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .icon(challenge.getIcon())
                .goal(challenge.getGoal())
                .goalUnit(challenge.getGoalUnit())
                .build();
    }

    private UserChallengeResponse mapToUserResponse(UserChallengeProgress progress) {
        return UserChallengeResponse.builder()
                .challenge(mapToResponse(progress.getChallenge()))
                .current(progress.getCurrent())
                .progress(progress.getProgress())
                .status(progress.getStatus())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .build();
    }
    
    // Method to be called when user performs an action (e.g., logs a meal)
    @Transactional
    public void updateProgress(User user, String challengeId, int increment) {
        userChallengeProgressRepository.findByUser(user).stream()
                .filter(p -> p.getChallenge().getId().equals(challengeId))
                .findFirst()
                .ifPresent(p -> {
                    p.setCurrent(p.getCurrent() + increment);
                    p.updateProgress();
                    userChallengeProgressRepository.save(p);
                });
    }
}
