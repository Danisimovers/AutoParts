package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class InventoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Inventory> findAll() {
        String sql = "SELECT * FROM inventory";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Inventory.class));
    }

    public Inventory findByProductId(Long productId) {
        String sql = "SELECT * FROM inventory WHERE product_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Inventory.class), productId);
    }

    public void save(Inventory inventory) {
        String sql = "INSERT INTO inventory (product_id, quantity, warehouse_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, inventory.getProductId(), inventory.getQuantity(), inventory.getWarehouseId());
    }

    public void updateQuantity(Long productId, Integer quantity) {
        String sql = "UPDATE inventory SET quantity = ? WHERE product_id = ?";
        jdbcTemplate.update(sql, quantity, productId);
    }

    public void deleteByProductId(Long productId) {
        String sql = "DELETE FROM inventory WHERE product_id = ?";
        jdbcTemplate.update(sql, productId);
    }
}