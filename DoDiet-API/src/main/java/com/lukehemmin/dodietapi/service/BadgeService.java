package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.BadgeResponse;
import com.lukehemmin.dodietapi.entity.BadgeDefinition;
import com.lukehemmin.dodietapi.entity.ChallengeStatus;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.WaterIntake;
import com.lukehemmin.dodietapi.repository.ChatHistoryRepository;
import com.lukehemmin.dodietapi.repository.MealRepository;
import com.lukehemmin.dodietapi.repository.UserChallengeProgressRepository;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.repository.WaterIntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final WaterIntakeRepository waterIntakeRepository;
    private final UserChallengeProgressRepository challengeProgressRepository;

    public List<BadgeResponse> getAllBadges() {
        return Arrays.stream(BadgeDefinition.values())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BadgeResponse> getUserBadges(User user) {
        Set<String> unlocked = user.getUnlockedBadges();
        return Arrays.stream(BadgeDefinition.values())
                .map(badge -> {
                    BadgeResponse response = mapToResponse(badge);
                    response.setUnlocked(unlocked.contains(badge.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    private BadgeResponse mapToResponse(BadgeDefinition badge) {
        return BadgeResponse.builder()
                .id(badge.getId())
                .name(badge.getName())
                .description(badge.getDescription())
                .icon(badge.getIcon())
                .isUnlocked(false) // Default
                .build();
    }

    @Transactional
    public void checkMealBadges(User user) {
        long mealCount = mealRepository.countByUserId(user.getId());
        Set<String> unlocked = user.getUnlockedBadges();
        boolean changed = false;

        // Check First Meal
        if (mealCount >= 1 && !unlocked.contains(BadgeDefinition.FIRST_MEAL.getId())) {
            unlocked.add(BadgeDefinition.FIRST_MEAL.getId());
            changed = true;
        }

        // Check Meal counts
        if (mealCount >= 10 && !unlocked.contains(BadgeDefinition.MEAL_10.getId())) {
            unlocked.add(BadgeDefinition.MEAL_10.getId());
            changed = true;
        }
        if (mealCount >= 50 && !unlocked.contains(BadgeDefinition.MEAL_50.getId())) {
            unlocked.add(BadgeDefinition.MEAL_50.getId());
            changed = true;
        }
        if (mealCount >= 100 && !unlocked.contains(BadgeDefinition.MEAL_MASTER.getId())) {
            unlocked.add(BadgeDefinition.MEAL_MASTER.getId());
            changed = true;
        }

        // Check Streaks - fetch meals for last 30 days
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);
        List<Meal> recentMeals = mealRepository.findByUserIdAndDateBetween(user.getId(), thirtyDaysAgo, today);
        
        List<LocalDate> dates = recentMeals.stream()
                .map(Meal::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
                
        int maxStreak = calculateMaxStreak(dates);

        if (maxStreak >= 3 && !unlocked.contains(BadgeDefinition.THREE_DAY_STREAK.getId())) {
            unlocked.add(BadgeDefinition.THREE_DAY_STREAK.getId());
            changed = true;
        }
        if (maxStreak >= 7 && !unlocked.contains(BadgeDefinition.SEVEN_DAY_STREAK.getId())) {
            unlocked.add(BadgeDefinition.SEVEN_DAY_STREAK.getId());
            changed = true;
        }
        if (maxStreak >= 14 && !unlocked.contains(BadgeDefinition.FOURTEEN_DAY_STREAK.getId())) {
            unlocked.add(BadgeDefinition.FOURTEEN_DAY_STREAK.getId());
            changed = true;
        }
        if (maxStreak >= 30 && !unlocked.contains(BadgeDefinition.THIRTY_DAY_STREAK.getId())) {
            unlocked.add(BadgeDefinition.THIRTY_DAY_STREAK.getId());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
    
    @Transactional
    public void checkChatBadges(User user) {
        long chatCount = chatHistoryRepository.countByUser(user);
        Set<String> unlocked = user.getUnlockedBadges();
        boolean changed = false;

        if (chatCount >= 1 && !unlocked.contains(BadgeDefinition.AI_FIRST_CHAT.getId())) {
            unlocked.add(BadgeDefinition.AI_FIRST_CHAT.getId());
            changed = true;
        }
        if (chatCount >= 10 && !unlocked.contains(BadgeDefinition.AI_CHAT_10.getId())) {
            unlocked.add(BadgeDefinition.AI_CHAT_10.getId());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
    
    @Transactional
    public void checkWaterBadges(User user) {
        Set<String> unlocked = user.getUnlockedBadges();
        boolean changed = false;

        // Check if user has achieved water goal at least once
        long goalAchievedDays = waterIntakeRepository.countGoalAchievedDays(user);
        
        if (goalAchievedDays >= 1 && !unlocked.contains(BadgeDefinition.WATER_MASTER.getId())) {
            unlocked.add(BadgeDefinition.WATER_MASTER.getId());
            changed = true;
        }
        
        // Check 7-day water streak
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        List<WaterIntake> recentIntakes = waterIntakeRepository.findByUserAndDateBetween(user, sevenDaysAgo, today);
        
        // 목표: 8잔 이상
        List<LocalDate> goalAchievedDates = recentIntakes.stream()
                .filter(w -> w.getGlasses() >= 8)
                .map(WaterIntake::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        int waterStreak = calculateMaxStreak(goalAchievedDates);
        
        if (waterStreak >= 7 && !unlocked.contains(BadgeDefinition.WATER_WEEK.getId())) {
            unlocked.add(BadgeDefinition.WATER_WEEK.getId());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
    
    @Transactional
    public void checkChallengeBadges(User user) {
        long completedChallenges = challengeProgressRepository.countByUserAndStatus(user, ChallengeStatus.COMPLETED);
        Set<String> unlocked = user.getUnlockedBadges();
        boolean changed = false;

        if (completedChallenges >= 1 && !unlocked.contains(BadgeDefinition.FIRST_CHALLENGE.getId())) {
            unlocked.add(BadgeDefinition.FIRST_CHALLENGE.getId());
            changed = true;
        }
        if (completedChallenges >= 5 && !unlocked.contains(BadgeDefinition.CHALLENGE_5.getId())) {
            unlocked.add(BadgeDefinition.CHALLENGE_5.getId());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
    
    /**
     * 모든 업적을 한번에 체크
     */
    @Transactional
    public void checkAllBadges(User user) {
        checkMealBadges(user);
        checkChatBadges(user);
        checkWaterBadges(user);
        checkChallengeBadges(user);
    }
    
    private int calculateMaxStreak(List<LocalDate> dates) {
        int maxStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;
        
        for (LocalDate date : dates) {
            if (lastDate == null) {
                currentStreak = 1;
            } else if (date.minusDays(1).equals(lastDate)) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }
            maxStreak = Math.max(maxStreak, currentStreak);
            lastDate = date;
        }
        return maxStreak;
    }
}
