package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:5173")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public List<Product> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long vehicleId) {
        return searchService.search(query, vehicleId);
    }
}