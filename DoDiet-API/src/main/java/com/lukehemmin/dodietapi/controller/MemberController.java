package com.lukehemmin.dodietapi.controller;

import com.lukehemmin.dodietapi.dto.response.ApiResponse;
import com.lukehemmin.dodietapi.dto.response.UserProfileResponse;
import com.lukehemmin.dodietapi.entity.ActivityLevel;
import com.lukehemmin.dodietapi.entity.Gender;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final UserRepository userRepository;
    private final StorageService storageService;

    /**
     * 현재 로그인한 사용자의 프로필 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileResponse response = UserProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .activityLevel(user.getActivityLevel() != null ? user.getActivityLevel().name() : null)
                .bmr(calculateBmr(user))
                .goalIntake(calculateGoalIntake(user))
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 현재 로그인한 사용자의 프로필 수정
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserProfileResponse request) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 업데이트 가능한 필드만 업데이트
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(ActivityLevel.valueOf(request.getActivityLevel()));
        }
        if (request.getGender() != null) {
            user.setGender(Gender.valueOf(request.getGender()));
        }
        
        userRepository.save(user);
        
        // 업데이트된 프로필 반환
        UserProfileResponse response = UserProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .age(user.getAge())
                .height(user.getHeight())
                .weight(user.getWeight())
                .activityLevel(user.getActivityLevel() != null ? user.getActivityLevel().name() : null)
                .bmr(calculateBmr(user))
                .goalIntake(request.getGoalIntake() != null ? request.getGoalIntake() : calculateGoalIntake(user))
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로필 이미지 업로드
     */
    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("image") MultipartFile image) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            String imageUrl = storageService.storeProfileImage(image, user.getId().toString());
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);
            
            return ResponseEntity.ok(ApiResponse.success(Map.of("profileImageUrl", imageUrl)));
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("이미지 업로드에 실패했습니다."));
        }
    }

    /**
     * 프로필 이미지 삭제
     */
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteProfileImage(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setProfileImageUrl(null);
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "프로필 이미지가 삭제되었습니다.")));
    }

    /**
     * BMR(기초대사량) 계산 - Mifflin-St Jeor 공식
     */
    private Double calculateBmr(User user) {
        if (user.getWeight() == null || user.getHeight() == null || user.getAge() == null) {
            return null;
        }
        
        double bmr;
        if (user.getGender() == Gender.MALE) {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() + 5;
        } else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge() - 161;
        }
        return bmr;
    }

    /**
     * 목표 섭취량 계산 (BMR * 활동계수)
     */
    private Double calculateGoalIntake(User user) {
        Double bmr = calculateBmr(user);
        if (bmr == null) {
            return null;
        }
        
        double activityMultiplier = 1.2; // 기본: 좌식 생활
        ActivityLevel activityLevel = user.getActivityLevel();
        
        if (activityLevel != null) {
            switch (activityLevel) {
                case LIGHT:
                    activityMultiplier = 1.375;
                    break;
                case MODERATE:
                    activityMultiplier = 1.55;
                    break;
                case ACTIVE:
                    activityMultiplier = 1.725;
                    break;
                case VERY_ACTIVE:
                    activityMultiplier = 1.9;
                    break;
                // SEDENTARY는 기본값 1.2 사용
            }
        }
        
        return bmr * activityMultiplier;
    }
}
