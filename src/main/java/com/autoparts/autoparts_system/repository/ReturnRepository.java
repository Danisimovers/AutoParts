package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Return;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReturnRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Получить все заявки (для админа/менеджера)
    public List<Return> findAll() {
        String sql = "SELECT * FROM returns ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Return.class));
    }

    // Найти заявку по ID
    public Return findById(Long id) {
        String sql = "SELECT * FROM returns WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Return.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    // Найти заявки по позиции заказа
    public List<Return> findByOrderItemId(Long orderItemId) {
        String sql = "SELECT * FROM returns WHERE order_item_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Return.class), orderItemId);
    }

    // Найти заявки пользователя
    public List<Return> findByUserId(Long userId) {
        String sql = "SELECT * FROM returns WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Return.class), userId);
    }

    // Сохранить новую заявку
    public void save(Return returnObj) {
        String sql = "INSERT INTO returns (order_item_id, user_id, reason, status, created_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                returnObj.getOrderItemId(),
                returnObj.getUserId(),
                returnObj.getReason(),
                returnObj.getStatus() != null ? returnObj.getStatus() : "PENDING",
                LocalDateTime.now()
        );
    }

    // Обновить статус заявки
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE returns SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);
    }

    // Удалить заявку
    public void deleteById(Long id) {
        String sql = "DELETE FROM returns WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}