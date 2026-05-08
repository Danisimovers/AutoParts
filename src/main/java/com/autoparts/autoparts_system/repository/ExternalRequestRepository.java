package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.ExternalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ExternalRequestRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(ExternalRequest request) {
        String sql = "INSERT INTO external_requests (user_id, product_name, factory_number, producer, supplier_name, price, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                request.getUserId(),
                request.getProductName(),
                request.getFactoryNumber(),
                request.getProducer(),
                request.getSupplierName(),
                request.getPrice(),
                request.getStatus() != null ? request.getStatus() : "PENDING",
                LocalDateTime.now()
        );
    }

    public List<ExternalRequest> findAll() {
        String sql = "SELECT * FROM external_requests ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExternalRequest.class));
    }

    public List<ExternalRequest> findByUserId(Long userId) {
        String sql = "SELECT * FROM external_requests WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExternalRequest.class), userId);
    }

    public ExternalRequest findById(Long id) {
        String sql = "SELECT * FROM external_requests WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(ExternalRequest.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE external_requests SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }
}