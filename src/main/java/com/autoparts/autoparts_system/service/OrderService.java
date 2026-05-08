package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.ExternalRequest;
import com.autoparts.autoparts_system.model.OrderItem;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.repository.ExternalRequestRepository;
import com.autoparts.autoparts_system.repository.OrderItemRepository;
import com.autoparts.autoparts_system.repository.SalesOrderRepository;
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
        System.out.println("=== CREATE ORDER ===");
        System.out.println("UserId: " + userId);

        // Получаем полную корзину (включая товары поставщиков)
        Map<String, CartService.CartItem> cart = cartService.getCart(userId);
        System.out.println("Cart size: " + cart.size());

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        // Отделяем обычные товары от товаров поставщиков
        Map<Long, Integer> regularItems = new HashMap<>();
        Map<String, CartService.CartItem> externalItems = new HashMap<>();

        for (Map.Entry<String, CartService.CartItem> entry : cart.entrySet()) {
            CartService.CartItem item = entry.getValue();
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

        // Создаем заказ
        SalesOrder order = new SalesOrder();
        order.setUserId(userId);

        // Если есть товары поставщиков, ставим статус PENDING_SUPPLIER
        if (!externalItems.isEmpty()) {
            order.setStatus("PENDING_SUPPLIER");
        } else {
            order.setStatus("CREATED");
        }
        order.setCreatedAt(LocalDateTime.now());

        // Вычисляем общую сумму (обычные товары + товары поставщиков)
        double total = 0;

        // Обычные товары
        for (Map.Entry<Long, Integer> entry : regularItems.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            total += product.getPrice().doubleValue() * entry.getValue();
        }

        // Товары поставщиков
        for (CartService.CartItem item : externalItems.values()) {
            total += item.getPrice().doubleValue() * item.getQuantity();
        }

        order.setTotal(BigDecimal.valueOf(total));
        salesOrderRepository.save(order);
        System.out.println("Order created with id: " + order.getId());

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

            // Списываем товар со склада
            stockService.removeStock(productId, quantity);
        }

        // Сохраняем позиции товаров поставщиков и создаем external_requests с order_id и supplier_id
        for (Map.Entry<String, CartService.CartItem> entry : externalItems.entrySet()) {
            CartService.CartItem item = entry.getValue();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(null);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItemRepository.save(orderItem);

            System.out.println("External OrderItem saved: " + item.getProductName() + ", quantity=" + item.getQuantity());

            // Получаем supplier_id по имени поставщика
            String supplierName = item.getSupplierName();
            String findSupplierSql = "SELECT id FROM suppliers WHERE name = ?";
            Long supplierId = null;
            try {
                supplierId = jdbcTemplate.queryForObject(findSupplierSql, Long.class, supplierName);
            } catch (Exception e) {
                System.out.println("Supplier not found, creating new: " + supplierName);
                String insertSupplierSql = "INSERT INTO suppliers (name) VALUES (?)";
                jdbcTemplate.update(insertSupplierSql, supplierName);
                supplierId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            }

            // Создаем запись в external_requests с привязкой к заказу
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
            System.out.println("ExternalRequest created for: " + item.getProductName() + " with orderId=" + order.getId() + ", supplierId=" + supplierId);
        }

        // Очищаем корзину
        cartService.clearCart(userId);
        System.out.println("Cart cleared for userId: " + userId);
        System.out.println("=== ORDER CREATED SUCCESSFULLY ===");

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