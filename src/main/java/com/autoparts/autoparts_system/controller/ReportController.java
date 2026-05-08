package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Выручка за период
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse> getRevenue(@RequestParam(required = false) String period) {
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

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(ApiResponse.success("Выручка загружена", results));
    }

    // 2. Топ-10 товаров по продажам
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse> getTopProducts() {
        String sql = "SELECT p.id, p.name, p.sku, SUM(oi.quantity) as total_quantity, SUM(oi.price * oi.quantity) as total_revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "GROUP BY p.id, p.name, p.sku " +
                "ORDER BY total_quantity DESC LIMIT 10";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(ApiResponse.success("Топ товаров загружен", results));
    }

    // 3. Количество заказов по статусам
    @GetMapping("/orders-by-status")
    public ResponseEntity<ApiResponse> getOrdersByStatus() {
        String sql = "SELECT status, COUNT(*) as count FROM sales_orders GROUP BY status";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return ResponseEntity.ok(ApiResponse.success("Статистика по статусам", results));
    }

    // 4. Общая статистика
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Общая выручка
        String revenueSql = "SELECT COALESCE(SUM(total), 0) as total_revenue FROM sales_orders";
        summary.put("totalRevenue", jdbcTemplate.queryForObject(revenueSql, BigDecimal.class));

        // Количество заказов
        String ordersSql = "SELECT COUNT(*) FROM sales_orders";
        summary.put("totalOrders", jdbcTemplate.queryForObject(ordersSql, Integer.class));

        // Количество пользователей
        String usersSql = "SELECT COUNT(*) FROM users";
        summary.put("totalUsers", jdbcTemplate.queryForObject(usersSql, Integer.class));

        // Количество товаров
        String productsSql = "SELECT COUNT(*) FROM products";
        summary.put("totalProducts", jdbcTemplate.queryForObject(productsSql, Integer.class));

        return ResponseEntity.ok(ApiResponse.success("Общая статистика", summary));
    }
}