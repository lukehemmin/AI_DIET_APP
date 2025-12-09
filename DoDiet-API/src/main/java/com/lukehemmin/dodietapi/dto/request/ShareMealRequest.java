package com.lukehemmin.dodietapi.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ShareMealRequest {
    private UUID mealId;
    private List<UUID> groupIds;  // 공유할 그룹 ID 목록
    private String message;       // 선택적 메시지
}
