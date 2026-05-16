package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.ProductDTO;
import com.autoparts.autoparts_system.model.ExternalProduct;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.service.ExternalSupplierService;
import com.autoparts.autoparts_system.service.SearchService;
import com.autoparts.autoparts_system.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private ExternalSupplierService externalSupplierService;

    @Autowired
    private StockService stockService;

    // ПОИСК У ПОСТАВЩИКОВ (только при наличии поискового запроса)
    @GetMapping("/external")
    public ResponseEntity<ApiResponse> searchExternalProducts(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "startsWith") String searchType) {

        // Если нет поискового запроса - возвращаем пустой список
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Введите поисковый запрос", new ArrayList<>()));
        }

        List<ExternalProduct> products = externalSupplierService.searchAllSuppliers(query, searchType);
        return ResponseEntity.ok(ApiResponse.success("Товары поставщиков", products));
    }

    // FALLBACK ТОВАРЫ (нужен для фронтенда при загрузке страницы)
    @GetMapping("/external/fallback")
    public ResponseEntity<ApiResponse> getExternalFallbackProducts() {
        // Возвращаем пустой список, чтобы не показывать товары при загрузке
        return ResponseEntity.ok(ApiResponse.success("Товары поставщиков", new ArrayList<>()));
    }

    // ОСНОВНОЙ ПОИСК
    @GetMapping
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long vehicleId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long manufacturerId,
            @RequestParam(required = false, defaultValue = "contains") String searchType) {

        List<Product> products = searchService.searchAdvanced(query, searchType, categoryId, manufacturerId, vehicleId);

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