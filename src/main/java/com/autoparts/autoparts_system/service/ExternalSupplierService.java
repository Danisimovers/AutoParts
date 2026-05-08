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

    public List<ExternalProduct> searchAllSuppliers(String query) {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        List<ExternalProduct> allProducts = new ArrayList<>();

        for (Supplier supplier : suppliers) {
            if (supplier.getApiUrl() == null || supplier.getApiUrl().isEmpty()) {
                continue;
            }
            try {
                List<ExternalProduct> products = searchFromSupplier(query, supplier);
                products.forEach(p -> p.setSupplierName(supplier.getName()));
                allProducts.addAll(products);
            } catch (Exception e) {
                System.err.println("Ошибка запроса к поставщику " + supplier.getName() + ": " + e.getMessage());
            }
        }
        return allProducts;
    }

    private List<ExternalProduct> searchFromSupplier(String query, Supplier supplier) {
        // ВРЕМЕННАЯ ЗАГЛУШКА (пока нет API ключей)
        return getMockProducts(query, supplier);
    }

    private List<ExternalProduct> getMockProducts(String query, Supplier supplier) {
        List<ExternalProduct> mockProducts = Arrays.asList(
                new ExternalProduct(1L, "M8020240", "Пружина подвески передняя", "MARSHALL", 2450.00, 3, "3-5 дней", supplier.getName(), supplier.getId()),
                new ExternalProduct(2L, "050.034", "Опора резиновая", "SAMPA", 890.00, 5, "В наличии", supplier.getName(), supplier.getId()),
                new ExternalProduct(3L, "M12332", "Тормозные колодки", "TRW", 1850.00, 2, "3-5 дней", supplier.getName(), supplier.getId())
        );

        return mockProducts.stream()
                .filter(p -> p.getFactoryNumber().toLowerCase().contains(query.toLowerCase()) ||
                        p.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}