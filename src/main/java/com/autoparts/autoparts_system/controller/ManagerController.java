package com.autoparts.autoparts_system.controller;

import com.autoparts.autoparts_system.dto.response.ApiResponse;
import com.autoparts.autoparts_system.model.*;
import com.autoparts.autoparts_system.repository.*;
import com.autoparts.autoparts_system.security.JwtService;
import com.autoparts.autoparts_system.service.OrderService;
import com.autoparts.autoparts_system.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private JdbcTemplate jdbcTemplate;

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

    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    @Autowired
    private ReturnService returnService;  // Добавлен сервис для возвратов

    // ========== VIN ЗАЯВКИ ==========
    @GetMapping("/vin-requests")
    public ResponseEntity<ApiResponse> getAllVinRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<VinRequest> requestsPage;

            if (search != null && !search.trim().isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    requestsPage = vinRequestRepository.searchWithStatus(search, status, pageable);
                } else {
                    requestsPage = vinRequestRepository.search(search, pageable);
                }
            } else if (status != null && !status.isEmpty()) {
                requestsPage = vinRequestRepository.findByStatus(status, pageable);
            } else {
                requestsPage = vinRequestRepository.findAll(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", requestsPage.getContent(),
                    "total", requestsPage.getTotalElements(),
                    "totalPages", requestsPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Заявки загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки заявок: " + e.getMessage()));
        }
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

    // ========== ЗАКАЗЫ ==========
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "date"));
            Page<SalesOrder> ordersPage;

            if (search != null && !search.trim().isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    ordersPage = salesOrderRepository.searchWithStatus(search, status, pageable);
                } else {
                    ordersPage = salesOrderRepository.search(search, pageable);
                }
            } else if (status != null && !status.isEmpty()) {
                ordersPage = salesOrderRepository.findByStatus(status, pageable);
            } else {
                ordersPage = salesOrderRepository.findAll(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", ordersPage.getContent(),
                    "total", ordersPage.getTotalElements(),
                    "totalPages", ordersPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Заказы загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки заказов: " + e.getMessage()));
        }
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

    // ========== ЗАПРОСЫ ПОСТАВЩИКАМ ==========
    @GetMapping("/external-requests")
    public ResponseEntity<ApiResponse> getExternalRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ExternalRequest> requestsPage;

            if (search != null && !search.trim().isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    requestsPage = externalRequestRepository.searchWithStatus(search, status, pageable);
                } else {
                    requestsPage = externalRequestRepository.search(search, pageable);
                }
            } else if (status != null && !status.isEmpty()) {
                requestsPage = externalRequestRepository.findByStatus(status, pageable);
            } else {
                requestsPage = externalRequestRepository.findAll(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", requestsPage.getContent(),
                    "total", requestsPage.getTotalElements(),
                    "totalPages", requestsPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Запросы загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки запросов: " + e.getMessage()));
        }
    }

    @PutMapping("/external-requests/{id}/status")
    public ResponseEntity<ApiResponse> updateExternalRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            externalRequestRepository.updateStatus(id, status);

            Long userId = externalRequestRepository.findUserIdById(id);
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType("EXTERNAL_REQUEST_STATUS");
            notification.setTitle("Статус вашего запроса изменен");
            notification.setMessage("Статус запроса на товар изменен на: " + getStatusText(status));
            notification.setLink("/profile?tab=external-requests");
            notification.setRead(false);
            notificationRepository.save(notification);

            return ResponseEntity.ok(ApiResponse.success("Статус обновлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    @PostMapping("/external-requests")
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

            return ResponseEntity.ok(ApiResponse.success("Запрос отправлен", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка: " + e.getMessage()));
        }
    }

    // ========== ВОЗВРАТЫ ==========
    @GetMapping("/returns")
    public ResponseEntity<ApiResponse> getAllReturns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status
    ) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Return> returnsPage;

            if (search != null && !search.trim().isEmpty()) {
                if (status != null && !status.isEmpty()) {
                    returnsPage = returnService.searchReturnsWithStatus(search, status, pageable);
                } else {
                    returnsPage = returnService.searchReturns(search, pageable);
                }
            } else if (status != null && !status.isEmpty()) {
                returnsPage = returnService.getReturnsByStatus(status, pageable);
            } else {
                returnsPage = returnService.getAllReturns(pageable);
            }

            Map<String, Object> response = Map.of(
                    "data", returnsPage.getContent(),
                    "total", returnsPage.getTotalElements(),
                    "totalPages", returnsPage.getTotalPages(),
                    "currentPage", page
            );

            return ResponseEntity.ok(ApiResponse.success("Заявки на возврат загружены", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Ошибка загрузки заявок: " + e.getMessage()));
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

    private String getStatusText(String status) {
        switch(status) {
            case "PENDING": return "Ожидает обработки";
            case "PROCESSING": return "В обработке";
            case "ORDERED": return "Заказан у поставщика";
            case "COMPLETED": return "Выполнен";
            case "REJECTED": return "Отклонен";
            default: return status;
        }
    }
}