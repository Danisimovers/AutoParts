package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Supplier;
import com.autoparts.autoparts_system.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    public Supplier createSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название поставщика обязательно");
        }
        supplierRepository.save(supplier);
        return supplier;
    }

    public Supplier updateSupplier(Long id, Supplier supplier) {
        Supplier existing = supplierRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Поставщик не найден");
        }
        supplier.setId(id);
        supplierRepository.update(supplier);
        return supplier;
    }

    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
}