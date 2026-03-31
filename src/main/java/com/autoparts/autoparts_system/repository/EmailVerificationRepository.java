package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.EmailVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class EmailVerificationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(EmailVerification verification) {
        String sql = "INSERT INTO email_verification (user_id, token, expires_at, verified, created_at, login, password_hash, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                verification.getUserId(),
                verification.getToken(),
                verification.getExpiresAt(),
                verification.isVerified(),
                LocalDateTime.now(),
                verification.getLogin(),
                verification.getPasswordHash(),
                verification.getEmail(),
                verification.getPhone()
        );
    }

    public void update(EmailVerification verification) {
        String sql = "UPDATE email_verification SET user_id = ?, verified = ? WHERE id = ?";
        jdbcTemplate.update(sql, verification.getUserId(), verification.isVerified(), verification.getId());
    }

    public EmailVerification findByToken(String token) {
        String sql = "SELECT * FROM email_verification WHERE token = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(EmailVerification.class), token);
        } catch (Exception e) {
            return null;
        }
    }

    public void markAsVerified(Long id) {
        String sql = "UPDATE email_verification SET verified = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM email_verification WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteByEmail(String email) {
        String sql = "DELETE FROM email_verification WHERE email = ? AND verified = FALSE";
        jdbcTemplate.update(sql, email);
    }
}