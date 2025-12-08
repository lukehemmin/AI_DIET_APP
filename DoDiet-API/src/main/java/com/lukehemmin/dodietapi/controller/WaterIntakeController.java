package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.WaterIntakeResponse;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterIntakeController {

    private final WaterIntakeService waterIntakeService;

    @GetMapping
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> getWaterIntake(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WaterIntakeResponse response = waterIntakeService.getIntakeByDate(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> addWaterGlass(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WaterIntakeResponse response = waterIntakeService.addGlass(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> removeWaterGlass(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WaterIntakeResponse response = waterIntakeService.removeGlass(currentUser.getId(), date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
