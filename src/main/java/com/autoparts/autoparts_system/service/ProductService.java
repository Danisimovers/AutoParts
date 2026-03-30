package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
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