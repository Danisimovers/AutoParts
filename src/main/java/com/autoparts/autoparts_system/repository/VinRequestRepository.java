package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.VinRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class VinRequestRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(VinRequest request) {
        String sql = "INSERT INTO vin_requests (user_id, vin, description, status) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                request.getUserId(),
                request.getVin(),
                request.getDescription(),
                request.getStatus() != null ? request.getStatus() : "PENDING"
        );
    }

    public List<VinRequest> findAll() {
        String sql = "SELECT * FROM vin_requests ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VinRequest.class));
    }

    public VinRequest findById(Long id) {
        String sql = "SELECT * FROM vin_requests WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(VinRequest.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<VinRequest> findByUserId(Long userId) {
        String sql = "SELECT * FROM vin_requests WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VinRequest.class), userId);
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE vin_requests SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM vin_requests WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}