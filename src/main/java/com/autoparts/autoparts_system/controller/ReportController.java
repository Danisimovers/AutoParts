package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse> getRevenue(@RequestParam(required = false) String period) {
        return ResponseEntity.ok(ApiResponse.success("Выручка загружена", reportService.getRevenue(period)));
    }

    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse> getTopProducts() {
        return ResponseEntity.ok(ApiResponse.success("Топ товаров загружен", reportService.getTopProducts()));
    }

    @GetMapping("/orders-by-status")
    public ResponseEntity<ApiResponse> getOrdersByStatus() {
        return ResponseEntity.ok(ApiResponse.success("Статистика по статусам", reportService.getOrdersByStatus()));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse> getSummary() {
        return ResponseEntity.ok(ApiResponse.success("Общая статистика", reportService.getSummary()));
    }
}