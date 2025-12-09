package com.lukehemmin.dodietapi.dto.request;

import lombok.Data;

@Data
public class AddReactionRequest {
    private String reactionType;  // LIKE, HEART, FIRE, CLAP, CELEBRATE
}
