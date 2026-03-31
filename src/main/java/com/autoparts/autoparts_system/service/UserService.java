package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.model.Role;
import com.autoparts.autoparts_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User registerUser(String login, String password, String email, String phone) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин обязателен");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль обязателен");
        }

        User existing = userRepository.findByLogin(login);
        if (existing != null) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.CUSTOMER);
        user.setEmail(email);
        user.setPhone(phone);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }

    public User login(String login, String password) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Login: " + login);
        System.out.println("Password: " + password);

        User user = userRepository.findByLogin(login);
        if (user == null) {
            System.out.println("User NOT found");
            throw new IllegalArgumentException("Пользователь не найден");
        }

        System.out.println("User found: " + user.getLogin());
        System.out.println("Stored password hash: " + user.getPassword());
        System.out.println("Password matches: " + passwordEncoder.matches(password, user.getPassword()));

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
        // Если пароль передан, хэшируем его
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existing.getPassword());
        }
        userRepository.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}