package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SupplierRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Supplier> findAll() {
        String sql = "SELECT * FROM suppliers ORDER BY name";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Supplier.class));
    }

    // НОВЫЙ МЕТОД: Пагинация для поставщиков
    public Page<Supplier> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM suppliers";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM suppliers ORDER BY name LIMIT ? OFFSET ?";
        List<Supplier> suppliers = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Supplier.class),
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(suppliers, pageable, total);
    }

    // НОВЫЙ МЕТОД: Поиск поставщиков с пагинацией
    public Page<Supplier> search(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM suppliers WHERE LOWER(name) ILIKE ? OR LOWER(contact) ILIKE ? OR LOWER(email) ILIKE ? OR LOWER(phone) ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern, searchPattern, searchPattern);

        String sql = "SELECT * FROM suppliers WHERE LOWER(name) ILIKE ? OR LOWER(contact) ILIKE ? OR LOWER(email) ILIKE ? OR LOWER(phone) ILIKE ? ORDER BY name LIMIT ? OFFSET ?";
        List<Supplier> suppliers = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Supplier.class),
                searchPattern, searchPattern, searchPattern, searchPattern,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(suppliers, pageable, total);
    }

    // НОВЫЙ МЕТОД: Подсчет всех поставщиков
    public long count() {
        String sql = "SELECT COUNT(*) FROM suppliers";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Supplier findById(Long id) {
        String sql = "SELECT * FROM suppliers WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Supplier.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    public void save(Supplier supplier) {
        String sql = "INSERT INTO suppliers (name, contact, email, phone, address, api_url, api_key) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                supplier.getName(),
                supplier.getContact(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getApiUrl(),
                supplier.getApiKey()
        );
    }

    public void update(Supplier supplier) {
        String sql = "UPDATE suppliers SET name = ?, contact = ?, email = ?, phone = ?, address = ?, api_url = ?, api_key = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                supplier.getName(),
                supplier.getContact(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getApiUrl(),
                supplier.getApiKey(),
                supplier.getId()
        );
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM suppliers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}