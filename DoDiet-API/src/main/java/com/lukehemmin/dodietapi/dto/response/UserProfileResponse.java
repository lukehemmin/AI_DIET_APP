package com.lukehemmin.dodietapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String name;
    private String email;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;
    private String activityLevel;
    private Double bmr;
    private Double goalIntake;
    private String profileImageUrl;
    private String createdAt;
}
