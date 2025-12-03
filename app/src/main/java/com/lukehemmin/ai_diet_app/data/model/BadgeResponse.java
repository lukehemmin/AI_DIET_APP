package com.lukehemmin.ai_diet_app.data.model;

import com.google.gson.annotations.SerializedName;

public class BadgeResponse {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("icon")
    private String icon;
    
    @SerializedName("unlocked")
    private boolean unlocked;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public boolean isUnlocked() { return unlocked; }
}
