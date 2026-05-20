package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.OrderDTO;
import com.autoparts.autoparts_system.dto.response.OrderItemDTO;
import com.autoparts.autoparts_system.model.OrderItem;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.security.JwtService;
import com.autoparts.autoparts_system.service.OrderService;
import com.autoparts.autoparts_system.service.ProductService;
import com.autoparts.autoparts_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    private Long getCurrentUserId(HttpServletRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                Object userId = ((Map<?, ?>) details).get("userId");
                if (userId instanceof Long) return (Long) userId;
                if (userId instanceof Integer) return ((Integer) userId).longValue();
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            if (userId != null) return userId;
        }

        throw new RuntimeException("Не удалось определить ID пользователя");
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(HttpServletRequest httpRequest) {
        try {
            Long userId = getCurrentUserId(httpRequest);
            SalesOrder order = orderService.createOrder(userId);
            OrderDTO dto = convertToDTO(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Заказ успешно создан", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserOrders(@PathVariable Long userId) {
        List<SalesOrder> orders = orderService.getUserOrders(userId);
        List<OrderDTO> dtos = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Заказы успешно загружены", dtos));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        SalesOrder order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Заказ не найден"));
        }
        return ResponseEntity.ok(ApiResponse.success("Заказ успешно загружен", convertToDTO(order)));
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse> getOrderItems(@PathVariable Long orderId) {
        List<OrderItem> items = orderService.getOrderItems(orderId);
        List<OrderItemDTO> dtos = items.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Позиции заказа успешно загружены", dtos));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(ApiResponse.success("Статус заказа обновлен", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private OrderDTO convertToDTO(SalesOrder order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());

        try {
            dto.setUserLogin(userService.getUserById(order.getUserId()).getLogin());
        } catch (Exception e) {
            dto.setUserLogin("Неизвестно");
        }

        List<OrderItem> items = orderService.getOrderItems(order.getId());
        List<OrderItemDTO> itemDTOs = items.stream()
                .map(this::convertToItemDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        return dto;
    }

    private OrderItemDTO convertToItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        try {
            Product product = productService.getProductById(item.getProductId());
            if (product != null) {
                dto.setProductName(product.getName());
                dto.setProductSku(product.getSku());
            }
        } catch (Exception e) {
            dto.setProductName("Неизвестно");
            dto.setProductSku("Неизвестно");
        }

        return dto;
    }
}