package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.UserVehicle;
import com.autoparts.autoparts_system.model.Vehicle;
import com.autoparts.autoparts_system.repository.UserVehicleRepository;
import com.autoparts.autoparts_system.repository.VehicleRepository;
import com.autoparts.autoparts_system.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/user-vehicles")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserVehicleController {

    @Autowired
    private UserVehicleRepository userVehicleRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private JwtService jwtService;

    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtService.extractUserId(token);
        }
        throw new RuntimeException("Не удалось определить ID пользователя");
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserVehicles(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<UserVehicle> vehicles = userVehicleRepository.findByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("Автомобили загружены", vehicles));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addUserVehicle(@RequestBody UserVehicle userVehicle, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);

            // Проверяем, существует ли выбранное авто
            Vehicle vehicle = vehicleRepository.findById(userVehicle.getVehicleId());
            if (vehicle == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Модель автомобиля не найдена"));
            }

            userVehicle.setUserId(userId);
            userVehicleRepository.save(userVehicle);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль добавлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка добавления: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUserVehicle(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            UserVehicle vehicle = userVehicleRepository.findById(id);
            if (vehicle == null || !vehicle.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Автомобиль не найден"));
            }
            userVehicleRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Автомобиль удален", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка удаления: " + e.getMessage()));
        }
    }
}