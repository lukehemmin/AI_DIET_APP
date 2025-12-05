package com.lukehemmin.ai_diet_app.data.model;

public class FindIdRequest {
    private String name;
    private String birthDate; // yyyy-MM-dd

    public FindIdRequest(String name, String birthDate) {
        this.name = name;
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
}
