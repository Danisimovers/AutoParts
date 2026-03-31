package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.LoginRequest;
import com.autoparts.autoparts_system.dto.request.RegisterRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.AuthResponse;
import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.security.JwtService;
import com.autoparts.autoparts_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            if ((request.getEmail() == null || request.getEmail().isEmpty()) &&
                    (request.getPhone() == null || request.getPhone().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Укажите email или номер телефона"));
            }

            User user = userService.registerUser(
                    request.getLogin(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getPhone()
            );

            String token = jwtService.generateToken(user.getId(), user.getLogin(), user.getRole().name());

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .login(user.getLogin())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Регистрация прошла успешно", authResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getLogin(), request.getPassword());

            String token = jwtService.generateToken(user.getId(), user.getLogin(), user.getRole().name());

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .login(user.getLogin())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Вход выполнен успешно", authResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        }
    }
}