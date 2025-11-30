package com.lukehemmin.dodietapi.service;

import com.lukehemmin.dodietapi.dto.request.LoginRequest;
import com.lukehemmin.dodietapi.dto.request.SignupRequest;
import com.lukehemmin.dodietapi.dto.response.AuthResponse;
import com.lukehemmin.dodietapi.entity.User;
import com.lukehemmin.dodietapi.repository.UserRepository;
import com.lukehemmin.dodietapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setGender(request.getProfile().getGender());
        user.setAge(request.getProfile().getAge());
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
}
