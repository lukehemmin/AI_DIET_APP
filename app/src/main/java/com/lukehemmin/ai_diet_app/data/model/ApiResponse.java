package com.lukehemmin.ai_diet_app.data.model;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }
}
