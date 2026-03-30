package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class OrderItemRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<OrderItem> findAll() {
        String sql = "SELECT * FROM order_items";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class));
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), orderId);
    }

    public OrderItem findById(Long id) {
        String sql = "SELECT * FROM order_items WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(OrderItem.class), id);
    }

    public void save(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, item.getOrderId(), item.getProductId(), item.getQuantity(), item.getPrice());
    }

    public void deleteByOrderId(Long orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        jdbcTemplate.update(sql, orderId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM order_items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}