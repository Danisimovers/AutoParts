package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.CreateProductRequest;
import com.autoparts.autoparts_system.dto.request.UpdateProductRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import com.autoparts.autoparts_system.dto.response.ProductDetailDTO;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductVehicleCompatService compatService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Товары успешно загружены", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Товар не найден"));
        }
        ProductDetailDTO dto = convertToDetailDTO(product);
        return ResponseEntity.ok(ApiResponse.success("Товар успешно загружен", dto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(@RequestBody CreateProductRequest request) {
        try {
            Product product = new Product();
            product.setSku(request.getSku());
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setCategoryId(request.getCategoryId());
            product.setManufacturerId(request.getManufacturerId());
            product.setOemCode(request.getOemCode());

            Product created = productService.createProduct(product);

            if (request.getCompatibleVehicleIds() != null) {
                for (Long vehicleId : request.getCompatibleVehicleIds()) {
                    compatService.addCompatibility(created.getId(), vehicleId, null);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Товар успешно создан", convertToDTO(created)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        try {
            Product product = new Product();
            product.setSku(request.getSku());
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setCategoryId(request.getCategoryId());
            product.setManufacturerId(request.getManufacturerId());
            product.setOemCode(request.getOemCode());

            Product updated = productService.updateProduct(id, product);

            if (request.getCompatibleVehicleIds() != null) {
                compatService.removeAllByProductId(id);
                for (Long vehicleId : request.getCompatibleVehicleIds()) {
                    compatService.addCompatibility(id, vehicleId, null);
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Товар успешно обновлен", convertToDTO(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Товар успешно удален", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse> getProductsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long manufacturerId,
            @RequestParam(required = false) Long vehicleId) {

        int offset = page * size;

        // Базовый SQL с условиями
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM products WHERE 1=1");
        StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM products WHERE 1=1");

        // Добавляем фильтры
        if (categoryId != null && categoryId > 0) {
            sqlBuilder.append(" AND category_id = ").append(categoryId);
            countSqlBuilder.append(" AND category_id = ").append(categoryId);
        }

        if (manufacturerId != null && manufacturerId > 0) {
            sqlBuilder.append(" AND manufacturer_id = ").append(manufacturerId);
            countSqlBuilder.append(" AND manufacturer_id = ").append(manufacturerId);
        }

        // Если есть фильтр по автомобилю, нужно JOIN с таблицей совместимости
        List<Product> products;
        int total;

        if (vehicleId != null && vehicleId > 0) {
            String sqlWithVehicle = "SELECT p.* FROM products p " +
                    "JOIN product_vehicle_compat pvc ON p.id = pvc.product_id " +
                    "WHERE pvc.vehicle_id = ? " +
                    (categoryId != null && categoryId > 0 ? " AND p.category_id = " + categoryId : "") +
                    (manufacturerId != null && manufacturerId > 0 ? " AND p.manufacturer_id = " + manufacturerId : "") +
                    " LIMIT ? OFFSET ?";
            products = jdbcTemplate.query(sqlWithVehicle, new BeanPropertyRowMapper<>(Product.class), vehicleId, size, offset);

            String countWithVehicle = "SELECT COUNT(DISTINCT p.id) FROM products p " +
                    "JOIN product_vehicle_compat pvc ON p.id = pvc.product_id " +
                    "WHERE pvc.vehicle_id = ? " +
                    (categoryId != null && categoryId > 0 ? " AND p.category_id = " + categoryId : "") +
                    (manufacturerId != null && manufacturerId > 0 ? " AND p.manufacturer_id = " + manufacturerId : "");
            total = jdbcTemplate.queryForObject(countWithVehicle, Integer.class, vehicleId);
        } else {
            sqlBuilder.append(" LIMIT ? OFFSET ?");
            products = jdbcTemplate.query(sqlBuilder.toString(), new BeanPropertyRowMapper<>(Product.class), size, offset);
            total = jdbcTemplate.queryForObject(countSqlBuilder.toString(), Integer.class);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("products", products.stream().map(this::convertToDTO).collect(Collectors.toList()));
        response.put("currentPage", page);
        response.put("totalItems", total);
        response.put("totalPages", (int) Math.ceil((double) total / size));

        return ResponseEntity.ok(ApiResponse.success("Товары загружены", response));
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

    private ProductDetailDTO convertToDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOemCode(product.getOemCode());

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

        List<String> vehicles = compatService.getCompatByProductId(product.getId()).stream()
                .map(compat -> {
                    try {
                        var vehicle = vehicleService.getVehicleById(compat.getVehicleId());
                        return vehicle.getMake() + " " + vehicle.getModel() +
                                (vehicle.getGeneration() != null ? " (" + vehicle.getGeneration() + ")" : "");
                    } catch (Exception e) {
                        return "Неизвестный автомобиль";
                    }
                })
                .collect(Collectors.toList());
        dto.setCompatibleVehicles(vehicles);

        return dto;
    }
}