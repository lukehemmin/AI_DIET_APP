package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.BadgeResponse;
import com.lukehemmin.dodietapi.entity.BadgeDefinition;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.MealRepository;
import com.lukehemmin.dodietapi.repository.UserRepository;
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

        // Check Meal Master
        if (mealCount >= 100 && !unlocked.contains(BadgeDefinition.MEAL_MASTER.getId())) {
            unlocked.add(BadgeDefinition.MEAL_MASTER.getId());
            changed = true;
        }

        // Check Streaks
        // Simple check: fetch meals for last 7 days
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        List<Meal> recentMeals = mealRepository.findByUserIdAndDateBetween(user.getId(), sevenDaysAgo, today);
        
        long distinctDays = recentMeals.stream()
                .map(Meal::getDate)
                .distinct()
                .count();

        // Note: This logic is simplified. Real streak logic might be more complex (consecutive days).
        // But for "distinct days in last N days" (which is close to streak if user is consistent), it works for now.
        // Ideally we should sort dates and check consecutiveness.
        
        // Better Streak Logic:
        List<LocalDate> dates = recentMeals.stream()
                .map(Meal::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
                
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

        if (maxStreak >= 3 && !unlocked.contains(BadgeDefinition.THREE_DAY_STREAK.getId())) {
            unlocked.add(BadgeDefinition.THREE_DAY_STREAK.getId());
            changed = true;
        }

        if (maxStreak >= 7 && !unlocked.contains(BadgeDefinition.SEVEN_DAY_STREAK.getId())) {
            unlocked.add(BadgeDefinition.SEVEN_DAY_STREAK.getId());
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }
}
