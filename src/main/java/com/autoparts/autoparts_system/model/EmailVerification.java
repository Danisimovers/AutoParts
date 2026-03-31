package com.autoparts.autoparts_system.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerification {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean verified;
    private String login;
    private String passwordHash;
    private String email;
    private String phone;
}