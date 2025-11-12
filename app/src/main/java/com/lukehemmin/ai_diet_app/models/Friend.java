package com.lukehemmin.ai_diet_app.models;

public class Friend {
    private String name;
    private int avatarResId;

    public Friend(String name, int avatarResId) {
        this.name = name;
        this.avatarResId = avatarResId;
    }

    public String getName() {
        return name;
    }

    public int getAvatarResId() {
        return avatarResId;
    }
}
