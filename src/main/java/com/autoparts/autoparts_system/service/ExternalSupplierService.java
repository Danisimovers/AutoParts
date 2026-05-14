package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.ExternalProduct;
import com.autoparts.autoparts_system.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<ExternalProduct> searchAllSuppliers(String query, String searchType) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<ExternalProduct> allProducts = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            if (supplier.getApiUrl() == null || supplier.getApiUrl().isEmpty()) {
                continue;
            }
            try {
                List<ExternalProduct> products = searchFromSupplier(query, searchType, supplier);
                products.forEach(p -> p.setSupplierName(supplier.getName()));
                allProducts.addAll(products);
            } catch (Exception e) {
                System.err.println("Ошибка запроса к поставщику " + supplier.getName() + ": " + e.getMessage());
            }
        }
        return allProducts;
    }

    // Получить fallback-товары для отображения (когда нет поиска)
    public List<ExternalProduct> getFallbackProducts() {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<ExternalProduct> allProducts = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            if (supplier.getApiUrl() == null || supplier.getApiUrl().isEmpty()) {
                continue;
            }
            try {
                List<ExternalProduct> products = getMockFallbackProducts(supplier);
                products.forEach(p -> p.setSupplierName(supplier.getName()));
                allProducts.addAll(products);
            } catch (Exception e) {
                System.err.println("Ошибка получения fallback товаров от " + supplier.getName() + ": " + e.getMessage());
            }
        }
        return allProducts;
    }

    private List<ExternalProduct> searchFromSupplier(String query, String searchType, Supplier supplier) {
        // ВРЕМЕННАЯ ЗАГЛУШКА (пока нет API ключей)
        return getMockProducts(query, searchType, supplier);
    }

    private List<ExternalProduct> getMockProducts(String query, String searchType, Supplier supplier) {
        List<ExternalProduct> mockProducts = Arrays.asList(
                new ExternalProduct(1L, "M8020240", "Пружина подвески передняя", "MARSHALL", 2450.00, 3, "3-5 дней", supplier.getName(), supplier.getId()),
                new ExternalProduct(2L, "050.034", "Опора резиновая", "SAMPA", 890.00, 5, "В наличии", supplier.getName(), supplier.getId()),
                new ExternalProduct(3L, "M12332", "Тормозные колодки", "TRW", 1850.00, 2, "3-5 дней", supplier.getName(), supplier.getId()),
                new ExternalProduct(4L, "D-OIL-001", "Моторное масло 10W-40 дизель", "MANNOL", 2450.00, 10, "В наличии", supplier.getName(), supplier.getId()),
                new ExternalProduct(5L, "0357-1", "Воздушный фильтр", "MANN", 1200.00, 7, "3-5 дней", supplier.getName(), supplier.getId()),
                new ExternalProduct(6L, "M-FILT-001", "Масляный фильтр", "BOSCH", 850.00, 15, "В наличии", supplier.getName(), supplier.getId())
        );

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String lowerQuery = query.toLowerCase().trim();

        switch (searchType) {
            case "startsWith":
                return mockProducts.stream()
                        .filter(p -> p.getFactoryNumber().toLowerCase().startsWith(lowerQuery))
                        .collect(Collectors.toList());
            case "exact":
                return mockProducts.stream()
                        .filter(p -> p.getFactoryNumber().equalsIgnoreCase(query) ||
                                p.getName().equalsIgnoreCase(query))
                        .collect(Collectors.toList());
            case "name":
                return mockProducts.stream()
                        .filter(p -> p.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
            default:
                return mockProducts.stream()
                        .filter(p -> p.getFactoryNumber().toLowerCase().contains(lowerQuery) ||
                                p.getName().toLowerCase().contains(lowerQuery))
                        .collect(Collectors.toList());
        }
    }

    private List<ExternalProduct> getMockFallbackProducts(Supplier supplier) {
        // Товары для отображения на главной (когда нет поиска)
        // Возвращаем первые 3 товара каждого поставщика
        List<ExternalProduct> allMock = Arrays.asList(
                new ExternalProduct(1L, "M8020240", "Пружина подвески передняя", "MARSHALL", 2450.00, 3, "3-5 дней", supplier.getName(), supplier.getId()),
                new ExternalProduct(2L, "050.034", "Опора резиновая", "SAMPA", 890.00, 5, "В наличии", supplier.getName(), supplier.getId()),
                new ExternalProduct(3L, "M12332", "Тормозные колодки", "TRW", 1850.00, 2, "3-5 дней", supplier.getName(), supplier.getId())
        );
        return allMock;
    }
}