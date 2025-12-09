package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.request.LoginRequest;
import com.lukehemmin.dodietapi.dto.request.SignupRequest;
import com.lukehemmin.dodietapi.dto.response.AuthResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.entity.VerificationCode;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.repository.VerificationCodeRepository;
import com.lukehemmin.dodietapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    // 앱의 strings.xml에 입력한 값과 동일해야 합니다.
    private static final String GOOGLE_CLIENT_ID = "1021694961860-cn6008vavbgjn82prj1gfdoakcm12qd6.apps.googleusercontent.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;

    @Transactional
    public void sendVerificationCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
        
        verificationCodeRepository.save(verificationCode);
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new RuntimeException("인증 코드를 찾을 수 없습니다."));

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("인증 코드가 만료되었습니다.");
        }

        if (verificationCode.getVerified()) {
            return true; // Already verified
        }

        if (!verificationCode.getCode().equals(code)) {
            throw new RuntimeException("인증 코드가 일치하지 않습니다.");
        }

        verificationCode.setVerified(true);
        verificationCodeRepository.save(verificationCode);
        return true;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Check if email was verified
        VerificationCode verificationCode = verificationCodeRepository.findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 인증이 필요합니다."));
        
        if (!verificationCode.getVerified()) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setGender(request.getProfile().getGender());
        user.setBirthDate(request.getProfile().getBirthDate());
        // Calculate age from birthDate
        user.setAge(java.time.Period.between(request.getProfile().getBirthDate(), java.time.LocalDate.now()).getYears());
        user.setHeight(request.getProfile().getHeight());
        user.setWeight(request.getProfile().getWeight());
        user.setActivityLevel(request.getProfile().getActivityLevel());

        User savedUser = userRepository.save(user);

        // Auto login after signup
        String accessToken = tokenProvider.generateToken(savedUser.getEmail(), 86400000); // 1 day
        String refreshToken = tokenProvider.generateToken(savedUser.getEmail(), 2592000000L); // 30 days

        return AuthResponse.builder()
                .user(AuthResponse.UserDto.from(savedUser))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return AuthResponse.builder()
                .user(AuthResponse.UserDto.from(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public java.util.List<String> findId(String name, java.time.LocalDate birthDate) {
        return userRepository.findByNameAndBirthDate(name, birthDate).stream()
                .map(User::getEmail)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void sendPasswordResetCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("가입되지 않은 이메일입니다.");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
        
        verificationCodeRepository.save(verificationCode);
        emailService.sendVerificationCode(email, code);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyCode(email, code); // This verifies and marks as verified

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse googleLogin(com.lukehemmin.dodietapi.dto.GoogleLoginRequest request) {
        // 1. Verify Google Token
        java.util.Map<String, Object> googleInfo = verifyGoogleToken(request.getIdToken());
        String email = (String) googleInfo.get("email");
        String name = (String) googleInfo.get("name");
        String providerId = (String) googleInfo.get("sub");
        String googleProfileImage = (String) googleInfo.get("picture");
        if (name == null) name = "Google User";

        // 2. Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // 3. Auto Signup if not exists
            user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString())); // Random password
            user.setName(name);
            user.setAuthProvider(com.lukehemmin.dodietapi.entity.AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            
            // Google 프로필 사진 설정
            if (googleProfileImage != null && !googleProfileImage.isEmpty()) {
                user.setProfileImageUrl(googleProfileImage);
            }
            
            // Set Default Values for required fields
            user.setGender(com.lukehemmin.dodietapi.entity.Gender.MALE);
            user.setBirthDate(java.time.LocalDate.now().minusYears(25));
            user.setAge(25);
            user.setHeight(170.0);
            user.setWeight(65.0);
            user.setActivityLevel(com.lukehemmin.dodietapi.entity.ActivityLevel.SEDENTARY);
            
            user = userRepository.save(user);
        } else {
             if (user.getAuthProvider() == null) {
                 user.setAuthProvider(com.lukehemmin.dodietapi.entity.AuthProvider.LOCAL);
             }
             
             // Link Google Account if not linked yet
             if (user.getProviderId() == null) {
                 user.setProviderId(providerId);
             }
             
             // 프로필 사진이 없으면 Google 프로필 사진으로 설정
             if (user.getProfileImageUrl() == null && googleProfileImage != null && !googleProfileImage.isEmpty()) {
                 user.setProfileImageUrl(googleProfileImage);
             }
             
             userRepository.save(user);
        }

        // 4. Generate Tokens manually since we don't have password for authenticationManager
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password("")
                .authorities(java.util.Collections.emptyList())
                .build();
                
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return AuthResponse.builder()
                .user(AuthResponse.UserDto.from(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private java.util.Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> response = restTemplate.getForObject(url, java.util.Map.class);
            
            if (response == null || response.get("email") == null) {
                throw new RuntimeException("Invalid Google Token");
            }
            
            String aud = (String) response.get("aud");
            if (aud == null || !aud.equals(GOOGLE_CLIENT_ID)) {
                throw new RuntimeException("Invalid Google Token Audience");
            }
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Google Token Verification Failed: " + e.getMessage());
        }
    }
}