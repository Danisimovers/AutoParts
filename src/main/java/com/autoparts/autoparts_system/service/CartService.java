package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Cart;
import com.autoparts.autoparts_system.model.CartItem;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.repository.CartItemRepository;
import com.autoparts.autoparts_system.repository.CartRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Data
    public static class CartItemView {
        private String tempId;
        private Long productId;
        private String productName;
        private String factoryNumber;
        private String producer;
        private String supplierName;
        private BigDecimal price;
        private int quantity;
        private String type;
    }

    private Long getOrCreateCartId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cartRepository.createCartForUser(userId);
            cart = cartRepository.findByUserId(userId);
        }
        return cart.getId();
    }

    // НОВЫЙ МЕТОД: обновляем время последнего изменения корзины
    private void touchCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cartRepository.updateUpdatedAt(cart.getId());
        }
    }

    @Transactional
    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Количество должно быть больше 0");

        Long cartId = getOrCreateCartId(userId);
        String tempId = "PROD_" + productId;

        CartItem existing = cartItemRepository.findByCartIdAndTempId(cartId, tempId);
        if (existing != null) {
            cartItemRepository.updateQuantity(cartId, tempId, existing.getQuantity() + quantity);
        } else {
            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new IllegalArgumentException("Товар не найден");
            }

            CartItem item = new CartItem();
            item.setCartId(cartId);
            item.setTempId(tempId);
            item.setProductId(productId);
            item.setItemType("REGULAR");
            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        touchCart(userId);  // ← ОБНОВЛЯЕМ ВРЕМЯ
    }

    @Transactional
    public void addExternalToCart(Long userId, String productName, String factoryNumber,
                                  String producer, String supplierName, BigDecimal price, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Количество должно быть больше 0");

        Long cartId = getOrCreateCartId(userId);
        String tempId = "EXT_" + factoryNumber;

        CartItem existing = cartItemRepository.findByCartIdAndTempId(cartId, tempId);
        if (existing != null) {
            cartItemRepository.updateQuantity(cartId, tempId, existing.getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setCartId(cartId);
            item.setTempId(tempId);
            item.setItemType("EXTERNAL");
            item.setProductName(productName);
            item.setFactoryNumber(factoryNumber);
            item.setProducer(producer);
            item.setSupplierName(supplierName);
            item.setPrice(price);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        touchCart(userId);  // ← ОБНОВЛЯЕМ ВРЕМЯ
    }

    @Transactional
    public void updateQuantity(Long userId, String tempId, int quantity) {
        Long cartId = getOrCreateCartId(userId);
        if (quantity <= 0) {
            cartItemRepository.deleteByCartIdAndTempId(cartId, tempId);
        } else {
            cartItemRepository.updateQuantity(cartId, tempId, quantity);
        }
        touchCart(userId);  // ← ОБНОВЛЯЕМ ВРЕМЯ
    }

    @Transactional
    public void removeFromCart(Long userId, String tempId) {
        Long cartId = getOrCreateCartId(userId);
        cartItemRepository.deleteByCartIdAndTempId(cartId, tempId);
        touchCart(userId);  // ← ОБНОВЛЯЕМ ВРЕМЯ
    }

    public Map<String, CartItemView> getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) return new HashMap<>();

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        Map<String, CartItemView> result = new HashMap<>();

        for (CartItem item : items) {
            CartItemView view = new CartItemView();
            view.setTempId(item.getTempId());
            view.setProductId(item.getProductId());
            view.setProductName(item.getProductName());
            view.setFactoryNumber(item.getFactoryNumber());
            view.setProducer(item.getProducer());
            view.setSupplierName(item.getSupplierName());
            view.setPrice(item.getPrice());
            view.setQuantity(item.getQuantity());
            view.setType(item.getItemType());
            result.put(item.getTempId(), view);
        }
        return result;
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cartItemRepository.deleteAllByCartId(cart.getId());
            touchCart(userId);  // ← ОБНОВЛЯЕМ ВРЕМЯ (корзина очищена, но не удалена)
        }
    }

    public Map<Long, Integer> getCartLegacy(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) return new HashMap<>();

        return cartItemRepository.findByCartId(cart.getId()).stream()
                .filter(item -> "REGULAR".equals(item.getItemType()) && item.getProductId() != null)
                .collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));
    }

    public void clearCartLegacy(Long userId) {
        clearCart(userId);
    }
}