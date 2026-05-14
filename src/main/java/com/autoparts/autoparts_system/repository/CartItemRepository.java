package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartItemRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CartItem> findByCartId(Long cartId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CartItem.class), cartId);
    }

    public CartItem findByCartIdAndTempId(Long cartId, String tempId) {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? AND temp_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(CartItem.class), cartId, tempId);
        } catch (Exception e) {
            return null;
        }
    }

    public void save(CartItem item) {
        String sql = "INSERT INTO cart_items (cart_id, temp_id, product_id, item_type, product_name, factory_number, producer, supplier_name, price, quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                item.getCartId(),
                item.getTempId(),
                item.getProductId(),
                item.getItemType(),
                item.getProductName(),
                item.getFactoryNumber(),
                item.getProducer(),
                item.getSupplierName(),
                item.getPrice(),
                item.getQuantity()
        );
    }

    public void updateQuantity(Long cartId, String tempId, Integer quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND temp_id = ?";
        jdbcTemplate.update(sql, quantity, cartId, tempId);
    }

    public void deleteByCartIdAndTempId(Long cartId, String tempId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND temp_id = ?";
        jdbcTemplate.update(sql, cartId, tempId);
    }

    public void deleteAllByCartId(Long cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        jdbcTemplate.update(sql, cartId);
    }
}