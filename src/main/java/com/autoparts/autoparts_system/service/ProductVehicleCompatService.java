package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.ProductVehicleCompat;
import com.autoparts.autoparts_system.repository.ProductVehicleCompatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductVehicleCompatService {

    @Autowired
    private ProductVehicleCompatRepository compatRepository;

    public List<ProductVehicleCompat> getAllCompat() {
        return compatRepository.findAll();
    }

    public List<ProductVehicleCompat> getCompatByProductId(Long productId) {
        return compatRepository.findByProductId(productId);
    }

    public List<ProductVehicleCompat> getCompatByVehicleId(Long vehicleId) {
        return compatRepository.findByVehicleId(vehicleId);
    }

    public void addCompatibility(Long productId, Long vehicleId, String details) {
        ProductVehicleCompat compat = new ProductVehicleCompat();
        compat.setProductId(productId);
        compat.setVehicleId(vehicleId);
        compat.setDetails(details);
        compatRepository.save(compat);
    }

    public void removeCompatibility(Long id) {
        compatRepository.deleteById(id);
    }

    public void removeAllByProductId(Long productId) {
        compatRepository.deleteByProductId(productId);
    }
}