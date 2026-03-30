package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Category findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Category.class), id);
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