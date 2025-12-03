package com.lukehemmin.dodietapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponse {
    private String id;
    private String name;
    private String description;
    private String icon;
    private boolean isUnlocked;
}
