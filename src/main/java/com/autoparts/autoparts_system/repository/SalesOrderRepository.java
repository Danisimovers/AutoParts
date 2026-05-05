package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.SalesOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
public class SalesOrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<SalesOrder> findAll() {
        String sql = "SELECT * FROM sales_orders";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SalesOrder.class));
    }

    public List<SalesOrder> findByUserId(Long userId) {
        String sql = "SELECT * FROM sales_orders WHERE user_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SalesOrder.class), userId);
    }

    public SalesOrder findById(Long id) {
        String sql = "SELECT * FROM sales_orders WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SalesOrder.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public void save(SalesOrder order) {
        String sql = "INSERT INTO sales_orders (user_id, total, status, created_at) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotal());
            ps.setString(3, order.getStatus());
            ps.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            order.setId(keyHolder.getKey().longValue());
        }
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE sales_orders SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM sales_orders WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}