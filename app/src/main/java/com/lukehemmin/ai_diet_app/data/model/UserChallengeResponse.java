package com.lukehemmin.ai_diet_app.data.model;

public class UserChallengeResponse {
    private ChallengeResponse challenge;
    private Integer current;
    private Double progress;
    private String status;
    private String startedAt;
    private String completedAt;

    public ChallengeResponse getChallenge() {
        return challenge;
    }

    public void setChallenge(ChallengeResponse challenge) {
        this.challenge = challenge;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
