package com.lukehemmin.ai_diet_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DietAnalyticsResponse {
    
    @SerializedName("calorieTrend")
    private List<CalorieTrendItem> calorieTrend;
    
    @SerializedName("nutritionBalance")
    private NutritionBalance nutritionBalance;
    
    @SerializedName("eatingHabitsAnalysis")
    private String eatingHabitsAnalysis;
    
    @SerializedName("patternAnalysis")
    private PatternAnalysis patternAnalysis;
    
    @SerializedName("mealTimePattern")
    private List<MealTimePattern> mealTimePattern;

    // Getters
    public List<CalorieTrendItem> getCalorieTrend() { return calorieTrend; }
    public NutritionBalance getNutritionBalance() { return nutritionBalance; }
    public String getEatingHabitsAnalysis() { return eatingHabitsAnalysis; }
    public PatternAnalysis getPatternAnalysis() { return patternAnalysis; }
    public List<MealTimePattern> getMealTimePattern() { return mealTimePattern; }

    public static class CalorieTrendItem {
        @SerializedName("date")
        private String date;
        
        @SerializedName("dayOfWeek")
        private String dayOfWeek;
        
        @SerializedName("calories")
        private double calories;
        
        @SerializedName("targetCalories")
        private double targetCalories;

        public String getDate() { return date; }
        public String getDayOfWeek() { return dayOfWeek; }
        public double getCalories() { return calories; }
        public double getTargetCalories() { return targetCalories; }
    }

    public static class NutritionBalance {
        @SerializedName("myProtein")
        private double myProtein;
        
        @SerializedName("myCarbs")
        private double myCarbs;
        
        @SerializedName("myFat")
        private double myFat;
        
        @SerializedName("myFiber")
        private double myFiber;
        
        @SerializedName("myWater")
        private double myWater;
        
        @SerializedName("recommendedProtein")
        private double recommendedProtein;
        
        @SerializedName("recommendedCarbs")
        private double recommendedCarbs;
        
        @SerializedName("recommendedFat")
        private double recommendedFat;
        
        @SerializedName("recommendedFiber")
        private double recommendedFiber;
        
        @SerializedName("recommendedWater")
        private double recommendedWater;

        public double getMyProtein() { return myProtein; }
        public double getMyCarbs() { return myCarbs; }
        public double getMyFat() { return myFat; }
        public double getMyFiber() { return myFiber; }
        public double getMyWater() { return myWater; }
        public double getRecommendedProtein() { return recommendedProtein; }
        public double getRecommendedCarbs() { return recommendedCarbs; }
        public double getRecommendedFat() { return recommendedFat; }
        public double getRecommendedFiber() { return recommendedFiber; }
        public double getRecommendedWater() { return recommendedWater; }
    }

    public static class PatternAnalysis {
        @SerializedName("goodHabitsPercent")
        private int goodHabitsPercent;
        
        @SerializedName("badHabitsPercent")
        private int badHabitsPercent;
        
        @SerializedName("improvePercent")
        private int improvePercent;
        
        @SerializedName("analysisText")
        private String analysisText;

        public int getGoodHabitsPercent() { return goodHabitsPercent; }
        public int getBadHabitsPercent() { return badHabitsPercent; }
        public int getImprovePercent() { return improvePercent; }
        public String getAnalysisText() { return analysisText; }
    }

    public static class MealTimePattern {
        @SerializedName("dayOfWeek")
        private String dayOfWeek;
        
        @SerializedName("breakfastCount")
        private int breakfastCount;
        
        @SerializedName("lunchCount")
        private int lunchCount;
        
        @SerializedName("dinnerCount")
        private int dinnerCount;

        public String getDayOfWeek() { return dayOfWeek; }
        public int getBreakfastCount() { return breakfastCount; }
        public int getLunchCount() { return lunchCount; }
        public int getDinnerCount() { return dinnerCount; }
    }
}
