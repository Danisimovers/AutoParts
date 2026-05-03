package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class PasswordResetTokenRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Сохранить токен
    public void save(PasswordResetToken token) {
        String sql = "INSERT INTO password_reset_tokens (email, token, expires_at, used) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                token.getEmail(),
                token.getToken(),
                token.getExpiresAt(),
                token.isUsed()
        );
    }

    // Найти токен по значению
    public PasswordResetToken findByToken(String token) {
        String sql = "SELECT * FROM password_reset_tokens WHERE token = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(PasswordResetToken.class), token);
        } catch (Exception e) {
            return null;
        }
    }

    // Отметить токен как использованный
    public void markAsUsed(Long id) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    // Удалить все токены для email (при повторном запросе)
    public void deleteByEmail(String email) {
        String sql = "DELETE FROM password_reset_tokens WHERE email = ?";
        jdbcTemplate.update(sql, email);
    }
}