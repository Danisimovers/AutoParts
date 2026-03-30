package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.PurchaseOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PurchaseOrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<PurchaseOrder> findAll() {
        String sql = "SELECT * FROM purchase_orders";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PurchaseOrder.class));
    }

    public PurchaseOrder findById(Long id) {
        String sql = "SELECT * FROM purchase_orders WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(PurchaseOrder.class), id);
    }

    public List<PurchaseOrder> findBySupplierId(Long supplierId) {
        String sql = "SELECT * FROM purchase_orders WHERE supplier_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PurchaseOrder.class), supplierId);
    }

    public void save(PurchaseOrder order) {
        String sql = "INSERT INTO purchase_orders (supplier_id, date, status, total) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, order.getSupplierId(), order.getDate(), order.getStatus(), order.getTotal());
    }

    public void updateStatus(Long id, String status) {
        String sql = "UPDATE purchase_orders SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM purchase_orders WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}