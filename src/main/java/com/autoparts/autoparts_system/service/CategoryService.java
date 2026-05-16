package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Category;
import com.autoparts.autoparts_system.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // НОВЫЙ МЕТОД: Пагинация для категорий
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    // НОВЫЙ МЕТОД: Поиск категорий с пагинацией
    public Page<Category> searchCategories(String search, Pageable pageable) {
        return categoryRepository.searchByName(search, pageable);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category createCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название категории обязательно");
        }
        categoryRepository.save(category);
        return category;
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Категория не найдена");
        }
        category.setId(id);
        categoryRepository.update(category);
        return category;
    }

    public void deleteCategory(Long id) {
        Category existing = categoryRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Категория не найдена");
        }
        categoryRepository.deleteById(id);
    }
}