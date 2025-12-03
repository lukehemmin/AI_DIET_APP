package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.request.MealCreateRequest;
import com.lukehemmin.dodietapi.dto.response.MealResponse;
import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final BadgeService badgeService;

    @Transactional
    public List<MealResponse> createMeals(User user, MealCreateRequest request) {
        List<Meal> meals = request.getMeals().stream()
                .map(item -> {
                    Meal meal = new Meal();
                    meal.setUser(user);
                    meal.setFoodItem(item.getFoodItem());
                    meal.setServingSize(item.getServingSize());
                    meal.setKcal(item.getKcal());
                    meal.setCarbs(item.getCarbs());
                    meal.setProtein(item.getProtein());
                    meal.setFat(item.getFat());
                    meal.setMealTime(item.getMealTime());
                    meal.setDate(item.getDate());
                    meal.setImageUrl(item.getImageUrl());
                    // Thumbnail would be processed here or handled by client/storage service
                    return meal;
                })
                .collect(Collectors.toList());

        List<Meal> savedMeals = mealRepository.saveAll(meals);
        
        // Check badges
        badgeService.checkMealBadges(user);
        
        return savedMeals.stream()
                .map(MealResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MealResponse> getMealsByDate(User user, LocalDate date) {
        return mealRepository.findByUserIdAndDate(user.getId(), date).stream()
                .map(MealResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MealResponse> getMealsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        return mealRepository.findByUserIdAndDateBetween(user.getId(), startDate, endDate).stream()
                .map(MealResponse::from)
                .collect(Collectors.toList());
    }
}
