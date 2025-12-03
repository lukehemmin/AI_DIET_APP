package com.lukehemmin.ai_diet_app.data.model;

import java.util.List;

public class MealCreateRequest {

    private List<MealItemRequest> meals;

    public MealCreateRequest(List<MealItemRequest> meals) {
        this.meals = meals;
    }

    public List<MealItemRequest> getMeals() {
        return meals;
    }

    public static class MealItemRequest {
        private String foodItem;
        private Double servingSize;
        private Double kcal;
        private Double carbs;
        private Double protein;
        private Double fat;
        private String mealTime; // BREAKFAST, LUNCH, DINNER, SNACK
        private String date; // YYYY-MM-DD
        private String imageUrl;

        public MealItemRequest(String foodItem, Double servingSize, Double kcal, Double carbs, Double protein, Double fat, String mealTime, String date, String imageUrl) {
            this.foodItem = foodItem;
            this.servingSize = servingSize;
            this.kcal = kcal;
            this.carbs = carbs;
            this.protein = protein;
            this.fat = fat;
            this.mealTime = mealTime;
            this.date = date;
            this.imageUrl = imageUrl;
        }
    }
}
