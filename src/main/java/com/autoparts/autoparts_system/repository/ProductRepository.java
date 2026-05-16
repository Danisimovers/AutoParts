package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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

    // Получить товары с пагинацией
    public Page<Product> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM products";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM products ORDER BY id LIMIT ? OFFSET ?";
        List<Product> products = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Product.class),
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(products, pageable, total);
    }

    // Поиск с пагинацией
    public Page<Product> searchWithPagination(String search, Pageable pageable) {
        String searchPattern = "%" + search.replace("-", "") + "%";

        String countSql = "SELECT COUNT(*) FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ? OR REPLACE(oem_code, '-', '') ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern, searchPattern);

        String sql = "SELECT * FROM products WHERE REPLACE(sku, '-', '') ILIKE ? OR REPLACE(name, '-', '') ILIKE ? OR REPLACE(oem_code, '-', '') ILIKE ? ORDER BY id LIMIT ? OFFSET ?";
        List<Product> products = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Product.class),
                searchPattern, searchPattern, searchPattern,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(products, pageable, total);
    }

    // НОВЫЙ МЕТОД: Поиск с фильтрами (категория, производитель, автомобиль)
    public Page<Product> searchWithFilters(String search, Long categoryId, Long manufacturerId, Long vehicleId, Pageable pageable) {
        String searchPattern = "%" + search.replace("-", "") + "%";

        StringBuilder sql = new StringBuilder("SELECT p.* FROM products p WHERE (REPLACE(p.sku, '-', '') ILIKE ? OR REPLACE(p.name, '-', '') ILIKE ? OR REPLACE(p.oem_code, '-', '') ILIKE ?)");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE (REPLACE(p.sku, '-', '') ILIKE ? OR REPLACE(p.name, '-', '') ILIKE ? OR REPLACE(p.oem_code, '-', '') ILIKE ?)");

        List<Object> params = new ArrayList<>();
        params.add(searchPattern);
        params.add(searchPattern);
        params.add(searchPattern);

        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            countSql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        if (manufacturerId != null && manufacturerId > 0) {
            sql.append(" AND p.manufacturer_id = ?");
            countSql.append(" AND p.manufacturer_id = ?");
            params.add(manufacturerId);
        }

        if (vehicleId != null && vehicleId > 0) {
            sql.append(" AND p.id IN (SELECT product_id FROM product_vehicle_compat WHERE vehicle_id = ?)");
            countSql.append(" AND p.id IN (SELECT product_id FROM product_vehicle_compat WHERE vehicle_id = ?)");
            params.add(vehicleId);
        }

        sql.append(" ORDER BY p.id LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Product> products = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Product.class), params.toArray());

        List<Object> countParams = params.subList(0, params.size() - 2);
        int total = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

        return new PageImpl<>(products, pageable, total);
    }

    // НОВЫЙ МЕТОД: Фильтрация без поиска
    public Page<Product> filterBy(Long categoryId, Long manufacturerId, Long vehicleId, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT p.* FROM products p WHERE 1=1");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (categoryId != null && categoryId > 0) {
            sql.append(" AND p.category_id = ?");
            countSql.append(" AND p.category_id = ?");
            params.add(categoryId);
        }

        if (manufacturerId != null && manufacturerId > 0) {
            sql.append(" AND p.manufacturer_id = ?");
            countSql.append(" AND p.manufacturer_id = ?");
            params.add(manufacturerId);
        }

        if (vehicleId != null && vehicleId > 0) {
            sql.append(" AND p.id IN (SELECT product_id FROM product_vehicle_compat WHERE vehicle_id = ?)");
            countSql.append(" AND p.id IN (SELECT product_id FROM product_vehicle_compat WHERE vehicle_id = ?)");
            params.add(vehicleId);
        }

        sql.append(" ORDER BY p.id LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<Product> products = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Product.class), params.toArray());

        List<Object> countParams = params.subList(0, params.size() - 2);
        int total = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, countParams.toArray());

        return new PageImpl<>(products, pageable, total);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM products";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        List<Product> products = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class), id);
        return products.isEmpty() ? null : products.get(0);
    }

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

    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}