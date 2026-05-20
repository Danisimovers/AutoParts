package com.autoparts.autoparts_system;

import com.autoparts.autoparts_system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Устанавливаем значения через рефлексию (вместо @Value)
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", "testSecretKeyForJWTTokenGenerationVeryLongKey123456");
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", 86400000L); // 24 часа
    }

    @Test
    void testGenerateTokenAndValidate() {
        Long userId = 1L;
        String login = "testuser";
        String role = "CUSTOMER";

        // Генерация токена
        String token = jwtService.generateToken(userId, login, role);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Валидация токена
        assertTrue(jwtService.validateToken(token));

        // Извлечение данных из токена
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals(login, jwtService.extractLogin(token));
        assertEquals(role, jwtService.extractRole(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.string";
        assertFalse(jwtService.validateToken(invalidToken));

        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxMjM0NTY3ODkwfQ.signature";
        assertFalse(jwtService.validateToken(expiredToken));
    }
}