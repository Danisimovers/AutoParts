package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Cart findByUserId(Long userId) {
        String sql = "SELECT * FROM carts WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Cart.class), userId);
        } catch (Exception e) {
            return null;
        }
    }

    public void createCartForUser(Long userId) {
        String sql = "INSERT INTO carts (user_id) VALUES (?)";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM carts WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void updateUpdatedAt(Long cartId) {
        String sql = "UPDATE carts SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, cartId);
    }
}