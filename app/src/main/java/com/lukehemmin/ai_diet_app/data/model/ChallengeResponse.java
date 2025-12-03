package com.lukehemmin.ai_diet_app.data.model;

public class ChallengeResponse {
    private String id;
    private String title;
    private String description;
    private String icon;
    private Integer goal;
    private String goalUnit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getGoal() {
        return goal;
    }

    public void setGoal(Integer goal) {
        this.goal = goal;
    }

    public String getGoalUnit() {
        return goalUnit;
    }

    public void setGoalUnit(String goalUnit) {
        this.goalUnit = goalUnit;
    }
}
