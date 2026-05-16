package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Category> findAll() {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }

    // НОВЫЙ МЕТОД: Пагинация для категорий
    public Page<Category> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM categories";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM categories ORDER BY name LIMIT ? OFFSET ?";
        List<Category> categories = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Category.class),
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(categories, pageable, total);
    }

    // НОВЫЙ МЕТОД: Поиск категорий по названию или описанию с пагинацией
    public Page<Category> searchByName(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) ILIKE ? OR LOWER(description) ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern);

        String sql = "SELECT * FROM categories WHERE LOWER(name) ILIKE ? OR LOWER(description) ILIKE ? ORDER BY name LIMIT ? OFFSET ?";
        List<Category> categories = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Category.class),
                searchPattern, searchPattern,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(categories, pageable, total);
    }

    // НОВЫЙ МЕТОД: Подсчет всех категорий
    public long count() {
        String sql = "SELECT COUNT(*) FROM categories";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        List<Category> categories = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class), id);
        return categories.isEmpty() ? null : categories.get(0);
    }

    public void save(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        jdbcTemplate.update(sql, category.getName(), category.getDescription());
    }

    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        jdbcTemplate.update(sql, category.getName(), category.getDescription(), category.getId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}