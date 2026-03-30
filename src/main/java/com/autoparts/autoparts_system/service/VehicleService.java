package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Vehicle;
import com.autoparts.autoparts_system.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> getVehiclesByMake(String make) {
        return vehicleRepository.findByMake(make);
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicle.getMake() == null || vehicle.getMake().trim().isEmpty()) {
            throw new IllegalArgumentException("Марка автомобиля обязательна");
        }
        if (vehicle.getModel() == null || vehicle.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Модель автомобиля обязательна");
        }
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    public Vehicle updateVehicle(Long id, Vehicle vehicle) {
        Vehicle existing = vehicleRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Автомобиль не найден");
        }
        vehicle.setId(id);
        vehicleRepository.update(vehicle);
        return vehicle;
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}