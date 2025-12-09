package com.lukehemmin.ai_diet_app.data.model;

import com.google.gson.annotations.SerializedName;

public class FridgeRecipeHistoryResponse {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("ingredients")
    private String ingredients;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("formattedDate")
    private String formattedDate;

    public String getId() {
        return id;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getFormattedDate() {
        return formattedDate;
    }
}
