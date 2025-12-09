package com.lukehemmin.ai_diet_app.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatHistoryResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("userMessage")
    private String userMessage;

    @SerializedName("aiResponse")
    private String aiResponse;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public String getUserMessage() { return userMessage; }
    public String getAiResponse() { return aiResponse; }
    public String getCreatedAt() { return createdAt; }
}
