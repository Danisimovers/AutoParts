package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.AddToCartRequest;
import com.autoparts.autoparts_system.dto.request.UpdateCartRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.CartItemDTO;
import com.autoparts.autoparts_system.dto.response.CartResponseDTO;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.service.CartService;
import com.autoparts.autoparts_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    private Long getCurrentUserId() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof Map) {
            Object userId = ((Map<?, ?>) details).get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            }
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
        }
        throw new RuntimeException("Не удалось определить ID пользователя");
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCart() {
        Long userId = getCurrentUserId();
        CartResponseDTO response = buildCartResponse(userId);
        return ResponseEntity.ok(ApiResponse.success("Корзина загружена", response));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(@RequestBody AddToCartRequest request) {
        try {
            Long userId = getCurrentUserId();
            cartService.addToCart(userId, request.getProductId(), request.getQuantity());
            CartResponseDTO response = buildCartResponse(userId);
            return ResponseEntity.ok(ApiResponse.success("Товар добавлен в корзину", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/add-external")
    public ResponseEntity<ApiResponse> addExternalToCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = getCurrentUserId();
            String productName = (String) request.get("productName");
            String factoryNumber = (String) request.get("factoryNumber");
            String producer = (String) request.get("producer");
            String supplierName = (String) request.get("supplierName");
            BigDecimal price = new BigDecimal(request.get("price").toString());
            int quantity = (Integer) request.get("quantity");

            cartService.addExternalToCart(userId, productName, factoryNumber, producer, supplierName, price, quantity);
            CartResponseDTO response = buildCartResponse(userId);
            return ResponseEntity.ok(ApiResponse.success("Товар поставщика добавлен в корзину", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateQuantity(@RequestBody Map<String, Object> request) {
        try {
            Long userId = getCurrentUserId();
            String itemId = (String) request.get("itemId");
            int quantity = (Integer) request.get("quantity");
            cartService.updateQuantity(userId, itemId, quantity);
            CartResponseDTO response = buildCartResponse(userId);
            return ResponseEntity.ok(ApiResponse.success("Корзина обновлена", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable String itemId) {
        Long userId = getCurrentUserId();
        cartService.removeFromCart(userId, itemId);
        CartResponseDTO response = buildCartResponse(userId);
        return ResponseEntity.ok(ApiResponse.success("Товар удален из корзины", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
        CartResponseDTO response = buildCartResponse(userId);
        return ResponseEntity.ok(ApiResponse.success("Корзина очищена", response));
    }

    private CartResponseDTO buildCartResponse(Long userId) {
        Map<String, CartService.CartItem> cart = cartService.getCart(userId);
        List<CartItemDTO> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItems = 0;

        for (Map.Entry<String, CartService.CartItem> entry : cart.entrySet()) {
            CartService.CartItem cartItem = entry.getValue();
            String itemId = entry.getKey();
            int quantity = cartItem.getQuantity();

            if ("REGULAR".equals(cartItem.getType())) {
                try {
                    Product product = productService.getProductById(cartItem.getProductId());
                    if (product != null) {
                        BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                        totalPrice = totalPrice.add(itemTotal);
                        totalItems += quantity;

                        CartItemDTO item = new CartItemDTO();
                        item.setItemId(itemId);
                        item.setProductId(cartItem.getProductId());
                        item.setSku(product.getSku());
                        item.setName(product.getName());
                        item.setPrice(product.getPrice());
                        item.setQuantity(quantity);
                        item.setTotal(itemTotal);
                        items.add(item);
                    }
                } catch (Exception e) {
                    // Товар не найден
                }
            } else if ("EXTERNAL".equals(cartItem.getType())) {
                BigDecimal itemTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(quantity));
                totalPrice = totalPrice.add(itemTotal);
                totalItems += quantity;

                CartItemDTO item = new CartItemDTO();
                item.setItemId(itemId);
                item.setProductId(null);
                item.setName(cartItem.getProductName() + (cartItem.getType().equals("EXTERNAL") ? " (под заказ)" : ""));
                item.setPrice(cartItem.getPrice());
                item.setQuantity(quantity);
                item.setTotal(itemTotal);
                items.add(item);
            }
        }

        CartResponseDTO response = new CartResponseDTO();
        response.setItems(items);
        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);
        return response;
    }
}