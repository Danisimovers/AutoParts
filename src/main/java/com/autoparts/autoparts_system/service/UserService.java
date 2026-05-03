package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.EmailVerification;
import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.model.Role;
import com.autoparts.autoparts_system.repository.EmailVerificationRepository;
import com.autoparts.autoparts_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    // Регулярное выражение для проверки email
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Временная регистрация - сохраняем данные в таблицу verification, пользователь НЕ создается
     */
    public void registerUserTemp(String login, String password, String email, String phone) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин обязателен");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        // Проверка корректности email
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Введите корректный email (пример: user@mail.ru)");
        }

        // Проверяем, не занят ли логин
        User existing = userRepository.findByLogin(login);
        if (existing != null) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        // Проверяем, не занят ли email
        User existingEmail = userRepository.findByEmail(email);
        if (existingEmail != null) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        // Удаляем старую неподтвержденную регистрацию, если есть
        emailVerificationRepository.deleteByEmail(email);

        // Создаем запись верификации (без создания пользователя!)
        String token = UUID.randomUUID().toString();
        EmailVerification verification = new EmailVerification();
        verification.setUserId(null); // Пользователь еще не создан
        verification.setToken(token);
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setVerified(false);
        verification.setLogin(login);
        verification.setPasswordHash(passwordEncoder.encode(password));
        verification.setEmail(email);
        verification.setPhone(phone);

        emailVerificationRepository.save(verification);

        // Отправляем email
        try {
            emailService.sendVerificationEmail(email, token);
            System.out.println("Verification email sent to: " + email);
        } catch (Exception e) {
            System.err.println("Ошибка отправки email: " + e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо подтверждения. Попробуйте позже.");
        }
    }

    /**
     * Подтверждение email и создание пользователя
     */
    @Transactional
    public User confirmAndCreateUser(String token) {
        EmailVerification verification = emailVerificationRepository.findByToken(token);

        if (verification == null) {
            throw new IllegalArgumentException("Неверный токен подтверждения");
        }

        if (verification.isVerified()) {
            throw new IllegalArgumentException("Email уже подтвержден");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Срок действия ссылки истек. Запросите новое письмо.");
        }

        // Создаем пользователя
        User user = new User();
        user.setLogin(verification.getLogin());
        user.setPassword(verification.getPasswordHash());
        user.setRole(Role.CUSTOMER);
        user.setEmail(verification.getEmail());
        user.setPhone(verification.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true); // Сразу активен после подтверждения

        userRepository.save(user);

        // Обновляем запись верификации
        verification.setVerified(true);
        verification.setUserId(user.getId());
        emailVerificationRepository.update(verification);

        System.out.println("User created and activated: " + user.getLogin());

        return user;
    }

    public User login(String login, String password) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Login: " + login);

        User user = userRepository.findByLogin(login);
        if (user == null) {
            System.out.println("User NOT found");
            throw new IllegalArgumentException("Пользователь не найден");
        }

        System.out.println("User found: " + user.getLogin());
        System.out.println("User enabled: " + user.isEnabled());

        if (!user.isEnabled()) {
            System.out.println("Account not verified!");
            throw new IllegalArgumentException("Аккаунт не активирован. Проверьте почту и перейдите по ссылке подтверждения.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("Password mismatch!");
            throw new IllegalArgumentException("Неверный пароль");
        }

        System.out.println("Login successful!");
        return user;
    }

    public User updateUser(Long id, User user) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        user.setId(id);
        user.setEnabled(existing.isEnabled());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existing.getPassword());
        }
        userRepository.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        emailVerificationRepository.deleteByUserId(id);
        userRepository.deleteById(id);
    }
}