package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.EmailVerification;
import com.autoparts.autoparts_system.model.PasswordResetToken;
import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.model.Role;
import com.autoparts.autoparts_system.repository.EmailVerificationRepository;
import com.autoparts.autoparts_system.repository.PasswordResetTokenRepository;
import com.autoparts.autoparts_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.replaceAll("[^\\d+]", "");
        return cleaned.matches("^(\\+7|8)?9\\d{9}$");
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

    public void registerUserTemp(String login, String password, String email, String phone) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин обязателен");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }
        if (password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не менее 4 символов");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Введите корректный email (пример: user@mail.ru)");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Телефон обязателен для регистрации");
        }

        if (!isValidPhone(phone)) {
            throw new IllegalArgumentException("Введите корректный номер телефона (например: +79161234567 или 89161234567)");
        }

        User existing = userRepository.findByLogin(login);
        if (existing != null) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        User existingEmail = userRepository.findByEmail(email);
        if (existingEmail != null) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User existingPhone = userRepository.findByPhone(phone);
        if (existingPhone != null) {
            throw new IllegalArgumentException("Пользователь с таким номером телефона уже зарегистрирован");
        }

        emailVerificationRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        EmailVerification verification = new EmailVerification();
        verification.setUserId(null);
        verification.setToken(token);
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setVerified(false);
        verification.setLogin(login);
        verification.setPasswordHash(passwordEncoder.encode(password));
        verification.setEmail(email);
        verification.setPhone(phone);

        emailVerificationRepository.save(verification);

        try {
            emailService.sendVerificationEmail(email, token);
            log.debug("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо подтверждения. Попробуйте позже.");
        }
    }

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

        User user = new User();
        user.setLogin(verification.getLogin());
        user.setPassword(verification.getPasswordHash());
        user.setRole(Role.CUSTOMER);
        user.setEmail(verification.getEmail());
        user.setPhone(verification.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);

        userRepository.save(user);

        verification.setVerified(true);
        verification.setUserId(user.getId());
        emailVerificationRepository.update(verification);

        log.debug("User created and activated: {}", user.getLogin());

        return user;
    }

    public User login(String login, String password) {
        log.debug("Login attempt for user: {}", login);

        User user = userRepository.findByLogin(login);
        if (user == null) {
            log.warn("User not found: {}", login);
            throw new IllegalArgumentException("Пользователь не найден");
        }

        log.debug("User found: {}, enabled: {}", user.getLogin(), user.isEnabled());

        if (!user.isEnabled()) {
            log.warn("Account not verified: {}", login);
            throw new IllegalArgumentException("Аккаунт не активирован. Проверьте почту и перейдите по ссылке подтверждения.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Password mismatch for user: {}", login);
            throw new IllegalArgumentException("Неверный пароль");
        }

        log.debug("Login successful for user: {}", login);
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

    public void sendPasswordResetLink(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email обязателен");
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(email, token);
            log.debug("Password reset link sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо для восстановления пароля. Попробуйте позже.");
        }
    }

    public void changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        user.setRole(Role.valueOf(roleName));
        userRepository.update(user);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("Пароль должен быть не менее 4 символов");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);

        if (resetToken == null) {
            throw new IllegalArgumentException("Неверный токен восстановления пароля");
        }

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Ссылка для восстановления пароля уже использована");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Срок действия ссылки истек. Запросите новое письмо");
        }

        User user = userRepository.findByEmail(resetToken.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        passwordResetTokenRepository.markAsUsed(resetToken.getId());

        log.debug("Password reset for user: {}", user.getLogin());
    }
}