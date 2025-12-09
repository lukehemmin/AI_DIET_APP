package com.lukehemmin.dodietapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lukehemmin.dodietapi.dto.request.SignupRequest;
import com.lukehemmin.dodietapi.entity.ActivityLevel;
import com.lukehemmin.dodietapi.entity.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSignup() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        
        SignupRequest.ProfileRequest profile = new SignupRequest.ProfileRequest();
        profile.setGender(Gender.MALE);
        profile.setBirthDate(LocalDate.of(1994, 1, 1));  // 30세에 해당하는 생년월일
        profile.setHeight(180.0);
        profile.setWeight(75.0);
        profile.setActivityLevel(ActivityLevel.MODERATE);
        
        request.setProfile(profile);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }
}
