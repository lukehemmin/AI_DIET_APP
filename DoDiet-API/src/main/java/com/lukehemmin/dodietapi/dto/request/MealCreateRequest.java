package com.lukehemmin.dodietapi.dto.request;

import com.lukehemmin.dodietapi.entity.MealTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MealCreateRequest {

    @Valid
    @NotNull
    private List<MealItemRequest> meals;

    @Data
    public static class MealItemRequest {
        @NotBlank
        private String foodItem;

        @NotNull
        private Double servingSize;

        @NotNull
        private Double kcal;

        @NotNull
        private Double carbs;

        @NotNull
        private Double protein;

        @NotNull
        private Double fat;

        @NotNull
        private MealTime mealTime;

        @NotNull
        private LocalDate date;

        private String imageUrl;
    }
}
