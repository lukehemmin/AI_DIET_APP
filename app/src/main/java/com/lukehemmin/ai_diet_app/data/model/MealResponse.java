package com.lukehemmin.ai_diet_app.data.model;

import java.util.UUID;

public class MealResponse {
    private UUID id;
    private String foodItem;
    private Double servingSize;
    private Double kcal;
    private Double carbs;
    private Double protein;
    private Double fat;
    private String mealTime;
    private String date;
    private String imageUrl;
    private String thumbnailUrl;

    public UUID getId() { return id; }
    public String getFoodItem() { return foodItem; }
    public Double getServingSize() { return servingSize; }
    public Double getKcal() { return kcal; }
    public Double getCarbs() { return carbs; }
    public Double getProtein() { return protein; }
    public Double getFat() { return fat; }
    public String getMealTime() { return mealTime; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
