package com.lukehemmin.ai_diet_app.data.model;

public class GroupCreateRequest {
    private String name;
    private String description;
    private String challenge;

    public GroupCreateRequest(String name, String description, String challenge) {
        this.name = name;
        this.description = description;
        this.challenge = challenge;
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
}
