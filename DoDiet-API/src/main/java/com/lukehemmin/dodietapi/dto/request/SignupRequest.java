package com.lukehemmin.dodietapi.dto.request;

import com.lukehemmin.dodietapi.entity.ActivityLevel;
import com.lukehemmin.dodietapi.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotNull
    private ProfileRequest profile;

    @Data
    public static class ProfileRequest {
        @NotNull
        private Gender gender;

        @NotNull
        private Integer age;

        @NotNull
        private Double height;

        @NotNull
        private Double weight;

        @NotNull
        private ActivityLevel activityLevel;
    }
}
