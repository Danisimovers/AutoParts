package com.autoparts.autoparts_system.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String login;
    private String password;
    private String email;
    private String phone;
}