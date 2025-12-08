package com.lukehemmin.ai_diet_app.data.model;

import java.util.ArrayList;
import java.util.List;

public class MealAnalysisResponse {
    private List<MealAnalysisResult> analysisResults;
    private String imageUrl;
    private List<String> imageUrls;

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

    public List<String> getImageUrls() {
        if (imageUrls == null || imageUrls.isEmpty()) {
            // Fallback to single imageUrl
            List<String> urls = new ArrayList<>();
            if (imageUrl != null) {
                urls.add(imageUrl);
            }
            return urls;
        }
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
