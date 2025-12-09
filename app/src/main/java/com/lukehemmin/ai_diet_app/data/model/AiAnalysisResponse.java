package com.lukehemmin.ai_diet_app.data.model;

import com.google.gson.annotations.SerializedName;

public class AiAnalysisResponse {
    
    @SerializedName("analysisType")
    private String analysisType;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("generatedAt")
    private String generatedAt;
    
    @SerializedName("canRefresh")
    private boolean canRefresh;
    
    @SerializedName("remainingCooldownSeconds")
    private long remainingCooldownSeconds;
    
    @SerializedName("needsUpdate")
    private boolean needsUpdate;

    public String getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(String analysisType) {
        this.analysisType = analysisType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isCanRefresh() {
        return canRefresh;
    }

    public void setCanRefresh(boolean canRefresh) {
        this.canRefresh = canRefresh;
    }

    public long getRemainingCooldownSeconds() {
        return remainingCooldownSeconds;
    }

    public void setRemainingCooldownSeconds(long remainingCooldownSeconds) {
        this.remainingCooldownSeconds = remainingCooldownSeconds;
    }

    public boolean isNeedsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    // 쿨다운 시간을 포맷팅 (예: "25분 30초")
    public String getFormattedCooldown() {
        if (remainingCooldownSeconds <= 0) return null;
        
        long minutes = remainingCooldownSeconds / 60;
        long seconds = remainingCooldownSeconds % 60;
        
        if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }
}
