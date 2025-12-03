package com.lukehemmin.ai_diet_app.data.model;

public class GroupResponse {
    private String id;
    private String name;
    private String description;
    private String challenge;
    private int memberCount;
    private String createdAt;

    public GroupResponse(String id, String name, String description, String challenge, int memberCount, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.challenge = challenge;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getChallenge() {
        return challenge;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
