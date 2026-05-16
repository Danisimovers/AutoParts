package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.ExternalRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        String sql = "INSERT INTO external_requests (user_id, order_id, product_name, factory_number, producer, supplier_name, supplier_id, price, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                request.getUserId(),
                request.getOrderId(),
                request.getProductName(),
                request.getFactoryNumber(),
                request.getProducer(),
                request.getSupplierName(),
                request.getSupplierId(),
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


    public Long findOrderIdById(Long id) {
        String sql = "SELECT order_id FROM external_requests WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    public Long findUserIdById(Long id) {
        String sql = "SELECT user_id FROM external_requests WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, id);
    }

    // Пагинация для запросов
    public Page<ExternalRequest> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM external_requests";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM external_requests ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ExternalRequest> requests = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ExternalRequest.class),
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(requests, pageable, total);
    }

    // Поиск запросов с пагинацией
    public Page<ExternalRequest> search(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM external_requests WHERE LOWER(product_name) ILIKE ? OR LOWER(factory_number) ILIKE ? OR LOWER(producer) ILIKE ? OR LOWER(supplier_name) ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern, searchPattern, searchPattern);

        String sql = "SELECT * FROM external_requests WHERE LOWER(product_name) ILIKE ? OR LOWER(factory_number) ILIKE ? OR LOWER(producer) ILIKE ? OR LOWER(supplier_name) ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ExternalRequest> requests = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(ExternalRequest.class),
                searchPattern, searchPattern, searchPattern, searchPattern,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(requests, pageable, total);
    }
}