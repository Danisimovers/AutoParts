package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.model.VinRequest;
import com.autoparts.autoparts_system.repository.NotificationRepository;
import com.autoparts.autoparts_system.repository.VinRequestRepository;
import com.autoparts.autoparts_system.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vin-requests")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class VinRequestController {

    @Autowired
    private VinRequestRepository vinRequestRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> createVinRequest(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            String vin = request.get("vin");
            String description = request.get("description");

            if (vin == null || vin.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("VIN номер обязателен"));
            }

            VinRequest vinRequest = new VinRequest();
            vinRequest.setUserId(userId);
            vinRequest.setVin(vin.toUpperCase());
            vinRequest.setDescription(description);
            vinRequest.setStatus("PENDING");
            vinRequest.setCreatedAt(LocalDateTime.now());

            vinRequestRepository.save(vinRequest);




            String sql = "SELECT id FROM users WHERE role = 'MANAGER' OR role = 'ADMIN'";
            List<Long> managerIds = jdbcTemplate.queryForList(sql, Long.class);

            for (Long managerId : managerIds) {
                Notification notification = new Notification();
                notification.setUserId(managerId);
                notification.setType("VIN_REQUEST");
                notification.setTitle("Новая VIN-заявка");
                notification.setMessage("Пользователь отправил запрос на подбор запчастей по VIN: " + vin);
                notification.setLink("/manager/vin-requests");
                notification.setRead(false);
                notificationRepository.save(notification);
            }

            return ResponseEntity.ok(ApiResponse.success("Заявка отправлена. Менеджер свяжется с вами.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка отправки заявки: " + e.getMessage()));
        }
    }
}