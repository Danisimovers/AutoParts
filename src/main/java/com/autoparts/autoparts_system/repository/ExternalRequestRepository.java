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
                "PENDING",
                LocalDateTime.now()
        );
    }

    public List<ExternalRequest> findAll() {
        String sql = "SELECT * FROM external_requests ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExternalRequest.class));
    }
}