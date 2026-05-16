package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Return;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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

    // Пагинация для заявок на возврат
    public Page<Return> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM returns";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        List<Return> returns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        }, pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(returns, pageable, total);
    }

    // Поиск заявок - ИСПРАВЛЕННЫЙ
    public Page<Return> search(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        // Исправлено: добавили JOIN в countSql
        String countSql = "SELECT COUNT(*) FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE LOWER(CAST(r.id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(oi.order_id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(r.user_id AS TEXT)) ILIKE ?";

        int total = jdbcTemplate.queryForObject(countSql, Integer.class,
                searchPattern, searchPattern, searchPattern);

        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE LOWER(CAST(r.id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(oi.order_id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(r.user_id AS TEXT)) ILIKE ? " +
                "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";

        List<Return> returns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        }, searchPattern, searchPattern, searchPattern, pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(returns, pageable, total);
    }

    // Фильтр по статусу
    public Page<Return> findByStatus(String status, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM returns WHERE status = ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, status);

        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE r.status = ? " +
                "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";
        List<Return> returns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        }, status, pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(returns, pageable, total);
    }

    // Поиск с фильтром по статусу - ИСПРАВЛЕННЫЙ
    public Page<Return> searchWithStatus(String search, String status, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        // Исправлено: добавили JOIN в countSql
        String countSql = "SELECT COUNT(*) FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE (LOWER(CAST(r.id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(oi.order_id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(r.user_id AS TEXT)) ILIKE ?) AND r.status = ?";

        int total = jdbcTemplate.queryForObject(countSql, Integer.class,
                searchPattern, searchPattern, searchPattern, status);

        String sql = "SELECT r.id, r.order_item_id, oi.order_id, r.user_id, r.reason, r.status, r.created_at " +
                "FROM returns r " +
                "JOIN order_items oi ON r.order_item_id = oi.id " +
                "WHERE (LOWER(CAST(r.id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(oi.order_id AS TEXT)) ILIKE ? " +
                "OR LOWER(CAST(r.user_id AS TEXT)) ILIKE ?) AND r.status = ? " +
                "ORDER BY r.created_at DESC LIMIT ? OFFSET ?";

        List<Return> returns = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Return ret = new Return();
            ret.setId(rs.getLong("id"));
            ret.setOrderItemId(rs.getLong("order_item_id"));
            ret.setOrderId(rs.getLong("order_id"));
            ret.setUserId(rs.getLong("user_id"));
            ret.setReason(rs.getString("reason"));
            ret.setStatus(rs.getString("status"));
            ret.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return ret;
        }, searchPattern, searchPattern, searchPattern, status, pageable.getPageSize(), pageable.getOffset());

        return new PageImpl<>(returns, pageable, total);
    }
}