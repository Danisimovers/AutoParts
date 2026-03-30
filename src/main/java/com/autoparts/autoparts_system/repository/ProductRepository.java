package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Получить все товары
    public List<Product> findAll() {
        String sql = "SELECT * FROM products";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class));
    }

    // Найти товар по ID
    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Product.class), id);
    }

    // Сохранить новый товар
    public void save(Product product) {
        String sql = "INSERT INTO products (sku, name, description, price, category_id, manufacturer_id, oem_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId(),
                product.getManufacturerId(),
                product.getOemCode()
        );
    }

    // Обновить товар
    public void update(Product product) {
        String sql = "UPDATE products SET sku = ?, name = ?, description = ?, price = ?, " +
                "category_id = ?, manufacturer_id = ?, oem_code = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId(),
                product.getManufacturerId(),
                product.getOemCode(),
                product.getId()
        );
    }

    // Удалить товар по ID
    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}