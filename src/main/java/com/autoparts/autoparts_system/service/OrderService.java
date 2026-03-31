package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.OrderItem;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.repository.OrderItemRepository;
import com.autoparts.autoparts_system.repository.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Transactional
    public SalesOrder createOrder(Long userId) {
        Map<Long, Integer> cart = cartService.getCart(userId);  // <-- ДОБАВЛЕН userId

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }

        // Проверяем наличие товаров
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
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
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());

        // Вычисляем общую сумму
        double total = cartService.getTotalPrice(userId, productService);  // <-- ДОБАВЛЕН userId
        order.setTotal(BigDecimal.valueOf(total));

        salesOrderRepository.save(order);

        // Сохраняем позиции заказа и списываем товары
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
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

        // Очищаем корзину
        cartService.clearCart(userId);  // <-- ДОБАВЛЕН userId

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