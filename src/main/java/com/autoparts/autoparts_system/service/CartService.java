package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    // Хранилище корзин: ключ - userId, значение - Map<cartItemId, CartItem>
    private final Map<Long, Map<String, CartItem>> userCarts = new ConcurrentHashMap<>();

    // Внутренний класс для товаров в корзине (включая товары поставщиков)
    public static class CartItem {
        private String tempId;
        private Long productId;
        private String productName;
        private String factoryNumber;
        private String producer;
        private String supplierName;
        private BigDecimal price;
        private int quantity;
        private String type; // "REGULAR" или "EXTERNAL"

        // Геттеры и сеттеры
        public String getTempId() { return tempId; }
        public void setTempId(String tempId) { this.tempId = tempId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getFactoryNumber() { return factoryNumber; }
        public void setFactoryNumber(String factoryNumber) { this.factoryNumber = factoryNumber; }
        public String getProducer() { return producer; }
        public void setProducer(String producer) { this.producer = producer; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    // Добавление обычного товара
    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
        Map<String, CartItem> cart = userCarts.computeIfAbsent(userId, k -> new HashMap<>());
        String key = "PROD_" + productId;
        if (cart.containsKey(key)) {
            cart.get(key).setQuantity(cart.get(key).getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setTempId(key);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setType("REGULAR");
            cart.put(key, item);
        }
    }

    // Добавление товара поставщика
    public void addExternalToCart(Long userId, String productName, String factoryNumber,
                                  String producer, String supplierName, BigDecimal price, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }
        Map<String, CartItem> cart = userCarts.computeIfAbsent(userId, k -> new HashMap<>());
        String key = "EXT_" + factoryNumber;
        if (cart.containsKey(key)) {
            cart.get(key).setQuantity(cart.get(key).getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setTempId(key);
            item.setProductName(productName);
            item.setFactoryNumber(factoryNumber);
            item.setProducer(producer);
            item.setSupplierName(supplierName);
            item.setPrice(price);
            item.setQuantity(quantity);
            item.setType("EXTERNAL");
            cart.put(key, item);
        }
    }

    public void updateQuantity(Long userId, String itemId, int quantity) {
        Map<String, CartItem> cart = userCarts.get(userId);
        if (cart == null) return;
        if (quantity <= 0) {
            cart.remove(itemId);
        } else {
            CartItem item = cart.get(itemId);
            if (item != null) {
                item.setQuantity(quantity);
            }
        }
        if (cart.isEmpty()) {
            userCarts.remove(userId);
        }
    }

    public void removeFromCart(Long userId, String itemId) {
        Map<String, CartItem> cart = userCarts.get(userId);
        if (cart != null) {
            cart.remove(itemId);
            if (cart.isEmpty()) {
                userCarts.remove(userId);
            }
        }
    }

    public Map<String, CartItem> getCart(Long userId) {
        Map<String, CartItem> cart = userCarts.get(userId);
        return cart != null ? new HashMap<>(cart) : new HashMap<>();
    }

    public void clearCart(Long userId) {
        userCarts.remove(userId);
    }

    // Временные методы для совместимости с OrderService
    public Map<Long, Integer> getCartLegacy(Long userId) {
        Map<Long, Integer> legacyCart = new HashMap<>();
        Map<String, CartItem> cart = userCarts.get(userId);
        if (cart != null) {
            for (CartItem item : cart.values()) {
                if ("REGULAR".equals(item.getType()) && item.getProductId() != null) {
                    legacyCart.put(item.getProductId(), item.getQuantity());
                }
            }
        }
        return legacyCart;
    }

    public void clearCartLegacy(Long userId) {
        userCarts.remove(userId);
    }

    public double getTotalPriceLegacy(Long userId, ProductService productService) {
        Map<Long, Integer> cart = getCartLegacy(userId);
        return cart.entrySet().stream()
                .mapToDouble(entry -> {
                    Product product = productService.getProductById(entry.getKey());
                    return product.getPrice().doubleValue() * entry.getValue();
                })
                .sum();
    }
}