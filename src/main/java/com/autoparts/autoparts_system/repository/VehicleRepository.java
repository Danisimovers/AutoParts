package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VehicleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Vehicle> findAll() {
        String sql = "SELECT * FROM vehicles";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Vehicle.class));
    }

    public Vehicle findById(Long id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Vehicle.class), id);
    }

    public List<Vehicle> findByMake(String make) {
        String sql = "SELECT * FROM vehicles WHERE make ILIKE ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Vehicle.class), "%" + make + "%");
    }

    public void save(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (make, model, generation, year_from, year_to, engine) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getGeneration(),
                vehicle.getYearFrom(),
                vehicle.getYearTo(),
                vehicle.getEngine()
        );
    }

    public void update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET make = ?, model = ?, generation = ?, " +
                "year_from = ?, year_to = ?, engine = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getGeneration(),
                vehicle.getYearFrom(),
                vehicle.getYearTo(),
                vehicle.getEngine(),
                vehicle.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM vehicles WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}