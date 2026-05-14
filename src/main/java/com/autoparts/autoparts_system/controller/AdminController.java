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
import org.springframework.web.bind.annotation.*;

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
    private ExternalRequestService externalRequestService;  // ← новый сервис

    // ========== Товары ==========
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

    // ========== Пользователи ==========
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

    // ========== Поставщики ==========
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

    // ========== Категории ==========
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

    // ========== Производители ==========
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

    // ========== Запросы поставщикам (теперь через сервис) ==========
    @GetMapping("/external-requests")
    public ResponseEntity<ApiResponse> getAllExternalRequests() {
        return ResponseEntity.ok(ApiResponse.success("Запросы загружены", externalRequestService.getAllExternalRequests()));
    }

    @PutMapping("/external-requests/{id}/status")
    public ResponseEntity<ApiResponse> updateExternalRequestStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            externalRequestService.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Статус обновлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    @PostMapping("/external-requests/{id}/order")
    public ResponseEntity<ApiResponse> orderFromRequest(@PathVariable Long id) {
        try {
            externalRequestService.orderFromRequest(id);
            return ResponseEntity.ok(ApiResponse.success("Заказ поставщику создан", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    @PutMapping("/external-requests/{id}/add-to-stock")
    public ResponseEntity<ApiResponse> addToStockFromRequest(@PathVariable Long id) {
        try {
            Map<String, Object> result = externalRequestService.addToStockFromRequest(id);
            return ResponseEntity.ok(ApiResponse.success("Товар добавлен на склад", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
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