package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.ChallengeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserChallengeResponse {
    private ChallengeResponse challenge;
    private Integer current;
    private Double progress;
    private ChallengeStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
