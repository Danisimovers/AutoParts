package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Inventory;
import com.autoparts.autoparts_system.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getAllStock() {
        return inventoryRepository.findAll();
    }

    public Inventory getStockByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public int getQuantity(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        return inventory != null ? inventory.getQuantity() : 0;
    }

    public void addStock(Long productId, int quantity, Long warehouseId) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        Inventory existing = inventoryRepository.findByProductId(productId);
        if (existing != null) {
            int newQuantity = existing.getQuantity() + quantity;
            inventoryRepository.updateQuantity(productId, newQuantity);
        } else {
            Inventory inventory = new Inventory();
            inventory.setProductId(productId);
            inventory.setQuantity(quantity);
            inventory.setWarehouseId(warehouseId);  // ← ИСПРАВЛЕНО: убрали String.valueOf()
            inventoryRepository.save(inventory);
        }
    }

    public void removeStock(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        Inventory existing = inventoryRepository.findByProductId(productId);
        if (existing == null) {
            throw new IllegalArgumentException("Товар не найден на складе");
        }

        int newQuantity = existing.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Недостаточно товара на складе");
        }

        inventoryRepository.updateQuantity(productId, newQuantity);
    }

    public boolean checkAvailability(Long productId, int requestedQuantity) {
        int available = getQuantity(productId);
        return available >= requestedQuantity;
    }
}