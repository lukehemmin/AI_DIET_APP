package com.lukehemmin.dodietapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeResponse {
    private String id;
    private String title;
    private String description;
    private String icon;
    private Integer goal;
    private String goalUnit;
}
