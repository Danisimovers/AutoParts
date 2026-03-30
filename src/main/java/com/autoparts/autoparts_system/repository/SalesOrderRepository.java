package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.SalesOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

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
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SalesOrder.class), id);
    }

    public void save(SalesOrder order) {
        String sql = "INSERT INTO sales_orders (user_id, total, status, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, order.getUserId(), order.getTotal(), order.getStatus(), order.getCreatedAt());
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