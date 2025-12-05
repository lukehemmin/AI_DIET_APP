package com.lukehemmin.ai_diet_app.data.model;

public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private ProfileRequest profile;

    public SignupRequest(String email, String password, String name, ProfileRequest profile) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profile = profile;
    }

    public static class ProfileRequest {
        private String gender; // MALE, FEMALE
        private String birthDate; // yyyy-MM-dd
        private Double height;
        private Double weight;
        private String activityLevel; // SEDENTARY, LIGHT, MODERATE, ACTIVE, VERY_ACTIVE

        public ProfileRequest(String gender, String birthDate, Double height, Double weight, String activityLevel) {
            this.gender = gender;
            this.birthDate = birthDate;
            this.height = height;
            this.weight = weight;
            this.activityLevel = activityLevel;
        }
    }
}
