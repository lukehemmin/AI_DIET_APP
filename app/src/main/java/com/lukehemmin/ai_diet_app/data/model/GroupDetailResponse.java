package com.lukehemmin.ai_diet_app.data.model;

import java.util.List;

public class GroupDetailResponse {
    private String id;
    private String name;
    private String description;
    private String challenge;
    private int challengeDays;
    private int progress;
    private int targetValue;
    private int currentValue;
    private String userRole;  // LEADER, MEMBER, NONE
    private List<GroupMemberResponse> members;
    private String challengeStartDate;
    private String createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public List<GroupMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberResponse> members) {
        this.members = members;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getChallengeDays() {
        return challengeDays;
    }

    public void setChallengeDays(int challengeDays) {
        this.challengeDays = challengeDays;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getChallengeStartDate() {
        return challengeStartDate;
    }

    public void setChallengeStartDate(String challengeStartDate) {
        this.challengeStartDate = challengeStartDate;
    }
}
