package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.ExternalRequest;
import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.repository.ExternalRequestRepository;
import com.autoparts.autoparts_system.repository.NotificationRepository;
import com.autoparts.autoparts_system.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external-requests")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ExternalRequestController {

    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> createExternalRequest(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);

            String productName = request.get("productName");
            String factoryNumber = request.get("factoryNumber");
            String producer = request.get("producer");
            String supplierName = request.get("supplierName");
            Double price = Double.parseDouble(request.get("price"));

            ExternalRequest extRequest = new ExternalRequest();
            extRequest.setUserId(userId);
            extRequest.setProductName(productName);
            extRequest.setFactoryNumber(factoryNumber);
            extRequest.setProducer(producer);
            extRequest.setSupplierName(supplierName);
            extRequest.setPrice(price);

            externalRequestRepository.save(extRequest);

            // Уведомление менеджерам
            String sql = "SELECT id FROM users WHERE role = 'MANAGER' OR role = 'ADMIN'";
            List<Long> managerIds = jdbcTemplate.queryForList(sql, Long.class);

            for (Long managerId : managerIds) {
                Notification notification = new Notification();
                notification.setUserId(managerId);
                notification.setType("EXTERNAL_REQUEST");
                notification.setTitle("Новый запрос товара у поставщика");
                notification.setMessage("Пользователь запросил товар: " + productName + " (" + factoryNumber + ")");
                notification.setLink("/manager/external-requests");
                notification.setRead(false);
                notificationRepository.save(notification);
            }

            return ResponseEntity.ok(ApiResponse.success("Запрос отправлен менеджеру", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }
}