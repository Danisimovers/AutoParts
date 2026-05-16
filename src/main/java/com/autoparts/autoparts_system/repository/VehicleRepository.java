package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VehicleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Vehicle> findAll() {
        String sql = "SELECT * FROM vehicles ORDER BY make ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Vehicle.class));
    }

    public Page<Vehicle> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM vehicles";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM vehicles ORDER BY make ASC LIMIT ? OFFSET ?";
        List<Vehicle> vehicles = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Vehicle.class),
                pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(vehicles, pageable, total);
    }

    public Page<Vehicle> search(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM vehicles WHERE LOWER(make) ILIKE ? OR LOWER(model) ILIKE ? OR LOWER(engine) ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern, searchPattern);

        String sql = "SELECT * FROM vehicles WHERE LOWER(make) ILIKE ? OR LOWER(model) ILIKE ? OR LOWER(engine) ILIKE ? ORDER BY make ASC LIMIT ? OFFSET ?";
        List<Vehicle> vehicles = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Vehicle.class),
                searchPattern, searchPattern, searchPattern, pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(vehicles, pageable, total);
    }

    public Vehicle findById(Long id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Vehicle.class), id);
        } catch (Exception e) {
            return null;
        }
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