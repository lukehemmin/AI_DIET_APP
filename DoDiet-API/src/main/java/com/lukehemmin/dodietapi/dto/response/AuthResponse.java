package com.lukehemmin.dodietapi.dto.response;

import com.lukehemmin.dodietapi.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private UserDto user;
    private String accessToken;
    private String refreshToken;

    @Data
    @Builder
    public static class UserDto {
        private java.util.UUID id;
        private String email;
        private String name;
        
        public static UserDto from(User user) {
            return UserDto.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();
        }
    }
}
