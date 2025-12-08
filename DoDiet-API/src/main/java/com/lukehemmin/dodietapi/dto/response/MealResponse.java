package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.Meal;
import com.lukehemmin.dodietapi.entity.MealTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealResponse {
    private UUID id;
    private String foodItem;
    private Double servingSize;
    private Double kcal;
    private Double carbs;
    private Double protein;
    private Double fat;
    private MealTime mealTime;
    private LocalDate date;
    private String imageUrl;
    private String thumbnailUrl;
    private LocalDateTime createdAt;

    public static MealResponse from(Meal meal) {
        return MealResponse.builder()
                .id(meal.getId())
                .foodItem(meal.getFoodItem())
                .servingSize(meal.getServingSize())
                .kcal(meal.getKcal())
                .carbs(meal.getCarbs())
                .protein(meal.getProtein())
                .fat(meal.getFat())
                .mealTime(meal.getMealTime())
                .date(meal.getDate())
                .imageUrl(meal.getImageUrl())
                .thumbnailUrl(meal.getThumbnailUrl())
                .createdAt(meal.getCreatedAt())
                .build();
    }
}
