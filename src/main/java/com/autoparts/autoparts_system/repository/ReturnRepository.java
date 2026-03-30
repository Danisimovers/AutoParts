package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Return;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ReturnRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Return> findAll() {
        String sql = "SELECT * FROM returns";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Return.class));
    }

    public Return findById(Long id) {
        String sql = "SELECT * FROM returns WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Return.class), id);
    }

    public List<Return> findByOrderItemId(Long orderItemId) {
        String sql = "SELECT * FROM returns WHERE order_item_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Return.class), orderItemId);
    }

    public void save(Return returnObj) {
        String sql = "INSERT INTO returns (order_item_id, reason, status, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, returnObj.getOrderItemId(), returnObj.getReason(),
                returnObj.getStatus(), returnObj.getCreatedAt());
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE returns SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM returns WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}