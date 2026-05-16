package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.ExternalProduct;
import com.autoparts.autoparts_system.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExternalSupplierService {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private RestTemplate restTemplate;

    // Поиск товаров у всех поставщиков
    public List<ExternalProduct> searchAllSuppliers(String query, String searchType) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<ExternalProduct> allProducts = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            // Пропускаем поставщиков без API URL
            if (supplier.getApiUrl() == null || supplier.getApiUrl().isEmpty()) {
                System.out.println("У поставщика " + supplier.getName() + " нет API URL");
                continue;
            }

            try {
                // РЕАЛЬНЫЙ ЗАПРОС к API поставщика с передачей supplierId
                List<ExternalProduct> products = searchFromSupplier(query, searchType, supplier);
                products.forEach(p -> {
                    p.setSupplierName(supplier.getName());
                    p.setSupplierId(supplier.getId());
                });
                allProducts.addAll(products);
                System.out.println("Найдено " + products.size() + " товаров у " + supplier.getName());
            } catch (Exception e) {
                System.err.println("Ошибка запроса к " + supplier.getName() + ": " + e.getMessage());
            }
        }
        return allProducts;
    }

    // Реальный HTTP-запрос к API поставщика с передачей supplierId
    private List<ExternalProduct> searchFromSupplier(String query, String searchType, Supplier supplier) {
        try {
            // Формируем URL для запроса с передачей supplierId
            String url = supplier.getApiUrl() + "/search?query=" + query + "&searchType=" + searchType + "&supplierId=" + supplier.getId();
            System.out.println("Запрос к API: " + url);

            // Выполняем GET-запрос
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                Object data = response.getBody().getData();
                if (data instanceof List) {
                    return convertToProductList((List<?>) data, supplier);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при запросе к " + supplier.getName() + ": " + e.getMessage());
        }
        return Collections.emptyList();
    }

    // Конвертация ответа API в список ExternalProduct
    @SuppressWarnings("unchecked")
    private List<ExternalProduct> convertToProductList(List<?> data, Supplier supplier) {
        List<ExternalProduct> products = new ArrayList<>();
        for (Object item : data) {
            if (item instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) item;
                ExternalProduct product = new ExternalProduct();
                product.setId(((Number) map.get("id")).longValue());
                product.setFactoryNumber((String) map.get("factoryNumber"));
                product.setName((String) map.get("name"));
                product.setProducer((String) map.get("producer"));
                product.setPrice(((Number) map.get("price")).doubleValue());
                product.setStock(((Number) map.get("stock")).intValue());
                product.setDelivery((String) map.get("delivery"));
                product.setSupplierName(supplier.getName());
                product.setSupplierId(supplier.getId());
                products.add(product);
            }
        }
        return products;
    }

    // Получить все товары от поставщиков (для fallback)
    public List<ExternalProduct> getAllProductsFromSuppliers() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<ExternalProduct> allProducts = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            if (supplier.getApiUrl() == null || supplier.getApiUrl().isEmpty()) {
                continue;
            }
            try {
                String url = supplier.getApiUrl() + "/products?supplierId=" + supplier.getId();
                ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url, ApiResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().isSuccess()) {
                    List<ExternalProduct> products = convertToProductList((List<?>) response.getBody().getData(), supplier);
                    allProducts.addAll(products);
                }
            } catch (Exception e) {
                System.err.println("Ошибка получения товаров от " + supplier.getName() + ": " + e.getMessage());
            }
        }
        return allProducts;
    }

    public List<ExternalProduct> getFallbackProducts() {
        return getAllProductsFromSuppliers();
    }
}