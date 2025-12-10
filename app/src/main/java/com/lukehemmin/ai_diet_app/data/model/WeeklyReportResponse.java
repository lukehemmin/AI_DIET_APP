package com.lukehemmin.ai_diet_app.data.model;

import java.util.List;

public class WeeklyReportResponse {
    private String aiSummary;
    private double averageCalories;
    private double totalCalories;
    private int recordedDays;
    private List<TopFood> topFoods;
    private List<CompletedChallenge> completedChallenges;
    private int completedChallengeCount;

    public String getAiSummary() {
        return aiSummary;
    }

    public double getAverageCalories() {
        return averageCalories;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public int getRecordedDays() {
        return recordedDays;
    }

    public List<TopFood> getTopFoods() {
        return topFoods;
    }

    public List<CompletedChallenge> getCompletedChallenges() {
        return completedChallenges;
    }

    public int getCompletedChallengeCount() {
        return completedChallengeCount;
    }

    public static class TopFood {
        private String foodName;
        private int count;

        public String getFoodName() {
            return foodName;
        }

        public int getCount() {
            return count;
        }
    }

    public static class CompletedChallenge {
        private String id;
        private String name;
        private String completedAt;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCompletedAt() {
            return completedAt;
        }
    }
}
