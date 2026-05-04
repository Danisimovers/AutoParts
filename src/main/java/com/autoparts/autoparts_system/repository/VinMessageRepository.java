package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.VinMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class VinMessageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(VinMessage message) {
        String sql = "INSERT INTO vin_messages (vin_request_id, sender_id, message, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                message.getVinRequestId(),
                message.getSenderId(),
                message.getMessage(),
                LocalDateTime.now()
        );
    }

    public List<VinMessage> findByVinRequestId(Long vinRequestId) {
        String sql = "SELECT * FROM vin_messages WHERE vin_request_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VinMessage.class), vinRequestId);
    }
}