package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.response.WaterIntakeResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.WaterIntake;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.repository.WaterIntakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WaterIntakeService {

    private final WaterIntakeRepository waterIntakeRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_GOAL_GLASSES = 8;

    @Transactional(readOnly = true)
    public WaterIntakeResponse getTodayIntake(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        WaterIntake waterIntake = waterIntakeRepository.findByUserAndDate(user, today)
                .orElse(WaterIntake.builder()
                        .user(user)
                        .date(today)
                        .glasses(0)
                        .build());

        return mapToResponse(waterIntake);
    }

    @Transactional
    public WaterIntakeResponse addGlass(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        WaterIntake waterIntake = waterIntakeRepository.findByUserAndDate(user, today)
                .orElse(WaterIntake.builder()
                        .user(user)
                        .date(today)
                        .glasses(0)
                        .build());

        waterIntake.setGlasses(waterIntake.getGlasses() + 1);
        WaterIntake saved = waterIntakeRepository.save(waterIntake);

        return mapToResponse(saved);
    }

    @Transactional
    public WaterIntakeResponse removeGlass(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        WaterIntake waterIntake = waterIntakeRepository.findByUserAndDate(user, today)
                .orElseThrow(() -> new RuntimeException("No water intake record for today"));

        if (waterIntake.getGlasses() > 0) {
            waterIntake.setGlasses(waterIntake.getGlasses() - 1);
            waterIntakeRepository.save(waterIntake);
        }

        return mapToResponse(waterIntake);
    }

    private WaterIntakeResponse mapToResponse(WaterIntake waterIntake) {
        int goal = DEFAULT_GOAL_GLASSES;
        double progress = (double) waterIntake.getGlasses() / goal;
        // Clamp progress to 1.0 if needed, or allow > 1.0
        // progress = Math.min(progress, 1.0); 

        return WaterIntakeResponse.builder()
                .date(waterIntake.getDate())
                .glasses(waterIntake.getGlasses())
                .goalGlasses(goal)
                .progress(progress)
                .build();
    }
}
