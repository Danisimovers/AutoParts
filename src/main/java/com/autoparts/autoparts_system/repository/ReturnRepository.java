package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Return;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ReturnRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Получить все заявки (для админа/менеджера) с order_id
    public List<Return> findAll() {
        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        });
    }

    // Найти заявку по ID
    public Return findById(Long id) {
        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE r.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Return ret = new Return();
                ret.setId(rs.getLong("id"));
                ret.setOrderItemId(rs.getLong("order_item_id"));
                ret.setOrderId(rs.getLong("order_id"));
                ret.setUserId(rs.getLong("user_id"));
                ret.setReason(rs.getString("reason"));
                ret.setStatus(rs.getString("status"));
                ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                return ret;
            }, id);
        } catch (Exception e) {
            return null;
        }
    }

    // Найти заявки по позиции заказа
    public List<Return> findByOrderItemId(Long orderItemId) {
        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE r.order_item_id = ? " +
                "ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        }, orderItemId);
    }

    // Найти заявки пользователя
    public List<Return> findByUserId(Long userId) {
        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        }, userId);
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