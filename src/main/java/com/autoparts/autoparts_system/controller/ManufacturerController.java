package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.Manufacturer;
import com.autoparts.autoparts_system.service.ManufacturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manufacturers")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManufacturerController {

    @Autowired
    private ManufacturerService manufacturerService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllManufacturers() {
        List<Manufacturer> manufacturers = manufacturerService.getAllManufacturers();
        return ResponseEntity.ok(ApiResponse.success("Производители загружены", manufacturers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getManufacturerById(@PathVariable Long id) {
        Manufacturer manufacturer = manufacturerService.getManufacturerById(id);
        if (manufacturer == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Производитель не найден"));
        }
        return ResponseEntity.ok(ApiResponse.success("Производитель загружен", manufacturer));
    }
}