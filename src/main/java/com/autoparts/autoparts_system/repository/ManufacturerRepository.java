package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Manufacturer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ManufacturerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Manufacturer> findAll() {
        String sql = "SELECT * FROM manufacturers";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Manufacturer.class));
    }

    public Manufacturer findById(Long id) {
        String sql = "SELECT * FROM manufacturers WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Manufacturer.class), id);
    }

    public void save(Manufacturer manufacturer) {
        String sql = "INSERT INTO manufacturers (name, country, contact_info) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, manufacturer.getName(), manufacturer.getCountry(), manufacturer.getContactInfo());
    }

    public void update(Manufacturer manufacturer) {
        String sql = "UPDATE manufacturers SET name = ?, country = ?, contact_info = ? WHERE id = ?";
        jdbcTemplate.update(sql, manufacturer.getName(), manufacturer.getCountry(), manufacturer.getContactInfo(), manufacturer.getId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM manufacturers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}