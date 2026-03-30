package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.service.CartService;
import com.autoparts.autoparts_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public Map<String, Object> getCart() {
        Map<String, Object> response = new HashMap<>();
        response.put("items", cartService.getCart());
        response.put("totalItems", cartService.getTotalItems());
        response.put("totalPrice", cartService.getTotalPrice(productService));
        return response;
    }

    @PostMapping("/add")
    public Map<String, Object> addToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity) {
        cartService.addToCart(productId, quantity);
        return getCart();
    }

    @PutMapping("/update")
    public Map<String, Object> updateQuantity(@RequestParam Long productId, @RequestParam int quantity) {
        cartService.updateQuantity(productId, quantity);
        return getCart();
    }

    @DeleteMapping("/remove/{productId}")
    public Map<String, Object> removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        return getCart();
    }

    @DeleteMapping("/clear")
    public Map<String, Object> clearCart() {
        cartService.clearCart();
        return getCart();
    }
}