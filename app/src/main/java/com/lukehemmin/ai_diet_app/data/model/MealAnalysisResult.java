package com.lukehemmin.ai_diet_app.data.model;

public class MealAnalysisResult {
    private String foodItem;
    private Double kcal;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Double servingSize;
    private Boolean isSnack;
    private String imageUrl;  // 이 음식이 분석된 이미지 URL

    public String getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(String foodItem) {
        this.foodItem = foodItem;
    }

    public Double getKcal() {
        return kcal;
    }

    public void setKcal(Double kcal) {
        this.kcal = kcal;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public Double getServingSize() {
        return servingSize;
    }

    public void setServingSize(Double servingSize) {
        this.servingSize = servingSize;
    }

    public Boolean getIsSnack() {
        return isSnack != null ? isSnack : false;
    }

    public void setIsSnack(Boolean isSnack) {
        this.isSnack = isSnack;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
