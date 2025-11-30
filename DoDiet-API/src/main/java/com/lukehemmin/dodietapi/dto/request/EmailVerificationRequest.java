package com.lukehemmin.dodietapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailVerificationRequest {
    @NotBlank
    @Email
    private String email;
    
    private String code; // Optional for sending, required for verifying
}
