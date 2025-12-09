package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.FridgeRecipeHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeRecipeHistoryResponse {
    
    private UUID id;
    private String ingredients;
    private String content;
    private LocalDateTime createdAt;
    private String formattedDate;  // "12월 9일 오후 7:52" 형식
    
    public static FridgeRecipeHistoryResponse from(FridgeRecipeHistory history) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 a h:mm");
        
        return FridgeRecipeHistoryResponse.builder()
                .id(history.getId())
                .ingredients(history.getIngredients())
                .content(history.getContent())
                .createdAt(history.getCreatedAt())
                .formattedDate(history.getCreatedAt().format(formatter))
                .build();
    }
}
