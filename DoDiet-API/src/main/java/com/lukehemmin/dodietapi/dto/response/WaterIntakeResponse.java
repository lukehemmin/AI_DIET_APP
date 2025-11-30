package com.lukehemmin.dodietapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WaterIntakeResponse {
    private LocalDate date;
    private Integer glasses;
    private Integer goalGlasses;
    private Double progress;
}
