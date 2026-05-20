package com.autoparts.autoparts_system;

import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.repository.UserRepository;
import com.autoparts.autoparts_system.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        // Внедряем passwordEncoder в userService через рефлексию
        try {
            var field = UserService.class.getDeclaredField("passwordEncoder");
            field.setAccessible(true);
            field.set(userService, passwordEncoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testLogin_Success() {
        // Подготовка тестовых данных
        String login = "testuser";
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User mockUser = new User();
        mockUser.setLogin(login);
        mockUser.setPassword(encodedPassword);
        mockUser.setEnabled(true);
        mockUser.setRole(com.autoparts.autoparts_system.model.Role.CUSTOMER);

        when(userRepository.findByLogin(login)).thenReturn(mockUser);

        // Выполнение теста
        User result = userService.login(login, rawPassword);

        // Проверка
        assertNotNull(result);
        assertEquals(login, result.getLogin());
        verify(userRepository, times(1)).findByLogin(login);
    }

    @Test
    void testLogin_UserNotFound() {
        String login = "nonexistent";
        when(userRepository.findByLogin(login)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(login, "anypass");
        });
        verify(userRepository, times(1)).findByLogin(login);
    }

    @Test
    void testLogin_UserNotEnabled() {
        String login = "testuser";
        User mockUser = new User();
        mockUser.setLogin(login);
        mockUser.setEnabled(false);

        when(userRepository.findByLogin(login)).thenReturn(mockUser);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(login, "anypass");
        });
        verify(userRepository, times(1)).findByLogin(login);
    }

    @Test
    void testLogin_WrongPassword() {
        String login = "testuser";
        String correctPassword = "correct123";
        String wrongPassword = "wrong123";
        String encodedPassword = passwordEncoder.encode(correctPassword);

        User mockUser = new User();
        mockUser.setLogin(login);
        mockUser.setPassword(encodedPassword);
        mockUser.setEnabled(true);

        when(userRepository.findByLogin(login)).thenReturn(mockUser);

        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(login, wrongPassword);
        });
        verify(userRepository, times(1)).findByLogin(login);
    }
}