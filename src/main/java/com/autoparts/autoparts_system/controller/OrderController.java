package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.model.OrderItem;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Создать заказ (userId пока заглушка, потом из сессии)
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestParam Long userId) {
        try {
            SalesOrder order = orderService.createOrder(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Получить заказы пользователя
    @GetMapping("/user/{userId}")
    public List<SalesOrder> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    // Получить заказ по ID
    @GetMapping("/{orderId}")
    public ResponseEntity<SalesOrder> getOrderById(@PathVariable Long orderId) {
        SalesOrder order = orderService.getOrderById(orderId);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }

    // Получить позиции заказа
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    // Обновить статус заказа
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok().build();
    }
}