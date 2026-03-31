package com.autoparts.autoparts_system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private Long userId;
    private String login;
    private String email;
    private String phone;
    private String role;
}