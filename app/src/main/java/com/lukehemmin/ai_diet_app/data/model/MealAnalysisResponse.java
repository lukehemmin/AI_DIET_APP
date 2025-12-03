package com.lukehemmin.ai_diet_app.data.model;

import java.util.List;

public class MealAnalysisResponse {
    private List<MealAnalysisResult> analysisResults;
    private String imageUrl;

    public List<MealAnalysisResult> getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(List<MealAnalysisResult> analysisResults) {
        this.analysisResults = analysisResults;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
