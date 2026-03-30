package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.model.Role;
import com.autoparts.autoparts_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id);
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
        user.setPassword(password); // В реальном проекте нужно хэшировать!
        user.setRole(Role.CUSTOMER);
        user.setEmail(email);
        user.setPhone(phone);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        return user;
    }

    public User login(String login, String password) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Неверный пароль");
        }
        return user;
    }

    public User updateUser(Long id, User user) {
        User existing = userRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        user.setId(id);
        userRepository.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}