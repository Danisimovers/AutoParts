package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.PurchaseOrder;
import com.autoparts.autoparts_system.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private StockService stockService;

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> getPurchaseOrdersBySupplierId(Long supplierId) {
        return purchaseOrderRepository.findBySupplierId(supplierId);
    }

    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        order.setDate(LocalDate.now());
        order.setStatus("PENDING");
        purchaseOrderRepository.save(order);
        return order;
    }

    @Transactional
    public PurchaseOrder receiveOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Заказ не найден");
        }

        order.setStatus("RECEIVED");
        purchaseOrderRepository.updateStatus(orderId, "RECEIVED");

        // Здесь нужно добавить логику пополнения склада
        // Для упрощения оставляем заглушку

        return order;
    }

    public void updateOrderStatus(Long orderId, String status) {
        purchaseOrderRepository.updateStatus(orderId, status);
    }

    public void deletePurchaseOrder(Long id) {
        purchaseOrderRepository.deleteById(id);
    }
}