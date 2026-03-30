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

    @GetMapping
    public ResponseEntity<ApiResponse> getCart() {
        CartResponseDTO response = buildCartResponse();
        return ResponseEntity.ok(ApiResponse.success("Корзина загружена", response));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(@RequestBody AddToCartRequest request) {
        try {
            cartService.addToCart(request.getProductId(), request.getQuantity());
            CartResponseDTO response = buildCartResponse();
            return ResponseEntity.ok(ApiResponse.success("Товар добавлен в корзину", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateQuantity(@RequestBody UpdateCartRequest request) {
        try {
            cartService.updateQuantity(request.getProductId(), request.getQuantity());
            CartResponseDTO response = buildCartResponse();
            return ResponseEntity.ok(ApiResponse.success("Корзина обновлена", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<ApiResponse> removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        CartResponseDTO response = buildCartResponse();
        return ResponseEntity.ok(ApiResponse.success("Товар удален из корзины", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart() {
        cartService.clearCart();
        CartResponseDTO response = buildCartResponse();
        return ResponseEntity.ok(ApiResponse.success("Корзина очищена", response));
    }

    private CartResponseDTO buildCartResponse() {
        Map<Long, Integer> cart = cartService.getCart();
        List<CartItemDTO> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItems = 0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            try {
                Product product = productService.getProductById(productId);
                if (product != null) {
                    BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                    totalPrice = totalPrice.add(itemTotal);
                    totalItems += quantity;

                    CartItemDTO item = new CartItemDTO();
                    item.setProductId(productId);
                    item.setSku(product.getSku());
                    item.setName(product.getName());
                    item.setPrice(product.getPrice());
                    item.setQuantity(quantity);
                    item.setTotal(itemTotal);
                    items.add(item);
                }
            } catch (Exception e) {
                // Товар не найден, пропускаем
            }
        }

        CartResponseDTO response = new CartResponseDTO();
        response.setItems(items);
        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);

        return response;
    }
}