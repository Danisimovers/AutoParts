package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.ExternalRequest;
import com.autoparts.autoparts_system.model.OrderItem;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.repository.ExternalRequestRepository;
import com.autoparts.autoparts_system.repository.OrderItemRepository;
import com.autoparts.autoparts_system.repository.SalesOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public SalesOrder createOrder(Long userId) {
        log.debug("Creating order for userId: {}", userId);

        Map<String, CartService.CartItemView> cart = cartService.getCart(userId);
        log.debug("Cart size: {}", cart.size());

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        Map<Long, Integer> regularItems = new HashMap<>();
        Map<String, CartService.CartItemView> externalItems = new HashMap<>();

        for (Map.Entry<String, CartService.CartItemView> entry : cart.entrySet()) {
            CartService.CartItemView item = entry.getValue();
            if ("REGULAR".equals(item.getType())) {
                regularItems.put(item.getProductId(), item.getQuantity());
            } else {
                externalItems.put(entry.getKey(), item);
            }
        }

        // Проверяем наличие обычных товаров на складе
        for (Map.Entry<Long, Integer> entry : regularItems.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            if (!stockService.checkAvailability(productId, quantity)) {
                Product product = productService.getProductById(productId);
                throw new IllegalArgumentException("Недостаточно товара: " + product.getName());
            }
        }

        SalesOrder order = new SalesOrder();
        order.setUserId(userId);

        if (!externalItems.isEmpty()) {
            order.setStatus("PENDING_SUPPLIER");
        } else {
            order.setStatus("CREATED");
        }
        order.setCreatedAt(LocalDateTime.now());

        double total = 0;

        for (Map.Entry<Long, Integer> entry : regularItems.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            total += product.getPrice().doubleValue() * entry.getValue();
        }

        for (CartService.CartItemView item : externalItems.values()) {
            total += item.getPrice().doubleValue() * item.getQuantity();
        }

        order.setTotal(BigDecimal.valueOf(total));
        salesOrderRepository.save(order);
        log.debug("Order created with id: {}", order.getId());

        // Сохраняем позиции обычных товаров и списываем их со склада
        for (Map.Entry<Long, Integer> entry : regularItems.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = productService.getProductById(productId);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(productId);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(product.getPrice());
            orderItemRepository.save(orderItem);

            stockService.removeStock(productId, quantity);
        }

        // Сохраняем позиции товаров поставщиков
        for (Map.Entry<String, CartService.CartItemView> entry : externalItems.entrySet()) {
            CartService.CartItemView item = entry.getValue();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(null);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItemRepository.save(orderItem);

            log.debug("External OrderItem saved: {}, quantity={}", item.getProductName(), item.getQuantity());

            String supplierName = item.getSupplierName();
            String findSupplierSql = "SELECT id FROM suppliers WHERE name = ?";
            Long supplierId = null;
            try {
                supplierId = jdbcTemplate.queryForObject(findSupplierSql, Long.class, supplierName);
            } catch (Exception e) {
                log.warn("Supplier not found, creating new: {}", supplierName);
                String insertSupplierSql = "INSERT INTO suppliers (name) VALUES (?)";
                jdbcTemplate.update(insertSupplierSql, supplierName);
                supplierId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            }

            ExternalRequest extRequest = new ExternalRequest();
            extRequest.setUserId(userId);
            extRequest.setOrderId(order.getId());
            extRequest.setProductName(item.getProductName());
            extRequest.setFactoryNumber(item.getFactoryNumber());
            extRequest.setProducer(item.getProducer());
            extRequest.setSupplierName(supplierName);
            extRequest.setSupplierId(supplierId);
            extRequest.setPrice(item.getPrice().doubleValue());
            extRequest.setStatus("PENDING");
            externalRequestRepository.save(extRequest);
            log.debug("ExternalRequest created for: {} with orderId={}, supplierId={}", item.getProductName(), order.getId(), supplierId);
        }

        cartService.clearCart(userId);
        log.debug("Cart cleared for userId: {}", userId);
        log.debug("Order created successfully for userId: {}", userId);

        return order;
    }

    public List<SalesOrder> getUserOrders(Long userId) {
        return salesOrderRepository.findByUserId(userId);
    }

    public SalesOrder getOrderById(Long orderId) {
        return salesOrderRepository.findById(orderId);
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public void updateOrderStatus(Long orderId, String status) {
        salesOrderRepository.updateStatus(orderId, status);
    }
}