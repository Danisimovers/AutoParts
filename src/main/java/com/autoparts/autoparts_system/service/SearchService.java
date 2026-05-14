package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductService productService;

    // ===== СТАРЫЕ МЕТОДЫ (оставляем как есть, они не используются, но пусть будут) =====

    public List<Product> searchByQuery(String query) {
        String sql = "SELECT * FROM products WHERE name ILIKE ? OR sku ILIKE ? OR oem_code ILIKE ?";
        String searchParam = "%" + query + "%";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class),
                searchParam, searchParam, searchParam);
    }

    public List<Product> searchByVehicle(Long vehicleId) {
        String sql = "SELECT p.* FROM products p " +
                "JOIN product_vehicle_compat pvc ON p.id = pvc.product_id " +
                "WHERE pvc.vehicle_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), vehicleId);
    }

    public List<Product> search(String query, Long vehicleId) {
        if (vehicleId != null && query != null && !query.isEmpty()) {
            String sql = "SELECT p.* FROM products p " +
                    "JOIN product_vehicle_compat pvc ON p.id = pvc.product_id " +
                    "WHERE pvc.vehicle_id = ? AND (p.name ILIKE ? OR p.sku ILIKE ? OR p.oem_code ILIKE ?)";
            String searchParam = "%" + query + "%";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class),
                    vehicleId, searchParam, searchParam, searchParam);
        } else if (vehicleId != null) {
            return searchByVehicle(vehicleId);
        } else if (query != null && !query.isEmpty()) {
            return searchByQuery(query);
        } else {
            return productService.getAllProducts();
        }
    }

    // ===== НОВЫЕ МЕТОДЫ (для контроллера) =====

    private String removeHyphens(String str) {
        if (str == null) return null;
        return str.replace("-", "");
    }

    /**
     * Расширенный поиск с поддержкой:
     * - типов поиска (contains, exact, startsWith, name)
     * - фильтрации по категории, производителю, автомобилю
     */
    public List<Product> searchAdvanced(String query, String searchType, Long categoryId, Long manufacturerId, Long vehicleId) {
        List<Product> products = new ArrayList<>();

        // 1. Поиск по тексту (если есть)
        if (query != null && !query.isEmpty()) {
            String normalizedQuery = removeHyphens(query);

            String sql;
            if ("exact".equals(searchType)) {
                sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), normalizedQuery, normalizedQuery);
            } else if ("startsWith".equals(searchType)) {
                String searchPattern = normalizedQuery + "%";
                sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchPattern, searchPattern);
            } else if ("name".equals(searchType)) {
                String searchPattern = "%" + normalizedQuery + "%";
                sql = "SELECT * FROM products WHERE REPLACE(name, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchPattern);
            } else { // contains (по умолчанию)
                String searchPattern = "%" + normalizedQuery + "%";
                sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ? OR REPLACE(oem_code, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchPattern, searchPattern, searchPattern);
            }
        } else {
            // Если нет поискового запроса, берем все товары
            products = productService.getAllProducts();
        }

        // 2. Фильтр по категории
        if (categoryId != null && categoryId > 0 && !products.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategoryId() != null && p.getCategoryId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // 3. Фильтр по производителю
        if (manufacturerId != null && manufacturerId > 0 && !products.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getManufacturerId() != null && p.getManufacturerId().equals(manufacturerId))
                    .collect(Collectors.toList());
        }

        // 4. Фильтр по автомобилю (через совместимость)
        if (vehicleId != null && vehicleId > 0 && !products.isEmpty()) {
            String ids = products.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.joining(","));
            if (!ids.isEmpty()) {
                String vehicleSql = "SELECT p.* FROM products p JOIN product_vehicle_compat pvc ON p.id = pvc.product_id WHERE pvc.vehicle_id = ? AND p.id IN (" + ids + ")";
                products = jdbcTemplate.query(vehicleSql, new BeanPropertyRowMapper<>(Product.class), vehicleId);
            } else {
                products = new ArrayList<>();
            }
        }

        return products;
    }
}