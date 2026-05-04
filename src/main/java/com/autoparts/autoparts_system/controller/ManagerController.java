package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.model.SalesOrder;
import com.autoparts.autoparts_system.model.VinMessage;
import com.autoparts.autoparts_system.model.VinRequest;
import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.repository.*;
import com.autoparts.autoparts_system.security.JwtService;
import com.autoparts.autoparts_system.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManagerController {

    @Autowired
    private VinRequestRepository vinRequestRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VinMessageRepository vinMessageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/vin-requests")
    public ResponseEntity<ApiResponse> getAllVinRequests() {
        List<VinRequest> requests = vinRequestRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Заявки загружены", requests));
    }

    @PutMapping("/vin-requests/{id}/status")
    public ResponseEntity<ApiResponse> updateVinRequestStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            vinRequestRepository.updateStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Статус заявки обновлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getAllOrders() {
        List<SalesOrder> orders = salesOrderRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Заказы загружены", orders));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("Статус заказа обновлен", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/vin-requests/{id}/messages")
    public ResponseEntity<ApiResponse> getMessages(@PathVariable Long id) {
        List<VinMessage> messages = vinMessageRepository.findByVinRequestId(id);
        return ResponseEntity.ok(ApiResponse.success("Сообщения загружены", messages));
    }

    @PostMapping("/vin-requests/{id}/messages")
    public ResponseEntity<ApiResponse> sendMessage(
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

            // Получаем информацию об отправителе (менеджере/админе)
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

            VinRequest vinRequest = vinRequestRepository.findById(id);
            if (vinRequest != null) {
                Notification notification = new Notification();
                notification.setUserId(vinRequest.getUserId());
                notification.setType("VIN_RESPONSE");
                notification.setTitle("Ответ по вашей VIN-заявке");
                notification.setMessage("Менеджер ответил на ваш запрос по VIN: " + vinRequest.getVin());
                notification.setLink("/vin-requests/" + id);
                notification.setRead(false);
                notificationRepository.save(notification);
            }

            return ResponseEntity.ok(ApiResponse.success("Сообщение отправлено", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка отправки: " + e.getMessage()));
        }
    }
}