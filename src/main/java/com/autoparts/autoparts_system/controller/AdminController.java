package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.CreateProductRequest;
import com.autoparts.autoparts_system.dto.request.UpdateProductRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import com.autoparts.autoparts_system.model.*;
import com.autoparts.autoparts_system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private StockService stockService;

    @Autowired
    private UserService userService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Товары загружены", dtos));
    }

    @PostMapping("/products")
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
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Товар создан", convertToDTO(created)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
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
            return ResponseEntity.ok(ApiResponse.success("Товар обновлен", convertToDTO(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Товар удален", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/stock/{productId}")
    public ResponseEntity<ApiResponse> updateStock(@PathVariable Long productId, @RequestParam int quantity) {
        try {
            stockService.addStock(productId, quantity, "MAIN");
            return ResponseEntity.ok(ApiResponse.success("Остатки обновлены", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Пользователи загружены", users));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse> changeUserRole(@PathVariable Long id, @RequestParam String role) {
        try {
            userService.changeUserRole(id, role);
            return ResponseEntity.ok(ApiResponse.success("Роль пользователя изменена", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success("Поставщики загружены", suppliers));
    }

    @PostMapping("/suppliers")
    public ResponseEntity<ApiResponse> createSupplier(@RequestBody Supplier supplier) {
        try {
            Supplier created = supplierService.createSupplier(supplier);
            return ResponseEntity.ok(ApiResponse.success("Поставщик создан", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/suppliers/{id}")
    public ResponseEntity<ApiResponse> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        try {
            Supplier updated = supplierService.updateSupplier(id, supplier);
            return ResponseEntity.ok(ApiResponse.success("Поставщик обновлен", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/suppliers/{id}")
    public ResponseEntity<ApiResponse> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(ApiResponse.success("Поставщик удален", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Категории загружены", categories));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse> createCategory(@RequestBody Category category) {
        try {
            Category created = categoryService.createCategory(category);
            return ResponseEntity.ok(ApiResponse.success("Категория создана", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            Category updated = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(ApiResponse.success("Категория обновлена", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Категория удалена", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse> getAllManufacturers() {
        List<Manufacturer> manufacturers = manufacturerService.getAllManufacturers();
        return ResponseEntity.ok(ApiResponse.success("Производители загружены", manufacturers));
    }

    @PostMapping("/manufacturers")
    public ResponseEntity<ApiResponse> createManufacturer(@RequestBody Manufacturer manufacturer) {
        try {
            Manufacturer created = manufacturerService.createManufacturer(manufacturer);
            return ResponseEntity.ok(ApiResponse.success("Производитель создан", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/manufacturers/{id}")
    public ResponseEntity<ApiResponse> updateManufacturer(@PathVariable Long id, @RequestBody Manufacturer manufacturer) {
        try {
            Manufacturer updated = manufacturerService.updateManufacturer(id, manufacturer);
            return ResponseEntity.ok(ApiResponse.success("Производитель обновлен", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/manufacturers/{id}")
    public ResponseEntity<ApiResponse> deleteManufacturer(@PathVariable Long id) {
        try {
            manufacturerService.deleteManufacturer(id);
            return ResponseEntity.ok(ApiResponse.success("Производитель удален", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== Запросы поставщикам ==========

    @GetMapping("/external-requests")
    public ResponseEntity<ApiResponse> getAllExternalRequests() {
        String sql = "SELECT * FROM external_requests ORDER BY created_at DESC";
        List<ExternalRequest> requests = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ExternalRequest.class));
        return ResponseEntity.ok(ApiResponse.success("Запросы загружены", requests));
    }

    @PutMapping("/external-requests/{id}/status")
    public ResponseEntity<ApiResponse> updateExternalRequestStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            String sql = "UPDATE external_requests SET status = ? WHERE id = ?";
            jdbcTemplate.update(sql, status, id);

            String selectSql = "SELECT user_id FROM external_requests WHERE id = ?";
            Long userId = jdbcTemplate.queryForObject(selectSql, Long.class, id);

            String insertSql = "INSERT INTO notifications (user_id, type, title, message, link, is_read) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, userId, "EXTERNAL_REQUEST_STATUS",
                    "Статус вашего запроса изменен",
                    "Статус запроса на товар изменен на: " + getStatusText(status),
                    "/profile?tab=external-requests", false);

            return ResponseEntity.ok(ApiResponse.success("Статус обновлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    @PostMapping("/external-requests/{id}/order")
    public ResponseEntity<ApiResponse> orderFromRequest(@PathVariable Long id) {
        try {
            String selectSql = "SELECT * FROM external_requests WHERE id = ?";
            Map<String, Object> request = jdbcTemplate.queryForMap(selectSql, id);

            String status = (String) request.get("status");
            if (!"PROCESSING".equals(status) && !"PENDING".equals(status)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Заказ уже обработан"));
            }

            Object supplierIdObj = request.get("supplier_id");
            Long supplierId;
            if (supplierIdObj == null) {
                String supplierName = (String) request.get("supplier_name");
                String findSupplierSql = "SELECT id FROM suppliers WHERE name = ?";
                supplierId = jdbcTemplate.queryForObject(findSupplierSql, Long.class, supplierName);
            } else {
                supplierId = ((Number) supplierIdObj).longValue();
            }

            String insertOrderSql = "INSERT INTO purchase_orders (supplier_id, date, status, total) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertOrderSql,
                    supplierId,
                    LocalDate.now(),
                    "ORDERED",
                    request.get("price"));

            String updateSql = "UPDATE external_requests SET status = 'ORDERED' WHERE id = ?";
            jdbcTemplate.update(updateSql, id);

            Long userId = ((Number) request.get("user_id")).longValue();

            String insertNotifSql = "INSERT INTO notifications (user_id, type, title, message, link, is_read) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertNotifSql, userId, "EXTERNAL_REQUEST_STATUS",
                    "Товар заказан у поставщика",
                    "Ваш запрос на товар " + request.get("product_name") + " передан в заказ поставщику",
                    "/profile?tab=external-requests", false);

            return ResponseEntity.ok(ApiResponse.success("Заказ поставщику создан", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    @PutMapping("/external-requests/{id}/add-to-stock")
    public ResponseEntity<ApiResponse> addToStockFromRequest(@PathVariable Long id) {
        try {
            System.out.println("=== addToStockFromRequest START ===");
            System.out.println("Request ID: " + id);

            String selectSql = "SELECT * FROM external_requests WHERE id = ?";
            Map<String, Object> request = jdbcTemplate.queryForMap(selectSql, id);

            System.out.println("Request data: " + request);

            String status = (String) request.get("status");
            System.out.println("Status: " + status);

            if (!"ORDERED".equals(status)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Товар еще не заказан у поставщика"));
            }

            String factoryNumber = (String) request.get("factory_number");
            String producerName = (String) request.get("producer");
            System.out.println("Factory number: " + factoryNumber);
            System.out.println("Producer: " + producerName);

            // Получаем или создаем категорию по умолчанию
            Long defaultCategoryId;
            String checkCategorySql = "SELECT id FROM categories WHERE name = 'Без категории'";
            try {
                defaultCategoryId = jdbcTemplate.queryForObject(checkCategorySql, Long.class);
            } catch (Exception e) {
                String insertCategorySql = "INSERT INTO categories (name) VALUES ('Без категории')";
                jdbcTemplate.update(insertCategorySql);
                defaultCategoryId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            }

            // Получаем или создаем производителя из запроса
            Long manufacturerId;
            String checkManufacturerSql = "SELECT id FROM manufacturers WHERE name = ?";
            try {
                manufacturerId = jdbcTemplate.queryForObject(checkManufacturerSql, Long.class, producerName);
            } catch (Exception e) {
                String insertManufacturerSql = "INSERT INTO manufacturers (name) VALUES (?)";
                jdbcTemplate.update(insertManufacturerSql, producerName);
                manufacturerId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
                System.out.println("Created new manufacturer: " + producerName + " with ID: " + manufacturerId);
            }

            String checkProductSql = "SELECT id FROM products WHERE sku = ?";
            Long productId;
            try {
                productId = jdbcTemplate.queryForObject(checkProductSql, Long.class, factoryNumber);
                System.out.println("Existing product found: " + productId);
            } catch (Exception e) {
                System.out.println("Product not found, creating new...");
                String insertProductSql = "INSERT INTO products (sku, name, price, category_id, manufacturer_id) VALUES (?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertProductSql,
                        factoryNumber,
                        request.get("product_name"),
                        request.get("price"),
                        defaultCategoryId,
                        manufacturerId);
                productId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
                System.out.println("New product created with ID: " + productId);
            }

            // Добавляем остатки (пока 1 шт, заглушка)
            String checkInventorySql = "SELECT id FROM inventory WHERE product_id = ?";
            try {
                jdbcTemplate.queryForObject(checkInventorySql, Long.class, productId);
                // Если есть — увеличиваем на 1
                String updateInventorySql = "UPDATE inventory SET quantity = quantity + 1 WHERE product_id = ?";
                jdbcTemplate.update(updateInventorySql, productId);
            } catch (Exception e) {
                // Если нет — создаем с количеством 1
                String insertInventorySql = "INSERT INTO inventory (product_id, quantity, warehouse_id) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertInventorySql, productId, 1, "MAIN");
            }

            String updateSql = "UPDATE external_requests SET status = 'COMPLETED' WHERE id = ?";
            jdbcTemplate.update(updateSql, id);
            System.out.println("Status updated to COMPLETED");

            Long userId = ((Number) request.get("user_id")).longValue();
            System.out.println("User ID: " + userId);

            String insertNotifSql = "INSERT INTO notifications (user_id, type, title, message, link, is_read) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertNotifSql, userId, "EXTERNAL_REQUEST_STATUS",
                    "Товар поступил на склад",
                    "Запрошенный товар " + request.get("product_name") + " теперь доступен для заказа",
                    "/catalog?search=" + factoryNumber, false);
            System.out.println("Notification sent");

            System.out.println("=== addToStockFromRequest SUCCESS ===");
            return ResponseEntity.ok(ApiResponse.success("Товар добавлен на склад", Map.of("productId", productId)));
        } catch (Exception e) {
            System.err.println("=== addToStockFromRequest ERROR ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "Ожидает обработки";
            case "PROCESSING":
                return "В обработке";
            case "ORDERED":
                return "Заказан у поставщика";
            case "COMPLETED":
                return "Выполнен";
            case "REJECTED":
                return "Отклонен";
            default:
                return status;
        }
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