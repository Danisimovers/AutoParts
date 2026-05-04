package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.repository.NotificationRepository;
import com.autoparts.autoparts_system.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtService jwtService;

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getNotifications(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        int unreadCount = notificationRepository.countUnread(userId);

        return ResponseEntity.ok(ApiResponse.success("Уведомления загружены",
                java.util.Map.of("notifications", notifications, "unreadCount", unreadCount)));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse> getUnreadCount(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        int count = notificationRepository.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.success("OK", java.util.Map.of("count", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long id) {
        notificationRepository.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Уведомление отмечено прочитанным", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllAsRead(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        notificationRepository.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("Все уведомления отмечены прочитанными", null));
    }
}