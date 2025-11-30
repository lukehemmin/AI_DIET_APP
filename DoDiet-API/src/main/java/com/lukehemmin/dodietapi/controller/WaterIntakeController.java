package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.WaterIntakeResponse;
import com.lukehemmin.dodietapi.security.UserPrincipal;
import com.lukehemmin.dodietapi.service.WaterIntakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterIntakeController {

    private final WaterIntakeService waterIntakeService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> getTodayWaterIntake(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        WaterIntakeResponse response = waterIntakeService.getTodayIntake(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> addWaterGlass(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        WaterIntakeResponse response = waterIntakeService.addGlass(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<WaterIntakeResponse>> removeWaterGlass(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        WaterIntakeResponse response = waterIntakeService.removeGlass(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
