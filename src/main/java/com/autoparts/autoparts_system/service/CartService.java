package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Product;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    // Хранилище корзин: ключ - userId, значение - Map<productId, quantity>
    private final Map<Long, Map<Long, Integer>> userCarts = new ConcurrentHashMap<>();

    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
        Map<Long, Integer> cart = userCarts.computeIfAbsent(userId, k -> new HashMap<>());
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
    }

    public void updateQuantity(Long userId, Long productId, int quantity) {
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart == null) return;

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }

        if (cart.isEmpty()) {
            userCarts.remove(userId);
        }
    }

    public void removeFromCart(Long userId, Long productId) {
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart != null) {
            cart.remove(productId);
            if (cart.isEmpty()) {
                userCarts.remove(userId);
            }
        }
    }

    public Map<Long, Integer> getCart(Long userId) {
        Map<Long, Integer> cart = userCarts.get(userId);
        return cart != null ? new HashMap<>(cart) : new HashMap<>();
    }

    public void clearCart(Long userId) {
        userCarts.remove(userId);
    }

    public int getTotalItems(Long userId) {
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart == null) return 0;
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getTotalPrice(Long userId, ProductService productService) {
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart == null) return 0;

        return cart.entrySet().stream()
                .mapToDouble(entry -> {
                    Product product = productService.getProductById(entry.getKey());
                    return product.getPrice().doubleValue() * entry.getValue();
                })
                .sum();
    }
}