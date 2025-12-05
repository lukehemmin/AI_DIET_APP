package com.lukehemmin.dodietapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FindIdRequest {
    @NotBlank
    private String name;

    @NotNull
    private LocalDate birthDate;
}