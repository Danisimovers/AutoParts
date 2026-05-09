package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.service.ProductService;
import com.autoparts.autoparts_system.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SearchController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    private String removeHyphens(String str) {
        if (str == null) return null;
        return str.replace("-", "");
    }

    @GetMapping
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false, defaultValue = "contains") String searchType) {

        System.out.println("=== SEARCH ===");
        System.out.println("Query: " + query);
        System.out.println("SearchType: " + searchType);
        System.out.println("VehicleId: " + vehicleId);

        List<Product> products;

        if (query != null && !query.isEmpty()) {
            String normalizedQuery = removeHyphens(query);
            System.out.println("Normalized query: " + normalizedQuery);

            if ("exact".equals(searchType)) {
                // Точный поиск: регистронезависимый, без учета дефисов
                String sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), normalizedQuery, normalizedQuery);
                System.out.println("Exact search, found: " + products.size());
            } else if ("startsWith".equals(searchType)) {
                String searchPattern = normalizedQuery + "%";
                String sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchPattern, searchPattern);
                System.out.println("StartsWith search, pattern: " + searchPattern + ", found: " + products.size());
            } else if ("name".equals(searchType)) {
                String searchPattern = "%" + normalizedQuery + "%";
                String sql = "SELECT * FROM products WHERE REPLACE(name, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchPattern);
                System.out.println("Name search, found: " + products.size());
            } else {
                String searchPattern = "%" + normalizedQuery + "%";
                String sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ? OR REPLACE(oem_code, '-', '') ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchPattern, searchPattern, searchPattern);
                System.out.println("Contains search, found: " + products.size());
            }
        } else {
            products = productService.getAllProducts();
        }

        if (vehicleId != null && vehicleId > 0 && !products.isEmpty()) {
            String vehicleSql = "SELECT p.* FROM products p JOIN product_vehicle_compat pvc ON p.id = pvc.product_id WHERE pvc.vehicle_id = ? AND p.id IN (";
            String ids = products.stream().map(p -> String.valueOf(p.getId())).collect(Collectors.joining(","));
            vehicleSql += ids + ")";
            products = jdbcTemplate.query(vehicleSql, new BeanPropertyRowMapper<>(Product.class), vehicleId);
            System.out.println("After vehicle filter, found: " + products.size());
        }

        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Результаты поиска", Map.of("products", dtos, "count", dtos.size())));
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(stockService.getQuantity(product.getId()));
        return dto;
    }
}