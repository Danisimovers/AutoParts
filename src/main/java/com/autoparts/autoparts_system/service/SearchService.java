package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductRepository productRepository;

    // Поиск по названию или OEM коду
    public List<Product> searchByQuery(String query) {
        String sql = "SELECT * FROM products WHERE name ILIKE ? OR sku ILIKE ? OR oem_code ILIKE ?";
        String searchParam = "%" + query + "%";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class),
                searchParam, searchParam, searchParam);
    }

    // Поиск по совместимости с автомобилем
    public List<Product> searchByVehicle(Long vehicleId) {
        String sql = "SELECT p.* FROM products p " +
                "JOIN product_vehicle_compat pvc ON p.id = pvc.product_id " +
                "WHERE pvc.vehicle_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), vehicleId);
    }

    // Комбинированный поиск
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
            return productRepository.findAll();
        }
    }
}