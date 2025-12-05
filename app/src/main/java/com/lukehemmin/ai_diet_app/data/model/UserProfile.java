package com.lukehemmin.ai_diet_app.data.model;

public class UserProfile {
    private String name;
    private String email;
    private String gender; // MALE, FEMALE
    private Integer age;
    private Double height;
    private Double weight;
    private String activityLevel; // SEDENTARY, LIGHT, ...
    private Double bmr;
    private Double goalIntake;

    public UserProfile() {
    }

    public UserProfile(String name, String email, String gender, Integer age, Double height, Double weight, String activityLevel, Double bmr, Double goalIntake) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.activityLevel = activityLevel;
        this.bmr = bmr;
        this.goalIntake = goalIntake;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWeight() {
        return weight;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public Double getBmr() {
        return bmr;
    }

    public Double getGoalIntake() {
        return goalIntake;
    }
}
