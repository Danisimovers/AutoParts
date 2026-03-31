package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.LoginRequest;
import com.autoparts.autoparts_system.dto.request.RegisterRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.AuthResponse;
import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.repository.EmailVerificationRepository;
import com.autoparts.autoparts_system.repository.UserRepository;
import com.autoparts.autoparts_system.security.JwtService;
import com.autoparts.autoparts_system.service.EmailService;
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
    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            if ((request.getEmail() == null || request.getEmail().isEmpty()) &&
                    (request.getPhone() == null || request.getPhone().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Укажите email или номер телефона"));
            }

            // Временная регистрация (пользователь НЕ создается)
            userService.registerUserTemp(
                    request.getLogin(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getPhone()
            );

            return ResponseEntity.ok(ApiResponse.success(
                    "На почту " + request.getEmail() + " отправлено письмо с подтверждением. " +
                            "Перейдите по ссылке в письме для завершения регистрации.", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (RuntimeException e) {
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

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        System.out.println("=== VERIFY EMAIL ===");
        System.out.println("Token: " + token);

        try {
            // Создаем пользователя после подтверждения
            User user = userService.confirmAndCreateUser(token);

            String jwtToken = jwtService.generateToken(user.getId(), user.getLogin(), user.getRole().name());

            AuthResponse authResponse = AuthResponse.builder()
                    .token(jwtToken)
                    .userId(user.getId())
                    .login(user.getLogin())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .build();

            System.out.println("User activated: " + user.getLogin());

            return ResponseEntity.ok(ApiResponse.success("Email успешно подтвержден! Теперь вы можете войти.", authResponse));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ТЕСТОВЫЙ ЭНДПОИНТ ДЛЯ ПРОВЕРКИ ОТПРАВКИ ПИСЕМ
    @GetMapping("/test-email")
    public ResponseEntity<ApiResponse> testEmail(@RequestParam String email) {
        try {
            emailService.sendVerificationEmail(email, "test-token-123");
            return ResponseEntity.ok(ApiResponse.success("Письмо отправлено на " + email, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }
}