package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.VinRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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

    // Пагинация для VIN заявок
    public Page<VinRequest> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM vin_requests";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM vin_requests ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<VinRequest> requests = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(VinRequest.class),
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(requests, pageable, total);
    }

    // Поиск по VIN или описанию
    public Page<VinRequest> search(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM vin_requests WHERE LOWER(vin) ILIKE ? OR LOWER(description) ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern);

        String sql = "SELECT * FROM vin_requests WHERE LOWER(vin) ILIKE ? OR LOWER(description) ILIKE ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<VinRequest> requests = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(VinRequest.class),
                searchPattern, searchPattern,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(requests, pageable, total);
    }

    // Фильтр по статусу
    public Page<VinRequest> findByStatus(String status, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM vin_requests WHERE status = ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, status);

        String sql = "SELECT * FROM vin_requests WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<VinRequest> requests = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(VinRequest.class),
                status,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(requests, pageable, total);
    }

    // Поиск с фильтром по статусу
    public Page<VinRequest> searchWithStatus(String search, String status, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM vin_requests WHERE (LOWER(vin) ILIKE ? OR LOWER(description) ILIKE ?) AND status = ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern, status);

        String sql = "SELECT * FROM vin_requests WHERE (LOWER(vin) ILIKE ? OR LOWER(description) ILIKE ?) AND status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<VinRequest> requests = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(VinRequest.class),
                searchPattern, searchPattern, status,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(requests, pageable, total);
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