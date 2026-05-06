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

    @GetMapping
    public ResponseEntity<ApiResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long vehicleId) {

        List<Product> products = searchService.search(query, vehicleId);

        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        SearchResultDTO result = new SearchResultDTO();
        result.setQuery(query);
        result.setVehicleId(vehicleId);
        result.setTotalResults(productDTOs.size());
        result.setProducts(productDTOs);

        if (vehicleId != null) {
            try {
                Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
                if (vehicle != null) {
                    result.setVehicleName(vehicle.getMake() + " " + vehicle.getModel());
                }
            } catch (Exception e) {
                result.setVehicleName("Неизвестный автомобиль");
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Поиск выполнен", result));
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