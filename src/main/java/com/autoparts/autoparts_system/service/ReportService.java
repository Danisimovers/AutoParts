package com.autoparts.autoparts_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Выручка за период
    public List<Map<String, Object>> getRevenue(String period) {
        String sql;
        if ("week".equals(period)) {
            sql = "SELECT DATE(created_at) as date, SUM(total) as revenue " +
                    "FROM sales_orders WHERE created_at >= CURRENT_DATE - INTERVAL '7 days' " +
                    "GROUP BY DATE(created_at) ORDER BY date";
        } else if ("month".equals(period)) {
            sql = "SELECT DATE(created_at) as date, SUM(total) as revenue " +
                    "FROM sales_orders WHERE created_at >= CURRENT_DATE - INTERVAL '30 days' " +
                    "GROUP BY DATE(created_at) ORDER BY date";
        } else {
            sql = "SELECT DATE(created_at) as date, SUM(total) as revenue " +
                    "FROM sales_orders GROUP BY DATE(created_at) ORDER BY date DESC LIMIT 30";
        }
        return jdbcTemplate.queryForList(sql);
    }

    // 2. Топ-10 товаров по продажам
    public List<Map<String, Object>> getTopProducts() {
        String sql = "SELECT p.id, p.name, p.sku, SUM(oi.quantity) as total_quantity, SUM(oi.price * oi.quantity) as total_revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "GROUP BY p.id, p.name, p.sku " +
                "ORDER BY total_quantity DESC LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    // 3. Количество заказов по статусам
    public List<Map<String, Object>> getOrdersByStatus() {
        String sql = "SELECT status, COUNT(*) as count FROM sales_orders GROUP BY status";
        return jdbcTemplate.queryForList(sql);
    }

    // 4. Общая статистика
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new java.util.HashMap<>();

        String revenueSql = "SELECT COALESCE(SUM(total), 0) as total_revenue FROM sales_orders";
        summary.put("totalRevenue", jdbcTemplate.queryForObject(revenueSql, BigDecimal.class));

        String ordersSql = "SELECT COUNT(*) FROM sales_orders";
        summary.put("totalOrders", jdbcTemplate.queryForObject(ordersSql, Integer.class));

        String usersSql = "SELECT COUNT(*) FROM users";
        summary.put("totalUsers", jdbcTemplate.queryForObject(usersSql, Integer.class));

        String productsSql = "SELECT COUNT(*) FROM products";
        summary.put("totalProducts", jdbcTemplate.queryForObject(productsSql, Integer.class));

        return summary;
    }
}