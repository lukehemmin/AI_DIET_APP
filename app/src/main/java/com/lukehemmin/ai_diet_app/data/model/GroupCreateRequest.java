package com.lukehemmin.ai_diet_app.data.model;

import java.util.List;

public class GroupCreateRequest {
    private String name;
    private String description;
    private String challenge;
    private List<String> invitedFriendIds;

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

    public List<String> getInvitedFriendIds() {
        return invitedFriendIds;
    }

    public void setInvitedFriendIds(List<String> invitedFriendIds) {
        this.invitedFriendIds = invitedFriendIds;
    }
}
