package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.ProductVehicleCompat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductVehicleCompatRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<ProductVehicleCompat> findAll() {
        String sql = "SELECT * FROM product_vehicle_compat";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ProductVehicleCompat.class));
    }

    public List<ProductVehicleCompat> findByProductId(Long productId) {
        String sql = "SELECT * FROM product_vehicle_compat WHERE product_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ProductVehicleCompat.class), productId);
    }

    public List<ProductVehicleCompat> findByVehicleId(Long vehicleId) {
        String sql = "SELECT * FROM product_vehicle_compat WHERE vehicle_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ProductVehicleCompat.class), vehicleId);
    }

    public void save(ProductVehicleCompat compat) {
        String sql = "INSERT INTO product_vehicle_compat (product_id, vehicle_id, details) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, compat.getProductId(), compat.getVehicleId(), compat.getDetails());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM product_vehicle_compat WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void deleteByProductId(Long productId) {
        String sql = "DELETE FROM product_vehicle_compat WHERE product_id = ?";
        jdbcTemplate.update(sql, productId);
    }
}