package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.UserVehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UserVehicleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(UserVehicle userVehicle) {
        String sql = "INSERT INTO user_vehicles (user_id, vehicle_id, vin, license_plate, nickname, year, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                userVehicle.getUserId(),
                userVehicle.getVehicleId(),
                userVehicle.getVin(),
                userVehicle.getLicensePlate(),
                userVehicle.getNickname(),
                userVehicle.getYear(),
                LocalDateTime.now()
        );
    }

    public List<UserVehicle> findByUserId(Long userId) {
        String sql = "SELECT uv.*, v.make, v.model, v.generation " +
                "FROM user_vehicles uv " +
                "JOIN vehicles v ON uv.vehicle_id = v.id " +
                "WHERE uv.user_id = ? " +
                "ORDER BY uv.created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserVehicle uv = new UserVehicle();
            uv.setId(rs.getLong("id"));
            uv.setUserId(rs.getLong("user_id"));
            uv.setVehicleId(rs.getLong("vehicle_id"));
            uv.setVin(rs.getString("vin"));
            uv.setLicensePlate(rs.getString("license_plate"));
            uv.setNickname(rs.getString("nickname"));
            uv.setYear(rs.getInt("year"));
            uv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            uv.setMake(rs.getString("make"));
            uv.setModel(rs.getString("model"));
            uv.setGeneration(rs.getString("generation"));
            return uv;
        }, userId);
    }

    public UserVehicle findById(Long id) {
        String sql = "SELECT uv.*, v.make, v.model, v.generation " +
                "FROM user_vehicles uv " +
                "JOIN vehicles v ON uv.vehicle_id = v.id " +
                "WHERE uv.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                UserVehicle uv = new UserVehicle();
                uv.setId(rs.getLong("id"));
                uv.setUserId(rs.getLong("user_id"));
                uv.setVehicleId(rs.getLong("vehicle_id"));
                uv.setVin(rs.getString("vin"));
                uv.setLicensePlate(rs.getString("license_plate"));
                uv.setNickname(rs.getString("nickname"));
                uv.setYear(rs.getInt("year"));
                uv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                uv.setMake(rs.getString("make"));
                uv.setModel(rs.getString("model"));
                uv.setGeneration(rs.getString("generation"));
                return uv;
            }, id);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM user_vehicles WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}