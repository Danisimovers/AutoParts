package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.model.VinRequest;
import com.autoparts.autoparts_system.repository.SalesOrderRepository;
import com.autoparts.autoparts_system.repository.VinRequestRepository;
import com.autoparts.autoparts_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManagerController {

    @Autowired
    private VinRequestRepository vinRequestRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private OrderService orderService;



    @GetMapping("/vin-requests")
    public ResponseEntity<ApiResponse> getAllVinRequests() {
        List<VinRequest> requests = vinRequestRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Заявки загружены", requests));
    }

    @PutMapping("/vin-requests/{id}/status")
    public ResponseEntity<ApiResponse> updateVinRequestStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            vinRequestRepository.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Статус заявки обновлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }



    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getAllOrders() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Заказы загружены", orders));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Статус заказа обновлен", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}