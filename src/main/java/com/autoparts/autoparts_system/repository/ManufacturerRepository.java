package com.autoparts.autoparts_system.repository;

import com.autoparts.autoparts_system.model.Manufacturer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ManufacturerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Manufacturer> findAll() {
        String sql = "SELECT * FROM manufacturers ORDER BY name";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Manufacturer.class));
    }

    // НОВЫЙ МЕТОД: Пагинация для производителей
    public Page<Manufacturer> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM manufacturers";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT * FROM manufacturers ORDER BY name LIMIT ? OFFSET ?";
        List<Manufacturer> manufacturers = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Manufacturer.class),
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(manufacturers, pageable, total);
    }

    // НОВЫЙ МЕТОД: Поиск производителей по названию, стране или контактной информации
    public Page<Manufacturer> searchByName(String search, Pageable pageable) {
        String searchPattern = "%" + search.toLowerCase() + "%";

        String countSql = "SELECT COUNT(*) FROM manufacturers WHERE LOWER(name) ILIKE ? OR LOWER(country) ILIKE ? OR LOWER(contact_info) ILIKE ?";
        int total = jdbcTemplate.queryForObject(countSql, Integer.class, searchPattern, searchPattern, searchPattern);

        String sql = "SELECT * FROM manufacturers WHERE LOWER(name) ILIKE ? OR LOWER(country) ILIKE ? OR LOWER(contact_info) ILIKE ? ORDER BY name LIMIT ? OFFSET ?";
        List<Manufacturer> manufacturers = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Manufacturer.class),
                searchPattern, searchPattern, searchPattern,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(manufacturers, pageable, total);
    }

    // НОВЫЙ МЕТОД: Подсчет всех производителей
    public long count() {
        String sql = "SELECT COUNT(*) FROM manufacturers";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Manufacturer findById(Long id) {
        String sql = "SELECT * FROM manufacturers WHERE id = ?";
        List<Manufacturer> manufacturers = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Manufacturer.class), id);
        return manufacturers.isEmpty() ? null : manufacturers.get(0);
    }

    public void save(Manufacturer manufacturer) {
        String sql = "INSERT INTO manufacturers (name, country, contact_info) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, manufacturer.getName(), manufacturer.getCountry(), manufacturer.getContactInfo());
        // Получаем сгенерированный ID
        Long id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
        manufacturer.setId(id);
    }

    public void update(Manufacturer manufacturer) {
        String sql = "UPDATE manufacturers SET name = ?, country = ?, contact_info = ? WHERE id = ?";
        jdbcTemplate.update(sql, manufacturer.getName(), manufacturer.getCountry(), manufacturer.getContactInfo(), manufacturer.getId());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM manufacturers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}