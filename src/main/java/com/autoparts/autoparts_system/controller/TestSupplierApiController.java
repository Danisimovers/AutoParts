package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.ExternalProduct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test-supplier")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TestSupplierApiController {

    // Тестовые данные товаров от поставщиков
    private List<ExternalProduct> getMockProducts() {
        List<ExternalProduct> products = new ArrayList<>();

        // Товары для Корона Авто (supplierId = 2)
        products.add(new ExternalProduct(1L, "M12332", "Тормозные колодки передние TRW", "TRW", 1850.00, 5, "3-5 дней", "Корона Авто", 2L));
        products.add(new ExternalProduct(2L, "050.034", "Опора резиновая подвески", "SAMPA", 890.00, 8, "В наличии", "Корона Авто", 2L));
        products.add(new ExternalProduct(3L, "M8020240", "Пружина подвески передняя", "MARSHALL", 2450.00, 3, "3-5 дней", "Корона Авто", 2L));

        // Товары для АвтоПрофи (supplierId = 3)
        products.add(new ExternalProduct(4L, "D-OIL-001", "Моторное масло 10W-40 дизель", "MANNOL", 2450.00, 10, "В наличии", "АвтоПрофи", 3L));
        products.add(new ExternalProduct(5L, "0357-1", "Воздушный фильтр", "MANN", 1200.00, 7, "3-5 дней", "АвтоПрофи", 3L));
        products.add(new ExternalProduct(6L, "M-FILT-001", "Масляный фильтр", "BOSCH", 850.00, 15, "В наличии", "АвтоПрофи", 3L));
        products.add(new ExternalProduct(11L, "B12345", "Тормозные колодки задние BOSCH", "BOSCH", 1650.00, 4, "3-5 дней", "АвтоПрофи", 3L));

        // Товары для СпецАвто (supplierId = 4)
        products.add(new ExternalProduct(7L, "1802905005730", "Амортизатор подвески передний", "SACHS", 5500.00, 2, "5-7 дней", "СпецАвто", 4L));
        products.add(new ExternalProduct(8L, "87-05066-SX", "Амортизатор подвески", "HENDRICKSON", 4700.00, 2, "3-5 дней", "СпецАвто", 4L));
        products.add(new ExternalProduct(9L, "T0005-7010", "Амортизатор подвески", "VOLVO", 4500.00, 7, "В наличии", "СпецАвто", 4L));
        products.add(new ExternalProduct(10L, "100.164-01", "Амортизатор кабины", "MERCEDES", 4000.00, 2, "3-5 дней", "СпецАвто", 4L));
        products.add(new ExternalProduct(12L, "F98765", "Тормозные колодки передние FERODO", "FERODO", 1950.00, 3, "5-7 дней", "СпецАвто", 4L));

        return products;
    }

    // 1. Поиск товаров (с фильтрацией по поставщику)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "contains") String searchType,
            @RequestParam(required = false) Long supplierId) {

        List<ExternalProduct> allProducts = getMockProducts();

        // Фильтруем по supplierId, если он передан
        if (supplierId != null) {
            allProducts = allProducts.stream()
                    .filter(p -> p.getSupplierId().equals(supplierId))
                    .collect(Collectors.toList());
        }

        List<ExternalProduct> results;
        String lowerQuery = query.toLowerCase().trim();

        switch (searchType) {
            case "startsWith":
                results = allProducts.stream()
                        .filter(p -> p.getFactoryNumber().toLowerCase().startsWith(lowerQuery))
                        .collect(Collectors.toList());
                break;
            case "exact":
                results = allProducts.stream()
                        .filter(p -> p.getFactoryNumber().equalsIgnoreCase(query))
                        .collect(Collectors.toList());
                break;
            case "name":
                results = allProducts.stream()
                        .filter(p -> p.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
                break;
            default:
                results = allProducts.stream()
                        .filter(p -> p.getFactoryNumber().toLowerCase().contains(lowerQuery) ||
                                p.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
        }

        if (results.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Товары не найдены", results));
        }
        return ResponseEntity.ok(ApiResponse.success("Найдено товаров: " + results.size(), results));
    }

    // 2. Получить все товары (с фильтрацией по поставщику)
    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts(@RequestParam(required = false) Long supplierId) {
        List<ExternalProduct> products = getMockProducts();

        // Фильтруем по supplierId, если он передан
        if (supplierId != null) {
            products = products.stream()
                    .filter(p -> p.getSupplierId().equals(supplierId))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("Все товары поставщиков", products));
    }

    // 3. Получить товары по поставщику
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse> getProductsBySupplier(@PathVariable Long supplierId) {
        List<ExternalProduct> products = getMockProducts().stream()
                .filter(p -> p.getSupplierId().equals(supplierId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Товары поставщика", products));
    }

    // 4. Проверка здоровья API
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> health() {
        return ResponseEntity.ok(ApiResponse.success("API работает", null));
    }
}