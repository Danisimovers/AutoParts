package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.request.CreateVehicleRequest;
import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.dto.response.VehicleDTO;
import com.autoparts.autoparts_system.model.Vehicle;
import com.autoparts.autoparts_system.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        List<VehicleDTO> dtos = vehicles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Автомобили успешно загружены", dtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getVehicleById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        if (vehicle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Автомобиль не найден"));
        }
        return ResponseEntity.ok(ApiResponse.success("Автомобиль успешно загружен", convertToDTO(vehicle)));
    }

    @GetMapping("/make/{make}")
    public ResponseEntity<ApiResponse> getVehiclesByMake(@PathVariable String make) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByMake(make);
        List<VehicleDTO> dtos = vehicles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Автомобили успешно загружены", dtos));
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createVehicle(@RequestBody CreateVehicleRequest request) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setMake(request.getMake());
            vehicle.setModel(request.getModel());
            vehicle.setGeneration(request.getGeneration());
            vehicle.setYearFrom(request.getYearFrom());
            vehicle.setYearTo(request.getYearTo());
            vehicle.setEngine(request.getEngine());

            Vehicle created = vehicleService.createVehicle(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Автомобиль успешно создан", convertToDTO(created)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateVehicle(@PathVariable Long id, @RequestBody CreateVehicleRequest request) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setMake(request.getMake());
            vehicle.setModel(request.getModel());
            vehicle.setGeneration(request.getGeneration());
            vehicle.setYearFrom(request.getYearFrom());
            vehicle.setYearTo(request.getYearTo());
            vehicle.setEngine(request.getEngine());

            Vehicle updated = vehicleService.updateVehicle(id, vehicle);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль успешно обновлен", convertToDTO(updated)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль успешно удален", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private VehicleDTO convertToDTO(Vehicle vehicle) {
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getGeneration(),
                vehicle.getYearFrom(),
                vehicle.getYearTo(),
                vehicle.getEngine()
        );
    }
}