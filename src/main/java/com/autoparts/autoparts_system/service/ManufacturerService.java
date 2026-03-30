package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Manufacturer;
import com.autoparts.autoparts_system.repository.ManufacturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ManufacturerService {

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    public List<Manufacturer> getAllManufacturers() {
        return manufacturerRepository.findAll();
    }

    public Manufacturer getManufacturerById(Long id) {
        return manufacturerRepository.findById(id);
    }

    public Manufacturer createManufacturer(Manufacturer manufacturer) {
        if (manufacturer.getName() == null || manufacturer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название производителя обязательно");
        }
        manufacturerRepository.save(manufacturer);
        return manufacturer;
    }

    public Manufacturer updateManufacturer(Long id, Manufacturer manufacturer) {
        Manufacturer existing = manufacturerRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Производитель не найден");
        }
        manufacturer.setId(id);
        manufacturerRepository.update(manufacturer);
        return manufacturer;
    }

    public void deleteManufacturer(Long id) {
        manufacturerRepository.deleteById(id);
    }
}