package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Inventory;
import com.autoparts.autoparts_system.repository.InventoryRepository;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> searchProducts(String search, Pageable pageable) {
        return productRepository.searchWithPagination(search, pageable);
    }

    public Page<Product> searchWithFilters(String search, Long categoryId, Long manufacturerId, Long vehicleId, Pageable pageable) {
        return productRepository.searchWithFilters(search, categoryId, manufacturerId, vehicleId, pageable);
    }

    public Page<Product> filterBy(Long categoryId, Long manufacturerId, Long vehicleId, Pageable pageable) {
        return productRepository.filterBy(categoryId, manufacturerId, vehicleId, pageable);
    }

    public long count() {
        return productRepository.count();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product createProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара обязательно");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше 0");
        }
        productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProductId(product.getId());
        inventory.setQuantity(0);
        inventory.setWarehouseId(1L);  // ← ИСПРАВЛЕНО: ID основного склада = 1
        inventoryRepository.save(inventory);
        return product;
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Товар не найден");
        }
        product.setId(id);
        productRepository.update(product);
        return product;
    }

    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Товар не найден");
        }
        productRepository.deleteById(id);
    }
}