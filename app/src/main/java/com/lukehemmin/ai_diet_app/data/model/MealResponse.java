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
    private String createdAt;

    public UUID getId() { return id; }
    public String getFoodItem() { return foodItem; }
    public Double getServingSize() { return servingSize; }
    public Double getKcal() { return kcal != null ? kcal : 0.0; }
    public Double getCarbs() { return carbs != null ? carbs : 0.0; }
    public Double getProtein() { return protein != null ? protein : 0.0; }
    public Double getFat() { return fat != null ? fat : 0.0; }
    public String getMealTime() { return mealTime; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id != null ? UUID.fromString(id) : null; }
    public void setFoodItem(String foodItem) { this.foodItem = foodItem; }
    public void setServingSize(Double servingSize) { this.servingSize = servingSize; }
    public void setKcal(Double kcal) { this.kcal = kcal; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }
    public void setProtein(Double protein) { this.protein = protein; }
    public void setFat(Double fat) { this.fat = fat; }
    public void setMealTime(String mealTime) { this.mealTime = mealTime; }
    public void setDate(String date) { this.date = date; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
