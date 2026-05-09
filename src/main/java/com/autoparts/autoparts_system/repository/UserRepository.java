package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public User findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), login);
        } catch (Exception e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), email);
        } catch (Exception e) {
            return null;
        }
    }

    // ========== ДОБАВЛЕН МЕТОД findByPhone ==========
    public User findByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), phone);
        } catch (Exception e) {
            return null;
        }
    }

    public void save(User user) {
        String sql = "INSERT INTO users (login, password, role, email, phone, created_at, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getLogin(),
                user.getPassword(),
                user.getRole().name(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.isEnabled()
        );
    }

    public void update(User user) {
        String sql = "UPDATE users SET login = ?, password = ?, role = ?, email = ?, phone = ?, enabled = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getLogin(),
                user.getPassword(),
                user.getRole().name(),
                user.getEmail(),
                user.getPhone(),
                user.isEnabled(),
                user.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}