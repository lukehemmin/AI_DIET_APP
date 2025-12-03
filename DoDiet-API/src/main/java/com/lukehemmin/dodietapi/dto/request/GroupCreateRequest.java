package com.lukehemmin.dodietapi.dto.request;

import lombok.Data;

@Data
public class GroupCreateRequest {
    private String name;
    private String description;
    private String challenge;
}
