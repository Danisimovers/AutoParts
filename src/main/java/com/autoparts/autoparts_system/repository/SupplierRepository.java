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
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Supplier.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public void save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact, email, phone, address, api_url, api_key) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                supplier.getName(),
                supplier.getContact(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getApiUrl(),
                supplier.getApiKey()
        );
    }

    public void update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, email = ?, phone = ?, address = ?, api_url = ?, api_key = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                supplier.getName(),
                supplier.getContact(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getApiUrl(),
                supplier.getApiKey(),
                supplier.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}