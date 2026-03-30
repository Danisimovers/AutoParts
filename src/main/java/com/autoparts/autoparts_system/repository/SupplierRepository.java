package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SupplierRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Supplier> findAll() {
        String sql = "SELECT * FROM suppliers";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Supplier.class));
    }

    public Supplier findById(Long id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Supplier.class), id);
    }

    public void save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact, email, phone, address) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, supplier.getName(), supplier.getContact(),
                supplier.getEmail(), supplier.getPhone(), supplier.getAddress());
    }

    public void update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        jdbcTemplate.update(sql, supplier.getName(), supplier.getContact(),
                supplier.getEmail(), supplier.getPhone(), supplier.getAddress(), supplier.getId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}