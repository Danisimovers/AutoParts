package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.Map;

@Service
@SessionScope
public class CartService {

    // cart: key = productId, value = quantity
    private Map<Long, Integer> cart = new HashMap<>();

    public void addToCart(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
    }

    public void removeFromCart(Long productId) {
        cart.remove(productId);
    }

    public Map<Long, Integer> getCart() {
        return new HashMap<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }

    public int getTotalItems() {
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getTotalPrice(ProductService productService) {
        return cart.entrySet().stream()
                .mapToDouble(entry -> {
                    Product product = productService.getProductById(entry.getKey());
                    return product.getPrice().doubleValue() * entry.getValue();
                })
                .sum();
    }
}