package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import com.autoparts.autoparts_system.dto.response.SearchResultDTO;
import com.autoparts.autoparts_system.model.ExternalProduct;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.model.Vehicle;
import com.autoparts.autoparts_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.stream.Collectors;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ExternalSupplierService externalSupplierService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false, defaultValue = "contains") String searchType) {

        List<Product> products;

        if (query != null && !query.isEmpty()) {
            String searchQuery = query;
            if ("startsWith".equals(searchType)) {
                searchQuery = query + "%";
            } else if ("exact".equals(searchType)) {
                searchQuery = query;
            } else {
                searchQuery = "%" + query + "%";
            }

            String sql;
            if ("exact".equals(searchType)) {
                sql = "SELECT * FROM products WHERE sku = ? OR name = ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchQuery, searchQuery);
            } else {
                sql = "SELECT * FROM products WHERE sku ILIKE ? OR name ILIKE ? OR oem_code ILIKE ?";
                products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), searchQuery, searchQuery, searchQuery);
            }
        } else {
            products = productService.getAllProducts();
        }

        // Фильтр по автомобилю
        if (vehicleId != null && vehicleId > 0) {
            String vehicleSql = "SELECT p.* FROM products p JOIN product_vehicle_compat pvc ON p.id = pvc.product_id WHERE pvc.vehicle_id = ?";
            products = jdbcTemplate.query(vehicleSql, new BeanPropertyRowMapper<>(Product.class), vehicleId);
        }

        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Результаты поиска", Map.of("products", dtos, "count", dtos.size())));
    }

    @GetMapping("/external")
    public ResponseEntity<ApiResponse> searchExternal(@RequestParam(required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Поставщики", new ArrayList<>()));
        }

        List<ExternalProduct> externalProducts = externalSupplierService.searchAllSuppliers(query);
        return ResponseEntity.ok(ApiResponse.success("Товары от поставщиков", externalProducts));
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());

        if (product.getCategoryId() != null) {
            try {
                dto.setCategoryName(categoryService.getCategoryById(product.getCategoryId()).getName());
            } catch (Exception e) {
                dto.setCategoryName("Неизвестно");
            }
        }
        if (product.getManufacturerId() != null) {
            try {
                dto.setManufacturerName(manufacturerService.getManufacturerById(product.getManufacturerId()).getName());
            } catch (Exception e) {
                dto.setManufacturerName("Неизвестно");
            }
        }

        dto.setStock(stockService.getQuantity(product.getId()));
        return dto;
    }
}