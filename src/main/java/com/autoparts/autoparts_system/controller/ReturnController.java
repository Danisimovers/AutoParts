package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.Return;
import com.autoparts.autoparts_system.security.JwtService;
import com.autoparts.autoparts_system.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/returns")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private JwtService jwtService;

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    // Получить все заявки (для админа/менеджера)
    @GetMapping
    public ResponseEntity<ApiResponse> getAllReturns() {
        List<Return> returns = returnService.getAllReturns();
        return ResponseEntity.ok(ApiResponse.success("Заявки на возврат загружены", returns));
    }

    // Получить свои заявки (для пользователя)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyReturns(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        List<Return> returns = returnService.getUserReturns(userId);
        return ResponseEntity.ok(ApiResponse.success("Ваши заявки на возврат", returns));
    }

    // Создать заявку на возврат
    @PostMapping
    public ResponseEntity<ApiResponse> createReturn(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            Long orderItemId = Long.parseLong(request.get("orderItemId"));
            String reason = request.get("reason");

            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Укажите причину возврата"));
            }

            returnService.createReturn(orderItemId, userId, reason);
            return ResponseEntity.ok(ApiResponse.success("Заявка на возврат отправлена", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    // Одобрить возврат (для админа/менеджера)
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approveReturn(@PathVariable Long id) {
        try {
            returnService.approveReturn(id);
            return ResponseEntity.ok(ApiResponse.success("Заявка на возврат одобрена", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Отклонить возврат (для админа/менеджера)
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> rejectReturn(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Укажите причину отклонения"));
            }
            returnService.rejectReturn(id, reason);
            return ResponseEntity.ok(ApiResponse.success("Заявка на возврат отклонена", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserReturns(@PathVariable Long userId) {
        try {
            List<Return> returns = returnService.getUserReturns(userId);
            return ResponseEntity.ok(ApiResponse.success("Заявки на возврат загружены", returns));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки: " + e.getMessage()));
        }
    }
}