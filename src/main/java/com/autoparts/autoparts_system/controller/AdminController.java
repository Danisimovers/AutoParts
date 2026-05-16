package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.CreateProductRequest;
import com.autoparts.autoparts_system.dto.request.CreateVehicleRequest;
import com.autoparts.autoparts_system.dto.request.UpdateProductRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import com.autoparts.autoparts_system.model.*;
import com.autoparts.autoparts_system.service.*;
import com.autoparts.autoparts_system.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private SearchService searchService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ExternalRequestService externalRequestService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    // ========== Товары ==========
    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long manufacturerId,
            @RequestParam(required = false) Long vehicleId
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<Product> productPage;

            // Если есть поисковый запрос
            if (search != null && !search.trim().isEmpty()) {
                productPage = productService.searchWithFilters(search, categoryId, manufacturerId, vehicleId, pageable);
            }
            // Если есть фильтры (без поиска)
            else if (categoryId != null || manufacturerId != null || vehicleId != null) {
                productPage = productService.filterBy(categoryId, manufacturerId, vehicleId, pageable);
            }
            // Обычная пагинация
            else {
                productPage = productService.getProducts(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", productPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()),
                    "total", productPage.getTotalElements(),
                    "totalPages", productPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Товары загружены", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки товаров: " + e.getMessage()));
        }
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
            stockService.addStock(productId, quantity, 1L);
            return ResponseEntity.ok(ApiResponse.success("Остатки обновлены", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== Пользователи ==========
    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<User> userPage;

            // Если есть поиск
            if (search != null && !search.trim().isEmpty()) {
                userPage = userService.searchUsers(search, pageable);
            } else {
                userPage = userService.getAllUsers(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", userPage.getContent(),
                    "total", userPage.getTotalElements(),
                    "totalPages", userPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Пользователи загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки пользователей: " + e.getMessage()));
        }
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
    public ResponseEntity<ApiResponse> getAllSuppliers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<Supplier> supplierPage;

            // Если есть поиск
            if (search != null && !search.trim().isEmpty()) {
                supplierPage = supplierService.searchSuppliers(search, pageable);
            } else {
                supplierPage = supplierService.getAllSuppliers(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", supplierPage.getContent(),
                    "total", supplierPage.getTotalElements(),
                    "totalPages", supplierPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Поставщики загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки поставщиков: " + e.getMessage()));
        }
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
    public ResponseEntity<ApiResponse> getAllCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<Category> categoryPage;

            // Если есть поиск
            if (search != null && !search.trim().isEmpty()) {
                categoryPage = categoryService.searchCategories(search, pageable);
            } else {
                categoryPage = categoryService.getAllCategories(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", categoryPage.getContent(),
                    "total", categoryPage.getTotalElements(),
                    "totalPages", categoryPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Категории загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки категорий: " + e.getMessage()));
        }
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
    public ResponseEntity<ApiResponse> getAllManufacturers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<Manufacturer> manufacturerPage;

            // Если есть поиск
            if (search != null && !search.trim().isEmpty()) {
                manufacturerPage = manufacturerService.searchManufacturers(search, pageable);
            } else {
                manufacturerPage = manufacturerService.getAllManufacturers(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", manufacturerPage.getContent(),
                    "total", manufacturerPage.getTotalElements(),
                    "totalPages", manufacturerPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Производители загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки производителей: " + e.getMessage()));
        }
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

    // ========== Автомобили (Vehicles) ==========
    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse> getAllVehicles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<Vehicle> vehiclesPage;

            // Если есть поиск
            if (search != null && !search.trim().isEmpty()) {
                vehiclesPage = vehicleRepository.search(search, pageable);
            } else {
                vehiclesPage = vehicleRepository.findAll(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", vehiclesPage.getContent(),
                    "total", vehiclesPage.getTotalElements(),
                    "totalPages", vehiclesPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Автомобили загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки автомобилей: " + e.getMessage()));
        }
    }

    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse> createVehicle(@RequestBody CreateVehicleRequest request) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setMake(request.getMake());
            vehicle.setModel(request.getModel());
            vehicle.setGeneration(request.getGeneration());
            vehicle.setYearFrom(request.getYearFrom());
            vehicle.setYearTo(request.getYearTo());
            vehicle.setEngine(request.getEngine());

            Vehicle created = vehicleService.createVehicle(vehicle);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль создан", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<ApiResponse> updateVehicle(@PathVariable Long id, @RequestBody CreateVehicleRequest request) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setMake(request.getMake());
            vehicle.setModel(request.getModel());
            vehicle.setGeneration(request.getGeneration());
            vehicle.setYearFrom(request.getYearFrom());
            vehicle.setYearTo(request.getYearTo());
            vehicle.setEngine(request.getEngine());

            Vehicle updated = vehicleService.updateVehicle(id, vehicle);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль обновлен", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<ApiResponse> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль удален", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== Запросы поставщикам ==========
    @GetMapping("/external-requests")
    public ResponseEntity<ApiResponse> getAllExternalRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit);
            Page<ExternalRequest> requestsPage;

            // Если есть поиск
            if (search != null && !search.trim().isEmpty()) {
                requestsPage = externalRequestService.searchExternalRequests(search, pageable);
            } else {
                requestsPage = externalRequestService.getAllExternalRequests(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", requestsPage.getContent(),
                    "total", requestsPage.getTotalElements(),
                    "totalPages", requestsPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Запросы загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки запросов: " + e.getMessage()));
        }
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