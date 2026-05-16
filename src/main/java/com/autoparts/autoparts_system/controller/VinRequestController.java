package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.model.VinMessage;
import com.autoparts.autoparts_system.model.VinRequest;
import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.repository.NotificationRepository;
import com.autoparts.autoparts_system.repository.VinMessageRepository;
import com.autoparts.autoparts_system.repository.VinRequestRepository;
import com.autoparts.autoparts_system.repository.UserRepository;
import com.autoparts.autoparts_system.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    @Autowired
    private VinMessageRepository vinMessageRepository;

    @Autowired
    private UserRepository userRepository;

    // Регулярное выражение для валидации VIN
    // 17 символов, только латиница и цифры, без букв I, O, Q
    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private boolean isValidVin(String vin) {
        if (vin == null) return false;
        vin = vin.toUpperCase().trim();
        return VIN_PATTERN.matcher(vin).matches();
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createVinRequest(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            String vin = (String) request.get("vin");
            String description = (String) request.get("description");

            // Исправлено: правильная обработка userVehicleId (может быть String или Number)
            Long userVehicleId = null;
            Object vehicleIdObj = request.get("userVehicleId");
            if (vehicleIdObj != null) {
                if (vehicleIdObj instanceof Number) {
                    userVehicleId = ((Number) vehicleIdObj).longValue();
                } else if (vehicleIdObj instanceof String) {
                    String vehicleIdStr = (String) vehicleIdObj;
                    if (!vehicleIdStr.isEmpty()) {
                        userVehicleId = Long.parseLong(vehicleIdStr);
                    }
                }
            }
            // Проверка VIN
            if (vin == null || vin.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("VIN номер обязателен"));
            }
            if (!isValidVin(vin)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("VIN номер должен содержать 17 символов (латиница и цифры, без букв I, O, Q)"));
            }

            // Проверка описания
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Опишите, какие запчасти нужны"));
            }

            VinRequest vinRequest = new VinRequest();
            vinRequest.setUserId(userId);
            vinRequest.setUserVehicleId(userVehicleId);
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getVinRequestById(@PathVariable Long id) {
        VinRequest vinRequest = vinRequestRepository.findById(id);
        if (vinRequest == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Заявка не найдена"));
        }
        return ResponseEntity.ok(ApiResponse.success("Заявка загружена", vinRequest));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse> getMessagesByVinRequest(@PathVariable Long id) {
        List<VinMessage> messages = vinMessageRepository.findByVinRequestId(id);
        return ResponseEntity.ok(ApiResponse.success("Сообщения загружены", messages));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse> sendUserMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long senderId = jwtService.extractUserId(token);
            String message = request.get("message");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Сообщение не может быть пустым"));
            }

            // Получаем информацию об отправителе (пользователе)
            User sender = userRepository.findById(senderId).orElse(null);
            String senderRole = sender != null ? sender.getRole().name() : "UNKNOWN";
            String senderLogin = sender != null ? sender.getLogin() : "Unknown";

            VinMessage vinMessage = new VinMessage();
            vinMessage.setVinRequestId(id);
            vinMessage.setSenderId(senderId);
            vinMessage.setSenderRole(senderRole);
            vinMessage.setSenderLogin(senderLogin);
            vinMessage.setMessage(message);
            vinMessageRepository.save(vinMessage);

            // Уведомление менеджерам
            String sql = "SELECT id FROM users WHERE role = 'MANAGER' OR role = 'ADMIN'";
            List<Long> managerIds = jdbcTemplate.queryForList(sql, Long.class);

            for (Long managerId : managerIds) {
                Notification notification = new Notification();
                notification.setUserId(managerId);
                notification.setType("VIN_RESPONSE");
                notification.setTitle("Новое сообщение в VIN-заявке");
                notification.setMessage("Пользователь ответил в заявке #" + id);
                notification.setLink("/manager/vin-requests/" + id);
                notification.setRead(false);
                notificationRepository.save(notification);
            }

            return ResponseEntity.ok(ApiResponse.success("Сообщение отправлено", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка отправки: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserVinRequests(@PathVariable Long userId) {
        try {
            List<VinRequest> requests = vinRequestRepository.findByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("Заявки загружены", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки: " + e.getMessage()));
        }
    }
}